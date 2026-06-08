package com.buildabear.tracker.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildabear.tracker.data.repository.BearRepository
import com.buildabear.tracker.data.repository.FilterViewRepository
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.buildabear.tracker.data.seed.SeedCatalogLoader
import com.buildabear.tracker.worker.CatalogImportWorker
import com.buildabear.tracker.domain.model.BearWithStatus
import com.buildabear.tracker.domain.model.FilterCriteria
import com.buildabear.tracker.domain.model.SourceType
import com.buildabear.tracker.util.FilterMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BearListUiState(
    val bears: List<BearWithStatus> = emptyList(),
    val searchQuery: String = "",
    val activeCriteria: FilterCriteria = FilterCriteria(),
    val selectedViewName: String = "All",
    val isLoading: Boolean = true,
)

@HiltViewModel
class BearListViewModel @Inject constructor(
    private val bearRepository: BearRepository,
    private val filterViewRepository: FilterViewRepository,
    private val seedCatalogLoader: SeedCatalogLoader,
    private val workManager: WorkManager,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val activeCriteria = MutableStateFlow(FilterCriteria())
    private val selectedViewName = MutableStateFlow("All")
    private val isLoading = MutableStateFlow(true)
    private val isRefreshing = MutableStateFlow(false)

    val isRefreshingState: StateFlow<Boolean> = isRefreshing

    val uiState: StateFlow<BearListUiState> = combine(
        bearRepository.observeBearsWithStatus(),
        searchQuery,
        activeCriteria,
        selectedViewName,
        isLoading,
    ) { bears, query, criteria, viewName, loading ->
        val mergedCriteria = criteria.copy(
            searchQuery = query.takeIf { it.isNotBlank() },
        )
        val filtered = bears.filter { FilterMatcher.matches(it, mergedCriteria) }
        BearListUiState(
            bears = filtered,
            searchQuery = query,
            activeCriteria = criteria,
            selectedViewName = viewName,
            isLoading = loading,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BearListUiState())

    val defaultViews = filterViewRepository.getDefaultFilters()

    init {
        viewModelScope.launch {
            val count = bearRepository.getBearCount()
            seedCatalogLoader.loadIfEmpty(count)
            isLoading.value = false
        }
    }

    fun onSearchChange(query: String) {
        searchQuery.value = query
    }

    fun applyView(name: String, criteria: FilterCriteria) {
        selectedViewName.value = name
        activeCriteria.value = criteria
    }

    fun setSourceFilter(sourceType: SourceType?) {
        activeCriteria.update {
            it.copy(
                sourceType = sourceType?.let { st -> listOf(st.name) },
            )
        }
        selectedViewName.value = when (sourceType) {
            SourceType.CATALOG -> "Catalog"
            SourceType.CUSTOM -> "Custom"
            null -> "All"
        }
    }

    fun setStatusFilter(status: String?) {
        activeCriteria.update {
            it.copy(status = status?.let { s -> listOf(s) })
        }
    }

    fun refreshCatalog() {
        isRefreshing.value = true
        val request = OneTimeWorkRequestBuilder<CatalogImportWorker>()
            .setInputData(workDataOf(CatalogImportWorker.KEY_MAX_PAGES to 1500))
            .build()
        workManager.enqueueUniqueWork(
            CatalogImportWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            isRefreshing.value = false
        }
    }
}
