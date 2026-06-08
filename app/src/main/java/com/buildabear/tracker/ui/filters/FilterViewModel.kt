package com.buildabear.tracker.ui.filters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildabear.tracker.data.repository.BearRepository
import com.buildabear.tracker.data.repository.FilterViewRepository
import com.buildabear.tracker.domain.model.FilterCriteria
import com.buildabear.tracker.domain.model.SavedFilter
import com.buildabear.tracker.domain.model.YearFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilterBuilderState(
    val name: String = "",
    val status: Set<String> = emptySet(),
    val furColors: Set<String> = emptySet(),
    val eyeColors: Set<String> = emptySet(),
    val categories: Set<String> = emptySet(),
    val yearContains: String = "",
    val sourceType: Set<String> = emptySet(),
    val available: Boolean? = null,
    val filterId: String? = null,
)

data class FilterUiState(
    val savedFilters: List<SavedFilter> = emptyList(),
    val defaultViews: List<Pair<String, FilterCriteria>> = emptyList(),
    val furColorOptions: List<String> = emptyList(),
    val eyeColorOptions: List<String> = emptyList(),
    val categoryOptions: List<String> = emptyList(),
    val builder: FilterBuilderState = FilterBuilderState(),
)

@HiltViewModel
class FilterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val filterViewRepository: FilterViewRepository,
    private val bearRepository: BearRepository,
) : ViewModel() {
    private val editFilterId: String? = savedStateHandle.get<String>("filterId")?.takeIf { it.isNotBlank() }
    private val builderState = MutableStateFlow(FilterBuilderState())
    private val optionsState = MutableStateFlow(Triple<List<String>, List<String>, List<String>>(emptyList(), emptyList(), emptyList()))

    val uiState: StateFlow<FilterUiState> = combine(
        filterViewRepository.observeSavedFilters(),
        builderState,
        optionsState,
    ) { saved, builder, (fur, eye, cats) ->
        FilterUiState(
            savedFilters = saved,
            defaultViews = filterViewRepository.getDefaultFilters(),
            furColorOptions = fur,
            eyeColorOptions = eye,
            categoryOptions = cats,
            builder = builder,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilterUiState())

    init {
        viewModelScope.launch {
            optionsState.value = Triple(
                bearRepository.getDistinctFurColors(),
                bearRepository.getDistinctEyeColors(),
                bearRepository.getDistinctCategories(),
            )
            if (editFilterId != null) {
                filterViewRepository.getFilterById(editFilterId)?.let { loadFilter(it) }
            } else {
                startNewFilter()
            }
        }
    }

    fun startNewFilter() {
        builderState.value = FilterBuilderState()
    }

    fun loadFilter(filter: SavedFilter) {
        val c = filter.criteria
        builderState.value = FilterBuilderState(
            name = filter.name,
            status = c.status?.toSet() ?: emptySet(),
            furColors = c.furColor?.toSet() ?: emptySet(),
            eyeColors = c.eyeColor?.toSet() ?: emptySet(),
            categories = c.categories?.toSet() ?: emptySet(),
            yearContains = c.yearReleased?.contains.orEmpty(),
            sourceType = c.sourceType?.toSet() ?: emptySet(),
            available = c.available,
            filterId = filter.id,
        )
    }

    fun updateName(v: String) = builderState.update { it.copy(name = v) }
    fun updateYear(v: String) = builderState.update { it.copy(yearContains = v) }

    fun toggleStatus(s: String) = builderState.update {
        it.copy(status = it.status.toggle(s))
    }

    fun toggleFurColor(c: String) = builderState.update {
        it.copy(furColors = it.furColors.toggle(c))
    }

    fun toggleEyeColor(c: String) = builderState.update {
        it.copy(eyeColors = it.eyeColors.toggle(c))
    }

    fun toggleCategory(c: String) = builderState.update {
        it.copy(categories = it.categories.toggle(c))
    }

    fun toggleSourceType(s: String) = builderState.update {
        it.copy(sourceType = it.sourceType.toggle(s))
    }

    fun setAvailable(value: Boolean?) = builderState.update { it.copy(available = value) }

    fun saveFilter(onSaved: () -> Unit) {
        val builder = builderState.value
        if (builder.name.isBlank()) return
        viewModelScope.launch {
            val criteria = FilterCriteria(
                status = builder.status.takeIf { it.isNotEmpty() }?.toList(),
                furColor = builder.furColors.takeIf { it.isNotEmpty() }?.toList(),
                eyeColor = builder.eyeColors.takeIf { it.isNotEmpty() }?.toList(),
                categories = builder.categories.takeIf { it.isNotEmpty() }?.toList(),
                yearReleased = builder.yearContains.takeIf { it.isNotBlank() }?.let { YearFilter(it) },
                sourceType = builder.sourceType.takeIf { it.isNotEmpty() }?.toList(),
                available = builder.available,
            )
            filterViewRepository.saveFilter(builder.name, criteria, builder.filterId)
            onSaved()
        }
    }

    fun deleteFilter(id: String) {
        viewModelScope.launch { filterViewRepository.deleteFilter(id) }
    }
}

private fun Set<String>.toggle(value: String): Set<String> =
    if (value in this) this - value else this + value
