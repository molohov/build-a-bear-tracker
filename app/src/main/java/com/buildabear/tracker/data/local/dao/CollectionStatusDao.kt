package com.buildabear.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.buildabear.tracker.data.local.entity.CollectionStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionStatusDao {
    @Query("SELECT * FROM collection_status")
    fun observeAllStatuses(): Flow<List<CollectionStatusEntity>>

    @Query("SELECT * FROM collection_status WHERE bearId = :bearId")
    suspend fun getStatusForBear(bearId: String): CollectionStatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStatus(status: CollectionStatusEntity)

    @Query("DELETE FROM collection_status WHERE bearId = :bearId")
    suspend fun deleteStatusForBear(bearId: String)
}
