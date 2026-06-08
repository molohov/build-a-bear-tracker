package com.buildabear.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_filters")
data class SavedFilterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val criteriaJson: String,
    val sortOrder: Int,
    val createdAt: Long,
)
