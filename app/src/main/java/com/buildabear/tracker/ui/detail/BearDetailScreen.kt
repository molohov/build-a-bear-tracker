package com.buildabear.tracker.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildabear.tracker.domain.model.CollectionStatusType
import com.buildabear.tracker.domain.model.SourceType
import com.buildabear.tracker.ui.components.BearImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BearDetailScreen(
    onBack: () -> Unit,
    onEditCustom: (String) -> Unit,
    viewModel: BearDetailViewModel = hiltViewModel(),
) {
    val bearWithStatus by viewModel.uiState.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (bearWithStatus == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bear Details") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
        ) { padding ->
            Text("Loading...", modifier = Modifier.padding(padding).padding(16.dp))
        }
        return
    }

    val bear = bearWithStatus!!.bear
    val isCustom = bear.sourceType == SourceType.CUSTOM

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete custom bear?") },
            text = { Text("This will permanently remove ${bear.name} and its photo.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCustom(onBack)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bear.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isCustom) {
                        IconButton(onClick = { onEditCustom(bear.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
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
            BearImage(
                bear = bear,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Fit,
            )

            Text("Collection status", style = MaterialTheme.typography.titleSmall)
            StatusSelector(
                current = bearWithStatus!!.status,
                onSelect = viewModel::updateStatus,
            )

            OutlinedTextField(
                value = notes,
                onValueChange = viewModel::updateNotes,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 2,
            )
            OutlinedButton(onClick = { viewModel.saveNotes() }) {
                Text("Save notes")
            }

            MetadataSection("Release", listOfNotNull(bear.yearReleased?.let { "Year: $it" }))
            MetadataSection(
                "Appearance",
                listOfNotNull(
                    bear.furColor?.let { "Fur: $it" },
                    bear.eyeColor?.let { "Eyes: $it" },
                    bear.height?.let { "Height: $it" },
                    bear.weight?.let { "Weight: $it" },
                ),
            )
            MetadataSection(
                "Identifiers",
                listOfNotNull(bear.sku?.let { "SKU: $it" }, bear.price?.let { "Price: $it" }),
            )
            bear.available?.let {
                MetadataSection("Availability", listOf(if (it) "Available" else "Retired / Unavailable"))
            }
            bear.description?.let {
                MetadataSection("Description", listOf(it))
            }

            if (bear.categories.isNotEmpty()) {
                Text("Categories", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bear.categories.forEach { cat ->
                        AssistChip(onClick = {}, label = { Text(cat) })
                    }
                }
            }

            if (!isCustom && bear.sourceUrl != null) {
                Button(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(bear.sourceUrl)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("View on wiki")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusSelector(
    current: CollectionStatusType,
    onSelect: (CollectionStatusType) -> Unit,
) {
    val options = listOf(
        CollectionStatusType.OWNED to "Owned",
        CollectionStatusType.WANT to "Want",
        CollectionStatusType.DONT_WANT to "Don't want",
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (status, label) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                onClick = { onSelect(status) },
                selected = current == status,
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun MetadataSection(title: String, items: List<String>) {
    if (items.isEmpty()) return
    Text(title, style = MaterialTheme.typography.titleSmall)
    items.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
}
