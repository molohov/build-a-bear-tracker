package com.buildabear.tracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.buildabear.tracker.data.repository.ImportRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CatalogImportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val importRepository: ImportRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val result = importRepository.importFromWiki(
                maxPages = inputData.getInt(KEY_MAX_PAGES, 200),
            )
            if (result.errors.isNotEmpty() && result.pagesFetched == 0) {
                Result.failure()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "catalog_import"
        const val KEY_MAX_PAGES = "max_pages"
    }
}
