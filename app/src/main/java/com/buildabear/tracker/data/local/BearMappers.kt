package com.buildabear.tracker.data.local

import com.buildabear.tracker.data.local.entity.BearEntity
import com.buildabear.tracker.data.local.entity.CollectionStatusEntity
import com.buildabear.tracker.data.local.entity.SavedFilterEntity
import com.buildabear.tracker.domain.model.Bear
import com.buildabear.tracker.domain.model.BearWithStatus
import com.buildabear.tracker.domain.model.CollectionStatusType
import com.buildabear.tracker.domain.model.FilterCriteria
import com.buildabear.tracker.domain.model.SavedFilter
import com.buildabear.tracker.domain.model.SourceType
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)

fun BearEntity.toDomain(categories: List<String> = emptyList()): Bear = Bear(
    id = id,
    sourceType = SourceType.valueOf(sourceType),
    externalId = externalId,
    name = name,
    slug = slug,
    description = description,
    yearReleased = yearReleased,
    furColor = furColor,
    eyeColor = eyeColor,
    height = height,
    weight = weight,
    sku = sku,
    price = price,
    available = available,
    imageUrls = parseImageUrls(imageUrlsJson),
    localImagePath = localImagePath,
    sourceUrl = sourceUrl,
    sourceName = sourceName,
    extraMetadataJson = extraMetadataJson,
    categories = categories,
    importedAt = importedAt,
    updatedAt = updatedAt,
    createdAt = createdAt,
)

fun Bear.toEntity(): BearEntity = BearEntity(
    id = id,
    sourceType = sourceType.name,
    externalId = externalId,
    name = name,
    slug = slug,
    description = description,
    yearReleased = yearReleased,
    furColor = furColor,
    eyeColor = eyeColor,
    height = height,
    weight = weight,
    sku = sku,
    price = price,
    available = available,
    imageUrlsJson = encodeImageUrls(imageUrls),
    localImagePath = localImagePath,
    sourceUrl = sourceUrl,
    sourceName = sourceName,
    extraMetadataJson = extraMetadataJson,
    importedAt = importedAt,
    updatedAt = updatedAt,
    createdAt = createdAt,
)

fun BearWithStatus.toDisplay(): BearWithStatus = this

fun CollectionStatusEntity?.toDomainType(): CollectionStatusType =
    this?.status?.let { runCatching { CollectionStatusType.valueOf(it) }.getOrNull() }
        ?: CollectionStatusType.UNSET

fun SavedFilterEntity.toDomain(moshi: Moshi): SavedFilter = SavedFilter(
    id = id,
    name = name,
    criteria = moshi.adapter(FilterCriteria::class.java).fromJson(criteriaJson) ?: FilterCriteria(),
    sortOrder = sortOrder,
    createdAt = createdAt,
)

fun SavedFilter.toEntity(moshi: Moshi): SavedFilterEntity = SavedFilterEntity(
    id = id,
    name = name,
    criteriaJson = moshi.adapter(FilterCriteria::class.java).toJson(criteria),
    sortOrder = sortOrder,
    createdAt = createdAt,
)

fun parseImageUrls(json: String, moshi: Moshi = Moshi.Builder().build()): List<String> {
    return runCatching {
        moshi.adapter<List<String>>(stringListType).fromJson(json) ?: emptyList()
    }.getOrDefault(emptyList())
}

fun encodeImageUrls(urls: List<String>, moshi: Moshi = Moshi.Builder().build()): String {
    return moshi.adapter<List<String>>(stringListType).toJson(urls)
}
