package com.buildabear.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "bear_categories",
    primaryKeys = ["bearId", "category"],
    foreignKeys = [
        ForeignKey(
            entity = BearEntity::class,
            parentColumns = ["id"],
            childColumns = ["bearId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("bearId"), Index("category")],
)
data class BearCategoryEntity(
    val bearId: String,
    val category: String,
)
