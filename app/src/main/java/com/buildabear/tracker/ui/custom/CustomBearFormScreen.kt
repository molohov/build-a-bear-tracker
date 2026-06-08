package com.buildabear.tracker.ui.custom

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.buildabear.tracker.ui.components.BearImage
import com.buildabear.tracker.domain.model.Bear
import com.buildabear.tracker.domain.model.SourceType
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBearFormScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: CustomBearViewModel = hiltViewModel(),
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let { viewModel.setPhotoUri(it) } }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) cameraUri?.let { viewModel.setPhotoUri(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val file = viewModel.formState.value.let {
                java.io.File(context.cacheDir, "camera/capture_${System.currentTimeMillis()}.jpg").also { f ->
                    f.parentFile?.mkdirs()
                }
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (formState.bearId == null) "Add custom bear" else "Edit custom bear") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                formState.photoUri != null -> {
                    AsyncImage(
                        model = formState.photoUri,
                        contentDescription = "Selected photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
                formState.existingImagePath != null -> {
                    BearImage(
                        bear = Bear(
                            id = formState.bearId.orEmpty(),
                            sourceType = SourceType.CUSTOM,
                            name = formState.name,
                            localImagePath = formState.existingImagePath,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    )
                }
                else -> {
                    Text("No photo selected", style = MaterialTheme.typography.bodyMedium)
                }
            }

            OutlinedButton(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Take photo") }

            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Choose from gallery") }

            OutlinedTextField(
                value = formState.name,
                onValueChange = viewModel::updateName,
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(value = formState.yearReleased, onValueChange = viewModel::updateYear, label = { Text("Year released") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = formState.furColor, onValueChange = viewModel::updateFurColor, label = { Text("Fur color") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = formState.eyeColor, onValueChange = viewModel::updateEyeColor, label = { Text("Eye color") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = formState.height, onValueChange = viewModel::updateHeight, label = { Text("Height") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = formState.weight, onValueChange = viewModel::updateWeight, label = { Text("Weight") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = formState.sku, onValueChange = viewModel::updateSku, label = { Text("SKU") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = formState.price, onValueChange = viewModel::updatePrice, label = { Text("Price") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = formState.description, onValueChange = viewModel::updateDescription, label = { Text("Description / notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            OutlinedTextField(value = formState.categoriesText, onValueChange = viewModel::updateCategories, label = { Text("Tags (comma-separated)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            formState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = { viewModel.save(onSaved) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !formState.isSaving,
            ) {
                Text(if (formState.isSaving) "Saving..." else "Save")
            }
        }
    }
}
