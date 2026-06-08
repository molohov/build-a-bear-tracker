package com.buildabear.tracker.domain.model

data class Bear(
    val id: String,
    val sourceType: SourceType,
    val externalId: String? = null,
    val name: String,
    val slug: String? = null,
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
    val localImagePath: String? = null,
    val sourceUrl: String? = null,
    val sourceName: String? = null,
    val extraMetadataJson: String? = null,
    val categories: List<String> = emptyList(),
    val importedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
)

data class BearWithStatus(
    val bear: Bear,
    val status: CollectionStatusType = CollectionStatusType.UNSET,
    val notes: String? = null,
)
