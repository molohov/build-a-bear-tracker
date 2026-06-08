package com.buildabear.tracker.domain.usecase

import android.content.Context
import com.buildabear.tracker.data.local.dao.BearDao
import com.buildabear.tracker.data.local.dao.CollectionStatusDao
import com.buildabear.tracker.data.local.parseImageUrls
import com.buildabear.tracker.data.local.toDomainType
import com.buildabear.tracker.domain.model.CollectionStatusType
import com.buildabear.tracker.domain.model.SourceType
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class ExportCollectionUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bearDao: BearDao,
    private val collectionStatusDao: CollectionStatusDao,
    private val moshi: Moshi,
) {
    suspend fun exportToZip(): File {
        val bearList = bearDao.observeAllBears().first()
        val statusList = collectionStatusDao.observeAllStatuses().first()
        val statusMap = statusList.associateBy { it.bearId }

        val exportBears = bearList.mapNotNull { entity ->
            val status = statusMap[entity.id]
            val statusType = status.toDomainType()
            val include = statusType != CollectionStatusType.UNSET || entity.sourceType == SourceType.CUSTOM.name
            if (!include) return@mapNotNull null
            val categories = bearDao.getCategoriesForBear(entity.id)
            val localImage = if (entity.sourceType == SourceType.CUSTOM.name && entity.localImagePath != null) {
                "images/${entity.id}.jpg"
            } else {
                null
            }
            ExportBear(
                id = entity.id,
                sourceType = entity.sourceType,
                name = entity.name,
                description = entity.description,
                yearReleased = entity.yearReleased,
                furColor = entity.furColor,
                eyeColor = entity.eyeColor,
                height = entity.height,
                weight = entity.weight,
                sku = entity.sku,
                price = entity.price,
                available = entity.available,
                imageUrls = parseImageUrls(entity.imageUrlsJson, moshi),
                status = statusType.name,
                notes = status?.notes,
                categories = categories,
                localImage = localImage,
                sourceUrl = entity.sourceUrl,
            )
        }

        val payload = ExportPayload(
            exportVersion = 1,
            exportedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            bears = exportBears,
        )

        val json = moshi.adapter(ExportPayload::class.java).toJson(payload)
        val exportDir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val zipFile = File(exportDir, "build-a-bear-collection-${System.currentTimeMillis()}.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
            zip.putNextEntry(ZipEntry("collection.json"))
            zip.write(json.toByteArray())
            zip.closeEntry()

            bearList.filter { it.sourceType == SourceType.CUSTOM.name && it.localImagePath != null }.forEach { entity ->
                val imageFile = File(entity.localImagePath!!)
                if (imageFile.exists()) {
                    zip.putNextEntry(ZipEntry("images/${entity.id}.jpg"))
                    imageFile.inputStream().use { input -> input.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }

        return zipFile
    }
}

@JsonClass(generateAdapter = true)
data class ExportPayload(
    val exportVersion: Int,
    val exportedAt: String,
    val bears: List<ExportBear>,
)

@JsonClass(generateAdapter = true)
data class ExportBear(
    val id: String,
    val sourceType: String,
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
    val status: String,
    val notes: String? = null,
    val categories: List<String> = emptyList(),
    val localImage: String? = null,
    val sourceUrl: String? = null,
)
