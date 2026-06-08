package com.buildabear.tracker.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.buildabear.tracker.data.local.entity.ImportRunEntity
import com.buildabear.tracker.data.repository.ImportRepository
import com.buildabear.tracker.domain.usecase.ExportCollectionUseCase
import com.buildabear.tracker.worker.CatalogImportWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val lastSyncText: String = "Never synced",
    val isSyncing: Boolean = false,
    val isExporting: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val importRepository: ImportRepository,
    private val exportCollectionUseCase: ExportCollectionUseCase,
    private val workManager: WorkManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshLastSync()
        observeSyncWork()
    }

    private fun refreshLastSync() {
        viewModelScope.launch {
            val run = importRepository.getLatestImportRun()
            _uiState.update {
                it.copy(lastSyncText = formatRun(run))
            }
        }
    }

    private fun observeSyncWork() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(CatalogImportWorker.WORK_NAME).collect { infos ->
                val running = infos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
                _uiState.update { it.copy(isSyncing = running) }
                if (infos.any { it.state == WorkInfo.State.SUCCEEDED }) {
                    refreshLastSync()
                    _uiState.update { it.copy(message = "Catalog sync completed") }
                }
                if (infos.any { it.state == WorkInfo.State.FAILED }) {
                    _uiState.update { it.copy(message = "Catalog sync failed") }
                }
            }
        }
    }

    fun syncCatalog() {
        val request = OneTimeWorkRequestBuilder<CatalogImportWorker>()
            .setInputData(workDataOf(CatalogImportWorker.KEY_MAX_PAGES to 1500))
            .build()
        workManager.enqueueUniqueWork(
            CatalogImportWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
        _uiState.update { it.copy(message = "Sync started in background") }
    }

    fun exportCollection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, message = null) }
            try {
                val zipFile = exportCollectionUseCase.exportToZip()
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    zipFile,
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "Export collection").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                _uiState.update { it.copy(message = "Export ready to share") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Export failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    private fun formatRun(run: ImportRunEntity?): String {
        if (run == null) return "Never synced"
        val date = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(run.startedAt))
        return if (run.finishedAt != null) {
            "Last sync: $date (${run.pagesFetched} pages)"
        } else {
            "Sync in progress since $date"
        }
    }
}
