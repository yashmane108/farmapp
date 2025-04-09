package com.example.myapplicationf.features.protection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Coronavirus
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Agriculture

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropProtectionScreen(
    onBackPressed: () -> Unit,
    viewModel: CropProtectionViewModel = viewModel()
) {
    val protectionState by viewModel.protectionState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Protection") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                SearchBar(
                    query = protectionState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    onSearch = viewModel::searchCrop,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (protectionState.searchResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(protectionState.searchResults) { cropInfo ->
                    CropProtectionInfoCard(cropInfo)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else if (protectionState.searchQuery.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "No Results",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No results found",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Try searching for crops like: rice, wheat, tomato, potato, or cotton",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Common Protection Tips",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(protectionState.commonTips) { tip ->
                CommonTipCard(tip)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search for crop protection info...") },
        trailingIcon = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Outlined.Search, contentDescription = "Search")
            }
        },
        singleLine = true
    )
}

@Composable
fun CropProtectionInfoCard(cropInfo: CropProtectionInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = cropInfo.cropName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Common Issues:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            cropInfo.commonIssues.forEach { issue ->
                Text("• $issue")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Prevention Methods:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            cropInfo.preventionMethods.forEach { method ->
                Text("• $method")
            }
            
            if (cropInfo.treatment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Treatment:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(cropInfo.treatment)
            }
        }
    }
}

@Composable
fun CommonTipCard(tip: ProtectionTip) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (tip.category) {
                    "Pest" -> Icons.Outlined.BugReport
                    "Disease" -> Icons.Outlined.Coronavirus
                    "Soil" -> Icons.Outlined.Landscape
                    else -> Icons.Outlined.Agriculture
                },
                contentDescription = tip.category,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
