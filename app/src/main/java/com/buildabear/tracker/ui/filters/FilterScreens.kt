package com.buildabear.tracker.ui.filters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildabear.tracker.domain.model.CollectionStatusType
import com.buildabear.tracker.domain.model.SourceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedFiltersScreen(
    onBack: () -> Unit,
    onCreateFilter: () -> Unit,
    onEditFilter: (String) -> Unit,
    onApplyFilter: (String, com.buildabear.tracker.domain.model.FilterCriteria) -> Unit,
    viewModel: FilterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved views") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text("Default views", style = MaterialTheme.typography.titleMedium)
            }
            items(uiState.defaultViews) { (name, criteria) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onApplyFilter(name, criteria) },
                ) {
                    Text(name, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }
            item {
                Text("Your saved views", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }
            items(uiState.savedFilters, key = { it.id }) { filter ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(filter.name, style = MaterialTheme.typography.bodyLarge)
                        androidx.compose.foundation.layout.Row {
                            TextButtonLink("Apply") { onApplyFilter(filter.name, filter.criteria) }
                            TextButtonLink("Edit") { onEditFilter(filter.id) }
                            IconButton(onClick = { viewModel.deleteFilter(filter.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
            item {
                Button(onClick = onCreateFilter, modifier = Modifier.fillMaxWidth()) {
                    Text("Create new view")
                }
            }
        }
    }
}

@Composable
private fun TextButtonLink(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBuilderScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: FilterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val builder = uiState.builder

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (builder.filterId == null) "New view" else "Edit view") },
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
            OutlinedTextField(
                value = builder.name,
                onValueChange = viewModel::updateName,
                label = { Text("View name") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = builder.yearContains,
                onValueChange = viewModel::updateYear,
                label = { Text("Year contains") },
                modifier = Modifier.fillMaxWidth(),
            )

            ChipSection("Status") {
                listOf(
                    CollectionStatusType.OWNED.name to "Owned",
                    CollectionStatusType.WANT.name to "Want",
                    CollectionStatusType.DONT_WANT.name to "Don't want",
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = value in builder.status,
                        onClick = { viewModel.toggleStatus(value) },
                        label = { Text(label) },
                    )
                }
            }

            ChipSection("Source") {
                listOf(SourceType.CATALOG.name to "Catalog", SourceType.CUSTOM.name to "Custom").forEach { (value, label) ->
                    FilterChip(
                        selected = value in builder.sourceType,
                        onClick = { viewModel.toggleSourceType(value) },
                        label = { Text(label) },
                    )
                }
            }

            ChipSection("Fur color") {
                uiState.furColorOptions.forEach { color ->
                    FilterChip(
                        selected = color in builder.furColors,
                        onClick = { viewModel.toggleFurColor(color) },
                        label = { Text(color) },
                    )
                }
            }

            ChipSection("Eye color") {
                uiState.eyeColorOptions.forEach { color ->
                    FilterChip(
                        selected = color in builder.eyeColors,
                        onClick = { viewModel.toggleEyeColor(color) },
                        label = { Text(color) },
                    )
                }
            }

            ChipSection("Categories") {
                uiState.categoryOptions.forEach { cat ->
                    FilterChip(
                        selected = cat in builder.categories,
                        onClick = { viewModel.toggleCategory(cat) },
                        label = { Text(cat) },
                    )
                }
            }

            ChipSection("Available") {
                FilterChip(selected = builder.available == true, onClick = { viewModel.setAvailable(true) }, label = { Text("Yes") })
                FilterChip(selected = builder.available == false, onClick = { viewModel.setAvailable(false) }, label = { Text("No") })
                FilterChip(selected = builder.available == null, onClick = { viewModel.setAvailable(null) }, label = { Text("Any") })
            }

            Button(
                onClick = { viewModel.saveFilter(onSaved) },
                modifier = Modifier.fillMaxWidth(),
                enabled = builder.name.isNotBlank(),
            ) {
                Text("Save view")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSection(title: String, content: @Composable () -> Unit) {
    Text(title, style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        content()
    }
}
