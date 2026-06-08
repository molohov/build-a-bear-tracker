package com.buildabear.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "collection_status",
    foreignKeys = [
        ForeignKey(
            entity = BearEntity::class,
            parentColumns = ["id"],
            childColumns = ["bearId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("bearId", unique = true)],
)
data class CollectionStatusEntity(
    @PrimaryKey val bearId: String,
    val status: String,
    val notes: String? = null,
    val updatedAt: Long,
)
