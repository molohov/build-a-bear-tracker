package com.buildabear.tracker.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildabear.tracker.data.repository.BearRepository
import com.buildabear.tracker.domain.model.BearWithStatus
import com.buildabear.tracker.domain.model.CollectionStatusType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BearDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bearRepository: BearRepository,
) : ViewModel() {
    private val bearId: String = checkNotNull(savedStateHandle["bearId"])

    private val _uiState = MutableStateFlow<BearWithStatus?>(null)
    val uiState: StateFlow<BearWithStatus?> = _uiState.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val bear = bearRepository.getBearWithStatus(bearId)
            _uiState.value = bear
            _notes.value = bear?.notes.orEmpty()
        }
    }

    fun updateStatus(status: CollectionStatusType) {
        viewModelScope.launch {
            bearRepository.updateStatus(bearId, status, _notes.value.takeIf { it.isNotBlank() })
            load()
        }
    }

    fun updateNotes(notes: String) {
        _notes.value = notes
    }

    fun saveNotes() {
        viewModelScope.launch {
            val current = _uiState.value ?: return@launch
            bearRepository.updateStatus(bearId, current.status, _notes.value.takeIf { it.isNotBlank() })
        }
    }

    fun deleteCustom(onDeleted: () -> Unit) {
        viewModelScope.launch {
            bearRepository.deleteCustomBear(bearId)
            onDeleted()
        }
    }
}
