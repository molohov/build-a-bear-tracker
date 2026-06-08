package com.buildabear.tracker.data.repository

import com.buildabear.tracker.domain.model.FilterCriteria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveFilterStore @Inject constructor() {
    private val _pendingSelection = MutableStateFlow<Pair<String, FilterCriteria>?>(null)
    val pendingSelection: StateFlow<Pair<String, FilterCriteria>?> = _pendingSelection.asStateFlow()

    fun setSelection(name: String, criteria: FilterCriteria) {
        _pendingSelection.value = name to criteria
    }

    fun consumeSelection(): Pair<String, FilterCriteria>? {
        val value = _pendingSelection.value
        _pendingSelection.value = null
        return value
    }
}
