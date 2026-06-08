package com.buildabear.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bears")
data class BearEntity(
    @PrimaryKey val id: String,
    val sourceType: String,
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
    val imageUrlsJson: String = "[]",
    val localImagePath: String? = null,
    val sourceUrl: String? = null,
    val sourceName: String? = null,
    val extraMetadataJson: String? = null,
    val importedAt: Long? = null,
    val updatedAt: Long,
    val createdAt: Long,
)
