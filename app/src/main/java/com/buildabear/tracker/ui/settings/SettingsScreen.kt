package com.buildabear.tracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Catalog", style = MaterialTheme.typography.titleMedium)
            Text(uiState.lastSyncText, style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = viewModel::syncCatalog,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSyncing,
            ) {
                if (uiState.isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text("Sync catalog from wiki")
            }

            Text("Backup", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(
                onClick = viewModel::exportCollection,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isExporting,
            ) {
                Text(if (uiState.isExporting) "Exporting..." else "Export my collection (ZIP)")
            }

            Text("Attribution", style = MaterialTheme.typography.titleMedium)
            Text(
                "Catalog data sourced from the Build-a-Bear Workshop Wiki on Fandom (CC-BY-SA). " +
                    "This app is not affiliated with Build-A-Bear Workshop.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
