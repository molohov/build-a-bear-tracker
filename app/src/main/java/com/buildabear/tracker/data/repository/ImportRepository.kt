package com.buildabear.tracker.data.repository

import com.buildabear.tracker.data.local.dao.BearDao
import com.buildabear.tracker.data.local.dao.ImportRunDao
import com.buildabear.tracker.data.local.entity.BearCategoryEntity
import com.buildabear.tracker.data.local.entity.BearEntity
import com.buildabear.tracker.data.local.entity.ImportRunEntity
import com.buildabear.tracker.data.local.encodeImageUrls
import com.buildabear.tracker.data.remote.WikiPageDetail
import com.buildabear.tracker.data.remote.MediaWikiApi
import com.buildabear.tracker.domain.model.SourceType
import com.buildabear.tracker.util.InfoboxParser
import com.buildabear.tracker.util.ParsedBear
import com.squareup.moshi.Moshi
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportRepository @Inject constructor(
    private val mediaWikiApi: MediaWikiApi,
    private val bearDao: BearDao,
    private val importRunDao: ImportRunDao,
    private val moshi: Moshi,
) {
    suspend fun importFromWiki(maxPages: Int = 500): ImportResult {
        val runId = importRunDao.insertRun(
            ImportRunEntity(startedAt = System.currentTimeMillis()),
        )
        var pagesFetched = 0
        val errors = mutableListOf<String>()
        var continueToken: String? = null

        try {
            do {
                val response = mediaWikiApi.queryAllPages(continueToken = continueToken)
                val pages = response.query?.allpages.orEmpty()
                if (pages.isEmpty()) break

                pages.chunked(20).forEach { chunk ->
                    if (pagesFetched >= maxPages) return@forEach
                    try {
                        importPageBatch(chunk.map { it.title })
                        pagesFetched += chunk.size
                        delay(1000)
                    } catch (e: Exception) {
                        errors.add("Batch error: ${e.message}")
                    }
                }

                continueToken = response.continueToken?.apcontinue
            } while (continueToken != null && pagesFetched < maxPages)

            val run = importRunDao.getLatestRun()
            if (run != null) {
                importRunDao.updateRun(
                    run.copy(
                        finishedAt = System.currentTimeMillis(),
                        pagesFetched = pagesFetched,
                        errors = errors.takeIf { it.isNotEmpty() }?.joinToString("\n"),
                    ),
                )
            }
        } catch (e: Exception) {
            errors.add(e.message ?: "Unknown error")
            val run = importRunDao.getLatestRun()
            if (run != null) {
                importRunDao.updateRun(
                    run.copy(
                        finishedAt = System.currentTimeMillis(),
                        pagesFetched = pagesFetched,
                        errors = errors.joinToString("\n"),
                    ),
                )
            }
        }

        return ImportResult(pagesFetched = pagesFetched, errors = errors)
    }

    private suspend fun importPageBatch(titles: List<String>) {
        val titlesParam = titles.joinToString("|")
        val contentResponse = mediaWikiApi.queryPages(
            prop = "revisions",
            titles = titlesParam,
            rvProp = "content",
            rvSlots = "main",
            redirects = 1,
        )
        val categoryResponse = mediaWikiApi.queryPages(
            prop = "categories",
            titles = titlesParam,
            clLimit = 50,
            redirects = 1,
        )

        val contentPages = contentResponse.query?.pages.orEmpty()
        val categoryPages = categoryResponse.query?.pages.orEmpty()

        data class PageImportData(
            val page: WikiPageDetail,
            val wikitext: String,
            val categories: List<String>,
            val parsed: ParsedBear?,
            val imageFileNames: List<String>,
        )

        val pageData = mutableListOf<PageImportData>()
        val imageFileNames = mutableSetOf<String>()
        val pagesNeedingThumbnail = mutableListOf<String>()

        for ((_, page) in contentPages) {
            if (page.pageid <= 0) continue
            val wikitext = page.revisions?.firstOrNull()?.slots?.main?.content.orEmpty()
            val catPage = categoryPages[page.pageid.toString()]
                ?: categoryPages.values.find { it.pageid == page.pageid }
            val rawCategories = catPage?.categories.orEmpty()
                .map { it.title.removePrefix("Category:") }
                .filter { !it.startsWith("Browse") && !it.contains("Stub") }

            val fields = InfoboxParser.parse(wikitext)
            val fileNames = InfoboxParser.extractImageFileNamesFromWikitext(wikitext)
            val parsed = if (fields.isNotEmpty()) {
                InfoboxParser.mapToBearFields(fields, page.title, page.pageid, rawCategories)
            } else {
                null
            }

            if (fileNames.isEmpty()) {
                pagesNeedingThumbnail.add(page.title)
            } else {
                imageFileNames.addAll(fileNames)
            }

            pageData.add(
                PageImportData(
                    page = page,
                    wikitext = wikitext,
                    categories = rawCategories,
                    parsed = parsed,
                    imageFileNames = fileNames,
                ),
            )
        }

        val imageUrlByFile = resolveImageUrls(imageFileNames)
        val thumbnailByPage = resolvePageThumbnails(pagesNeedingThumbnail)

        val entities = mutableListOf<BearEntity>()
        val categories = mutableListOf<BearCategoryEntity>()

        for (data in pageData) {
            val page = data.page
            val imageUrls = resolvePageImageUrls(
                fileNames = data.imageFileNames,
                imageUrlByFile = imageUrlByFile,
                pageTitle = page.title,
                thumbnailByPage = thumbnailByPage,
            )

            val existing = bearDao.getCatalogBearByExternalId(page.pageid.toString())
                ?: bearDao.getCatalogBearByName(page.title)
            val id = existing?.id ?: UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val entity = if (data.parsed != null) {
                BearEntity(
                    id = id,
                    sourceType = SourceType.CATALOG.name,
                    externalId = data.parsed.externalId,
                    name = data.parsed.name,
                    description = data.parsed.description,
                    yearReleased = data.parsed.yearReleased,
                    furColor = data.parsed.furColor,
                    eyeColor = data.parsed.eyeColor,
                    height = data.parsed.height,
                    weight = data.parsed.weight,
                    sku = data.parsed.sku,
                    price = data.parsed.price,
                    available = data.parsed.available,
                    imageUrlsJson = encodeImageUrls(imageUrls, moshi),
                    sourceUrl = data.parsed.sourceUrl,
                    sourceName = "fandom",
                    importedAt = now,
                    updatedAt = now,
                    createdAt = existing?.createdAt ?: now,
                )
            } else {
                BearEntity(
                    id = id,
                    sourceType = SourceType.CATALOG.name,
                    externalId = page.pageid.toString(),
                    name = page.title,
                    imageUrlsJson = encodeImageUrls(imageUrls, moshi),
                    sourceUrl = "https://buildabear.fandom.com/wiki/${page.title.replace(' ', '_')}",
                    sourceName = "fandom",
                    importedAt = now,
                    updatedAt = now,
                    createdAt = existing?.createdAt ?: now,
                )
            }
            entities.add(entity)
            categories.addAll(data.categories.map { BearCategoryEntity(id, it) })
        }

        if (entities.isNotEmpty()) {
            bearDao.upsertBears(entities)
            bearDao.upsertCategories(categories.distinctBy { it.bearId to it.category })
        }
    }

    private suspend fun resolveImageUrls(fileNames: Collection<String>): Map<String, String> {
        if (fileNames.isEmpty()) return emptyMap()
        val urls = mutableMapOf<String, String>()
        fileNames.distinct().chunked(50).forEach { chunk ->
            val titles = chunk.joinToString("|") { "File:$it" }
            val response = mediaWikiApi.queryPages(
                prop = "imageinfo",
                titles = titles,
                iiProp = "url",
                iiUrlWidth = 400,
            )
            for ((_, page) in response.query?.pages.orEmpty()) {
                val fileName = page.title.removePrefix("File:").trim()
                val imageUrl = page.imageinfo?.firstOrNull()?.thumburl
                    ?: page.imageinfo?.firstOrNull()?.url
                if (imageUrl != null) {
                    urls[fileName] = imageUrl
                }
            }
        }
        return urls
    }

    private suspend fun resolvePageThumbnails(pageTitles: List<String>): Map<String, String> {
        if (pageTitles.isEmpty()) return emptyMap()
        val thumbnails = mutableMapOf<String, String>()
        pageTitles.distinct().chunked(50).forEach { chunk ->
            val titles = chunk.joinToString("|")
            val response = mediaWikiApi.queryPages(
                prop = "pageimages",
                titles = titles,
                piProp = "thumbnail",
                piThumbSize = 400,
                redirects = 1,
            )
            val redirects = response.query?.redirects.orEmpty().associate { it.from to it.to }
            for ((_, page) in response.query?.pages.orEmpty()) {
                val imageUrl = page.thumbnail?.source ?: continue
                thumbnails[page.title] = imageUrl
                redirects.filterValues { it == page.title }.keys.forEach { fromTitle ->
                    thumbnails[fromTitle] = imageUrl
                }
            }
        }
        return thumbnails
    }

    private fun resolvePageImageUrls(
        fileNames: List<String>,
        imageUrlByFile: Map<String, String>,
        pageTitle: String,
        thumbnailByPage: Map<String, String>,
    ): List<String> {
        for (fileName in fileNames) {
            imageUrlByFile[fileName]?.let { return listOf(it) }
        }
        thumbnailByPage[pageTitle]?.let { return listOf(it) }
        return emptyList()
    }

    suspend fun importSeedBears(seedBears: List<SeedBear>) {
        val now = System.currentTimeMillis()
        val entities = seedBears.map { seed ->
            BearEntity(
                id = seed.id,
                sourceType = SourceType.CATALOG.name,
                externalId = seed.externalId,
                name = seed.name,
                description = seed.description,
                yearReleased = seed.yearReleased,
                furColor = seed.furColor,
                eyeColor = seed.eyeColor,
                height = seed.height,
                weight = seed.weight,
                sku = seed.sku,
                price = seed.price,
                available = seed.available,
                imageUrlsJson = encodeImageUrls(seed.imageUrls, moshi),
                sourceUrl = seed.sourceUrl,
                sourceName = "fandom",
                importedAt = now,
                updatedAt = now,
                createdAt = now,
            )
        }
        bearDao.upsertBears(entities)
        val categories = seedBears.flatMap { seed ->
            seed.categories.map { BearCategoryEntity(seed.id, it) }
        }
        if (categories.isNotEmpty()) {
            bearDao.upsertCategories(categories)
        }
    }

    suspend fun getLatestImportRun(): ImportRunEntity? = importRunDao.getLatestRun()
}

data class ImportResult(
    val pagesFetched: Int,
    val errors: List<String>,
)

data class SeedBear(
    val id: String,
    val externalId: String?,
    val name: String,
    val description: String? = null,
    val yearReleased: String? = null,
    val furColor: String? = null,
    val eyeColor: String? = null,
    val height: String? = null,
    val weight: String? = null,
    val sku: String? = null,
    val price: String? = null,
    val available: Boolean? = null,
    val imageUrls: List<String> = emptyList(),
    val sourceUrl: String? = null,
    val categories: List<String> = emptyList(),
)
