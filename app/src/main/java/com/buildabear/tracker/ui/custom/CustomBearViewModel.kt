package com.buildabear.tracker.ui.custom

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildabear.tracker.data.repository.BearRepository
import com.buildabear.tracker.data.storage.CustomImageStore
import com.buildabear.tracker.domain.model.Bear
import com.buildabear.tracker.domain.model.SourceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class CustomBearFormState(
    val bearId: String? = null,
    val name: String = "",
    val yearReleased: String = "",
    val furColor: String = "",
    val eyeColor: String = "",
    val height: String = "",
    val weight: String = "",
    val sku: String = "",
    val price: String = "",
    val description: String = "",
    val categoriesText: String = "",
    val photoUri: Uri? = null,
    val existingImagePath: String? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CustomBearViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bearRepository: BearRepository,
    private val customImageStore: CustomImageStore,
) : ViewModel() {
    private val editBearId: String? = savedStateHandle.get<String>("bearId")?.takeIf { it.isNotBlank() }

    private val _formState = MutableStateFlow(CustomBearFormState())
    val formState: StateFlow<CustomBearFormState> = _formState.asStateFlow()

    init {
        editBearId?.let { loadForEdit(it) }
    }

    private fun loadForEdit(bearId: String) {
        viewModelScope.launch {
            val bearWithStatus = bearRepository.getBearWithStatus(bearId) ?: return@launch
            val bear = bearWithStatus.bear
            _formState.update {
                it.copy(
                    bearId = bear.id,
                    name = bear.name,
                    yearReleased = bear.yearReleased.orEmpty(),
                    furColor = bear.furColor.orEmpty(),
                    eyeColor = bear.eyeColor.orEmpty(),
                    height = bear.height.orEmpty(),
                    weight = bear.weight.orEmpty(),
                    sku = bear.sku.orEmpty(),
                    price = bear.price.orEmpty(),
                    description = bear.description.orEmpty(),
                    categoriesText = bear.categories.joinToString(", "),
                    existingImagePath = bear.localImagePath,
                )
            }
        }
    }

    fun updateName(v: String) = _formState.update { it.copy(name = v) }
    fun updateYear(v: String) = _formState.update { it.copy(yearReleased = v) }
    fun updateFurColor(v: String) = _formState.update { it.copy(furColor = v) }
    fun updateEyeColor(v: String) = _formState.update { it.copy(eyeColor = v) }
    fun updateHeight(v: String) = _formState.update { it.copy(height = v) }
    fun updateWeight(v: String) = _formState.update { it.copy(weight = v) }
    fun updateSku(v: String) = _formState.update { it.copy(sku = v) }
    fun updatePrice(v: String) = _formState.update { it.copy(price = v) }
    fun updateDescription(v: String) = _formState.update { it.copy(description = v) }
    fun updateCategories(v: String) = _formState.update { it.copy(categoriesText = v) }

    fun setPhotoUri(uri: Uri) = _formState.update { it.copy(photoUri = uri, error = null) }

    fun setPhotoFromFile(file: File) {
        _formState.update { it.copy(photoUri = Uri.fromFile(file), error = null) }
    }

    fun save(onSaved: (String) -> Unit) {
        val state = _formState.value
        if (state.name.isBlank()) {
            _formState.update { it.copy(error = "Name is required") }
            return
        }
        if (state.photoUri == null && state.existingImagePath == null) {
            _formState.update { it.copy(error = "Photo is required") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, error = null) }
            try {
                val bearId = state.bearId ?: UUID.randomUUID().toString()
                var localPath = state.existingImagePath

                state.photoUri?.let { uri ->
                    if (state.existingImagePath != null) {
                        customImageStore.deleteImage(state.existingImagePath)
                    }
                    localPath = if (uri.scheme == "file") {
                        customImageStore.saveFromFile(bearId, File(uri.path!!))
                    } else {
                        customImageStore.saveFromUri(bearId, uri)
                    }
                }

                val categories = state.categoriesText.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                val bear = Bear(
                    id = bearId,
                    sourceType = SourceType.CUSTOM,
                    name = state.name.trim(),
                    description = state.description.takeIf { it.isNotBlank() },
                    yearReleased = state.yearReleased.takeIf { it.isNotBlank() },
                    furColor = state.furColor.takeIf { it.isNotBlank() },
                    eyeColor = state.eyeColor.takeIf { it.isNotBlank() },
                    height = state.height.takeIf { it.isNotBlank() },
                    weight = state.weight.takeIf { it.isNotBlank() },
                    sku = state.sku.takeIf { it.isNotBlank() },
                    price = state.price.takeIf { it.isNotBlank() },
                    localImagePath = localPath,
                )
                bearRepository.saveCustomBear(bear, categories)
                _formState.update { it.copy(isSaving = false) }
                onSaved(bearId)
            } catch (e: Exception) {
                _formState.update { it.copy(isSaving = false, error = e.message ?: "Save failed") }
            }
        }
    }
}
