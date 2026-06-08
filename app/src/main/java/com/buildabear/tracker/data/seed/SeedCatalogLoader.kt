package com.buildabear.tracker.data.seed

import android.content.Context
import com.buildabear.tracker.data.repository.ImportRepository
import com.buildabear.tracker.data.repository.SeedBear
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedCatalogLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val importRepository: ImportRepository,
    private val moshi: Moshi,
) {
    suspend fun loadIfEmpty(bearCount: Int) {
        if (bearCount > 0) return
        val json = context.assets.open("seed_catalog.json").bufferedReader().use { it.readText() }
        val listType = Types.newParameterizedType(List::class.java, SeedBearJson::class.java)
        val seeds = moshi.adapter<List<SeedBearJson>>(listType).fromJson(json).orEmpty()
        importRepository.importSeedBears(seeds.map { it.toSeedBear() })
    }
}

@JsonClass(generateAdapter = true)
data class SeedBearJson(
    val id: String,
    @Json(name = "external_id") val externalId: String? = null,
    val name: String,
    val description: String? = null,
    @Json(name = "year_released") val yearReleased: String? = null,
    @Json(name = "fur_color") val furColor: String? = null,
    @Json(name = "eye_color") val eyeColor: String? = null,
    val height: String? = null,
    val weight: String? = null,
    val sku: String? = null,
    val price: String? = null,
    val available: Boolean? = null,
    @Json(name = "image_urls") val imageUrls: List<String> = emptyList(),
    @Json(name = "source_url") val sourceUrl: String? = null,
    val categories: List<String> = emptyList(),
) {
    fun toSeedBear() = SeedBear(
        id = id,
        externalId = externalId,
        name = name,
        description = description,
        yearReleased = yearReleased,
        furColor = furColor,
        eyeColor = eyeColor,
        height = height,
        weight = weight,
        sku = sku,
        price = price,
        available = available,
        imageUrls = imageUrls,
        sourceUrl = sourceUrl,
        categories = categories,
    )
}
