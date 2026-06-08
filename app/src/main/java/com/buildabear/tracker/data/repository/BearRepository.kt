package com.buildabear.tracker.data.repository

import com.buildabear.tracker.data.local.dao.BearDao
import com.buildabear.tracker.data.local.dao.CollectionStatusDao
import com.buildabear.tracker.data.local.entity.CollectionStatusEntity
import com.buildabear.tracker.data.local.toDomain
import com.buildabear.tracker.data.local.toDomainType
import com.buildabear.tracker.data.local.toEntity
import com.buildabear.tracker.data.storage.CustomImageStore
import com.buildabear.tracker.domain.model.Bear
import com.buildabear.tracker.domain.model.BearWithStatus
import com.buildabear.tracker.domain.model.CollectionStatusType
import com.buildabear.tracker.domain.model.SourceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BearRepository @Inject constructor(
    private val bearDao: BearDao,
    private val collectionStatusDao: CollectionStatusDao,
    private val customImageStore: CustomImageStore,
) {
    fun observeBearsWithStatus(): Flow<List<BearWithStatus>> =
        combine(
            bearDao.observeAllBears(),
            collectionStatusDao.observeAllStatuses(),
            bearDao.observeAllCategories(),
        ) { bears, statuses, allCategories ->
            val statusMap = statuses.associateBy { it.bearId }
            val categoryMap = allCategories.groupBy { it.bearId }.mapValues { entry ->
                entry.value.map { it.category }
            }
            bears.map { entity ->
                BearWithStatus(
                    bear = entity.toDomain(categoryMap[entity.id].orEmpty()),
                    status = statusMap[entity.id].toDomainType(),
                    notes = statusMap[entity.id]?.notes,
                )
            }
        }

    suspend fun getBearWithStatus(id: String): BearWithStatus? {
        val entity = bearDao.getBearById(id) ?: return null
        val categories = bearDao.getCategoriesForBear(id)
        val status = collectionStatusDao.getStatusForBear(id)
        return BearWithStatus(
            bear = entity.toDomain(categories),
            status = status.toDomainType(),
            notes = status?.notes,
        )
    }

    suspend fun updateStatus(bearId: String, status: CollectionStatusType, notes: String? = null) {
        collectionStatusDao.upsertStatus(
            CollectionStatusEntity(
                bearId = bearId,
                status = status.name,
                notes = notes,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun saveCustomBear(bear: Bear, categories: List<String>): String {
        val id = bear.id.ifBlank { UUID.randomUUID().toString() }
        val now = System.currentTimeMillis()
        val entity = bear.copy(
            id = id,
            sourceType = SourceType.CUSTOM,
            sourceName = null,
            sourceUrl = null,
            updatedAt = now,
            createdAt = if (bear.createdAt > 0) bear.createdAt else now,
        ).toEntity()
        bearDao.upsertBear(entity)
        bearDao.replaceCategories(id, categories)
        collectionStatusDao.upsertStatus(
            CollectionStatusEntity(
                bearId = id,
                status = CollectionStatusType.OWNED.name,
                notes = bear.description,
                updatedAt = now,
            ),
        )
        return id
    }

    suspend fun deleteCustomBear(id: String) {
        val bear = bearDao.getBearById(id) ?: return
        if (bear.sourceType != SourceType.CUSTOM.name) return
        customImageStore.deleteImage(bear.localImagePath)
        collectionStatusDao.deleteStatusForBear(id)
        bearDao.deleteCustomBear(id)
    }

    suspend fun getDistinctFurColors(): List<String> = bearDao.getDistinctFurColors()
    suspend fun getDistinctEyeColors(): List<String> = bearDao.getDistinctEyeColors()
    suspend fun getDistinctYears(): List<String> = bearDao.getDistinctYears()
    suspend fun getDistinctCategories(): List<String> = bearDao.getDistinctCategories()
    suspend fun getBearCount(): Int = bearDao.getBearCount()
}
