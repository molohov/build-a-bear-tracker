package com.buildabear.tracker.data.repository

import com.buildabear.tracker.data.local.dao.BearDao
import com.buildabear.tracker.data.local.dao.ImportRunDao
import com.buildabear.tracker.data.local.entity.BearCategoryEntity
import com.buildabear.tracker.data.local.entity.BearEntity
import com.buildabear.tracker.data.local.entity.ImportRunEntity
import com.buildabear.tracker.data.local.encodeImageUrls
import com.buildabear.tracker.data.remote.MediaWikiApi
import com.buildabear.tracker.domain.model.SourceType
import com.buildabear.tracker.util.InfoboxParser
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
        )
        val categoryResponse = mediaWikiApi.queryPages(
            prop = "categories",
            titles = titlesParam,
            clLimit = 50,
        )

        val contentPages = contentResponse.query?.pages.orEmpty()
        val categoryPages = categoryResponse.query?.pages.orEmpty()

        val entities = mutableListOf<BearEntity>()
        val categories = mutableListOf<BearCategoryEntity>()

        for ((_, page) in contentPages) {
            if (page.pageid <= 0) continue
            val wikitext = page.revisions?.firstOrNull()?.slots?.main?.content.orEmpty()
            val catPage = categoryPages[page.pageid.toString()]
                ?: categoryPages.values.find { it.pageid == page.pageid }
            val rawCategories = catPage?.categories.orEmpty()
                .map { it.title.removePrefix("Category:") }
                .filter { !it.startsWith("Browse") && !it.contains("Stub") }

            val fields = InfoboxParser.parse(wikitext)
            val parsed = if (fields.isNotEmpty()) {
                InfoboxParser.mapToBearFields(fields, page.title, page.pageid, rawCategories)
            } else {
                null
            }

            val existing = bearDao.getCatalogBearByExternalId(page.pageid.toString())
            val id = existing?.id ?: UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val entity = if (parsed != null) {
                BearEntity(
                    id = id,
                    sourceType = SourceType.CATALOG.name,
                    externalId = parsed.externalId,
                    name = parsed.name,
                    description = parsed.description,
                    yearReleased = parsed.yearReleased,
                    furColor = parsed.furColor,
                    eyeColor = parsed.eyeColor,
                    height = parsed.height,
                    weight = parsed.weight,
                    sku = parsed.sku,
                    price = parsed.price,
                    available = parsed.available,
                    imageUrlsJson = encodeImageUrls(parsed.imageUrls, moshi),
                    sourceUrl = parsed.sourceUrl,
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
                    imageUrlsJson = "[]",
                    sourceUrl = "https://buildabear.fandom.com/wiki/${page.title.replace(' ', '_')}",
                    sourceName = "fandom",
                    importedAt = now,
                    updatedAt = now,
                    createdAt = existing?.createdAt ?: now,
                )
            }
            entities.add(entity)
            categories.addAll(rawCategories.map { BearCategoryEntity(id, it) })
        }

        if (entities.isNotEmpty()) {
            bearDao.upsertBears(entities)
            bearDao.upsertCategories(categories.distinctBy { it.bearId to it.category })
        }
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
