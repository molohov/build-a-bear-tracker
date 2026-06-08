package com.buildabear.tracker.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildabear.tracker.domain.model.BearWithStatus
import com.buildabear.tracker.domain.model.SourceType
import com.buildabear.tracker.ui.components.BearImage
import com.buildabear.tracker.ui.components.SourceBadge
import com.buildabear.tracker.ui.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BearListScreen(
    onBearClick: (String) -> Unit,
    onAddCustom: () -> Unit,
    onFiltersClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: BearListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshingState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Build-A-Bears") },
                actions = {
                    IconButton(onClick = onFiltersClick) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCustom) {
                Icon(Icons.Default.Add, contentDescription = "Add custom bear")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            androidx.compose.material3.OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by name or SKU") },
                singleLine = true,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = uiState.selectedViewName == "All",
                    onClick = { viewModel.applyView("All", com.buildabear.tracker.domain.model.FilterCriteria()) },
                    label = { Text("All") },
                )
                FilterChip(
                    selected = uiState.activeCriteria.sourceType?.contains(SourceType.CUSTOM.name) == true,
                    onClick = {
                        viewModel.setSourceFilter(
                            if (uiState.activeCriteria.sourceType?.contains(SourceType.CUSTOM.name) == true) null
                            else SourceType.CUSTOM,
                        )
                    },
                    label = { Text("Custom") },
                )
                viewModel.defaultViews.drop(1).take(3).forEach { (name, criteria) ->
                    FilterChip(
                        selected = uiState.selectedViewName == name,
                        onClick = { viewModel.applyView(name, criteria) },
                        label = { Text(name) },
                    )
                }
            }

            Text(
                text = "View: ${uiState.selectedViewName} (${uiState.bears.size})",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
            )

            if (uiState.isLoading) {
                BoxLoading()
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = viewModel::refreshCatalog,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.bears, key = { it.bear.id }) { bearWithStatus ->
                            BearListItem(
                                bearWithStatus = bearWithStatus,
                                onClick = { onBearClick(bearWithStatus.bear.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxLoading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BearListItem(
    bearWithStatus: BearWithStatus,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BearImage(
                bear = bearWithStatus.bear,
                modifier = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bearWithStatus.bear.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                bearWithStatus.bear.yearReleased?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusChip(bearWithStatus.status)
                    SourceBadge(bearWithStatus.bear.sourceType == SourceType.CUSTOM)
                }
            }
        }
    }
}
