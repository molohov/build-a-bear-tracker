package com.buildabear.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.buildabear.tracker.data.local.entity.SavedFilterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedFilterDao {
    @Query("SELECT * FROM saved_filters ORDER BY sortOrder ASC, name ASC")
    fun observeAllFilters(): Flow<List<SavedFilterEntity>>

    @Query("SELECT * FROM saved_filters WHERE id = :id")
    suspend fun getFilterById(id: String): SavedFilterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFilter(filter: SavedFilterEntity)

    @Query("DELETE FROM saved_filters WHERE id = :id")
    suspend fun deleteFilter(id: String)
}
