package com.buildabear.tracker.util

import com.buildabear.tracker.domain.model.BearWithStatus
import com.buildabear.tracker.domain.model.CollectionStatusType
import com.buildabear.tracker.domain.model.FilterCriteria
import com.buildabear.tracker.domain.model.SourceType

object FilterMatcher {
    fun matches(bearWithStatus: BearWithStatus, criteria: FilterCriteria): Boolean {
        val bear = bearWithStatus.bear
        val status = bearWithStatus.status

        criteria.searchQuery?.takeIf { it.isNotBlank() }?.let { query ->
            val q = query.trim().lowercase()
            val haystack = listOfNotNull(
                bear.name,
                bear.sku,
                bear.description,
                bear.furColor,
                bear.eyeColor,
                bear.yearReleased,
            ).joinToString(" ").lowercase()
            if (!haystack.contains(q)) return false
        }

        criteria.status?.takeIf { it.isNotEmpty() }?.let { statuses ->
            if (status.name !in statuses) return false
        }

        criteria.sourceType?.takeIf { it.isNotEmpty() }?.let { types ->
            if (bear.sourceType.name !in types) return false
        }

        criteria.furColor?.takeIf { it.isNotEmpty() }?.let { colors ->
            val fur = bear.furColor?.lowercase() ?: return false
            if (colors.none { fur.contains(it.lowercase()) }) return false
        }

        criteria.eyeColor?.takeIf { it.isNotEmpty() }?.let { colors ->
            val eye = bear.eyeColor?.lowercase() ?: return false
            if (colors.none { eye.contains(it.lowercase()) }) return false
        }

        criteria.yearReleased?.contains?.takeIf { it.isNotBlank() }?.let { year ->
            val released = bear.yearReleased ?: return false
            if (!released.contains(year)) return false
        }

        criteria.categories?.takeIf { it.isNotEmpty() }?.let { cats ->
            if (cats.none { it in bear.categories }) return false
        }

        criteria.available?.let { available ->
            if (bear.available != available) return false
        }

        return true
    }

    fun defaultFilters(): List<Pair<String, FilterCriteria>> = listOf(
        "All" to FilterCriteria(),
        "Owned" to FilterCriteria(status = listOf(CollectionStatusType.OWNED.name)),
        "Want list" to FilterCriteria(status = listOf(CollectionStatusType.WANT.name)),
        "Don't want" to FilterCriteria(status = listOf(CollectionStatusType.DONT_WANT.name)),
        "My custom bears" to FilterCriteria(sourceType = listOf(SourceType.CUSTOM.name)),
    )
}
