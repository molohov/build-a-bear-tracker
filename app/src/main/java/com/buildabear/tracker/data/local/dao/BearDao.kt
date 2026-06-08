package com.buildabear.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.buildabear.tracker.data.local.entity.BearCategoryEntity
import com.buildabear.tracker.data.local.entity.BearEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BearDao {
    @Query("SELECT * FROM bears ORDER BY name COLLATE NOCASE ASC")
    fun observeAllBears(): Flow<List<BearEntity>>

    @Query("SELECT * FROM bears WHERE id = :id")
    suspend fun getBearById(id: String): BearEntity?

    @Query("SELECT * FROM bears WHERE externalId = :externalId AND sourceType = 'CATALOG' LIMIT 1")
    suspend fun getCatalogBearByExternalId(externalId: String): BearEntity?

    @Query("SELECT * FROM bears WHERE name = :name AND sourceType = 'CATALOG' LIMIT 1")
    suspend fun getCatalogBearByName(name: String): BearEntity?

    @Query("SELECT DISTINCT furColor FROM bears WHERE furColor IS NOT NULL AND furColor != '' ORDER BY furColor")
    suspend fun getDistinctFurColors(): List<String>

    @Query("SELECT DISTINCT eyeColor FROM bears WHERE eyeColor IS NOT NULL AND eyeColor != '' ORDER BY eyeColor")
    suspend fun getDistinctEyeColors(): List<String>

    @Query("SELECT DISTINCT yearReleased FROM bears WHERE yearReleased IS NOT NULL AND yearReleased != '' ORDER BY yearReleased DESC")
    suspend fun getDistinctYears(): List<String>

    @Query("SELECT COUNT(*) FROM bears")
    suspend fun getBearCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBears(bears: List<BearEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBear(bear: BearEntity)

    @Query("DELETE FROM bears WHERE id = :id AND sourceType = 'CUSTOM'")
    suspend fun deleteCustomBear(id: String): Int

    @Query("DELETE FROM bear_categories WHERE bearId = :bearId")
    suspend fun deleteCategoriesForBear(bearId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(categories: List<BearCategoryEntity>)

    @Query("SELECT category FROM bear_categories WHERE bearId = :bearId ORDER BY category")
    suspend fun getCategoriesForBear(bearId: String): List<String>

    @Query("SELECT DISTINCT category FROM bear_categories ORDER BY category")
    suspend fun getDistinctCategories(): List<String>

    @Query("SELECT * FROM bear_categories")
    fun observeAllCategories(): Flow<List<BearCategoryEntity>>

    @Transaction
    suspend fun replaceCategories(bearId: String, categories: List<String>) {
        deleteCategoriesForBear(bearId)
        if (categories.isNotEmpty()) {
            upsertCategories(categories.map { BearCategoryEntity(bearId, it) })
        }
    }
}
