package com.buildabear.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.buildabear.tracker.data.local.dao.BearDao
import com.buildabear.tracker.data.local.dao.CollectionStatusDao
import com.buildabear.tracker.data.local.dao.ImportRunDao
import com.buildabear.tracker.data.local.dao.SavedFilterDao
import com.buildabear.tracker.data.local.entity.BearCategoryEntity
import com.buildabear.tracker.data.local.entity.BearEntity
import com.buildabear.tracker.data.local.entity.CollectionStatusEntity
import com.buildabear.tracker.data.local.entity.ImportRunEntity
import com.buildabear.tracker.data.local.entity.SavedFilterEntity

@Database(
    entities = [
        BearEntity::class,
        BearCategoryEntity::class,
        CollectionStatusEntity::class,
        SavedFilterEntity::class,
        ImportRunEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bearDao(): BearDao
    abstract fun collectionStatusDao(): CollectionStatusDao
    abstract fun savedFilterDao(): SavedFilterDao
    abstract fun importRunDao(): ImportRunDao
}
