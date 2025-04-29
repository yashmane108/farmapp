package com.example.myapplicationf.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplicationf.features.marketplace.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSignOut: () -> Unit,
    viewModel: MarketplaceViewModel
) {
    var showMigrationDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text("Sign Out")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome to Dashboard!")
            
            // Migration button
            Button(
                onClick = { showMigrationDialog = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Update Database Structure")
            }
        }
    }
    
    // Confirmation dialog
    if (showMigrationDialog) {
        AlertDialog(
            onDismissRequest = { showMigrationDialog = false },
            title = { Text("Update Database") },
            text = { Text("This will update the database structure. Continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.migrateData()
                        showMigrationDialog = false
                    }
                ) {
                    Text("Yes, Update")
                }
            },
            dismissButton = {
                Button(onClick = { showMigrationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 