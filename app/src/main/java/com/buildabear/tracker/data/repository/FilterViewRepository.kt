package com.buildabear.tracker.data.repository

import com.buildabear.tracker.data.local.dao.SavedFilterDao
import com.buildabear.tracker.data.local.toDomain
import com.buildabear.tracker.data.local.toEntity
import com.buildabear.tracker.domain.model.FilterCriteria
import com.buildabear.tracker.domain.model.SavedFilter
import com.buildabear.tracker.util.FilterMatcher
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterViewRepository @Inject constructor(
    private val savedFilterDao: SavedFilterDao,
    private val moshi: Moshi,
) {
    fun observeSavedFilters(): Flow<List<SavedFilter>> =
        savedFilterDao.observeAllFilters().map { entities ->
            entities.map { it.toDomain(moshi) }
        }

    suspend fun saveFilter(name: String, criteria: FilterCriteria, id: String? = null): String {
        val filterId = id ?: UUID.randomUUID().toString()
        val existing = id?.let { savedFilterDao.getFilterById(it) }
        savedFilterDao.upsertFilter(
            SavedFilter(
                id = filterId,
                name = name,
                criteria = criteria,
                sortOrder = existing?.sortOrder ?: 0,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            ).toEntity(moshi),
        )
        return filterId
    }

    suspend fun deleteFilter(id: String) {
        savedFilterDao.deleteFilter(id)
    }

    suspend fun getFilterById(id: String): SavedFilter? =
        savedFilterDao.getFilterById(id)?.toDomain(moshi)

    suspend fun ensureDefaultFilters() {
        val existing = savedFilterDao.observeAllFilters()
        // Defaults are provided in-memory via FilterMatcher; persisted filters are user-created only.
    }

    fun getDefaultFilters(): List<Pair<String, FilterCriteria>> = FilterMatcher.defaultFilters()
}
