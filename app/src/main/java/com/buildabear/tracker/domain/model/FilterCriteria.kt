package com.buildabear.tracker.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FilterCriteria(
    val status: List<String>? = null,
    val yearReleased: YearFilter? = null,
    val furColor: List<String>? = null,
    val eyeColor: List<String>? = null,
    val categories: List<String>? = null,
    val available: Boolean? = null,
    val sourceType: List<String>? = null,
    val searchQuery: String? = null,
)

@JsonClass(generateAdapter = true)
data class YearFilter(
    val contains: String? = null,
)

data class SavedFilter(
    val id: String,
    val name: String,
    val criteria: FilterCriteria,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
