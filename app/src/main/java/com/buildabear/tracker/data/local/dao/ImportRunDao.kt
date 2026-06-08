package com.buildabear.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.buildabear.tracker.data.local.entity.ImportRunEntity

@Dao
interface ImportRunDao {
    @Insert
    suspend fun insertRun(run: ImportRunEntity): Long

    @Update
    suspend fun updateRun(run: ImportRunEntity)

    @Query("SELECT * FROM import_runs ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatestRun(): ImportRunEntity?
}
