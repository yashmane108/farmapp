package com.example.myapplicationf.features.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplicationf.features.marketplace.models.ListedCrop
import com.example.myapplicationf.features.marketplace.models.BuyerDetail
import com.example.myapplicationf.features.marketplace.models.Category
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import java.util.UUID
import com.example.myapplicationf.ui.theme.HeaderBackground
import com.example.myapplicationf.ui.theme.HeaderText
import com.example.myapplicationf.ui.theme.HeaderIcon
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

private val DarkGreen = Color(0xFF084521)
private val LightGreen = Color(0xFF4CAF50)
private val BackgroundGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onBackPressed: () -> Unit,
    onNavigateToSell: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onCropSelected: (String) -> Unit,
    viewModel: MarketplaceViewModel = viewModel()
) {
    val listedCrops by viewModel.listedCrops.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    // Filter crops based on selected category and search query
    val filteredCrops = remember(listedCrops, selectedFilter, searchQuery) {
        listedCrops.filter { crop ->
            val matchesCategory = when (selectedFilter) {
                "All" -> true
                "Vegetables" -> crop.category == Category.VEGETABLES
                "Grains" -> crop.category == Category.GRAINS
                "Fruits" -> crop.category == Category.FRUITS
                "Oil seeds" -> crop.category == Category.OILSEEDS
                else -> true
            }
            
            val matchesSearch = if (searchQuery.isNotEmpty()) {
                crop.name.contains(searchQuery, ignoreCase = true) ||
                crop.location.contains(searchQuery, ignoreCase = true)
            } else {
                true
            }
            
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market.Place", color = HeaderText) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HeaderIcon)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSell) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Sell", tint = HeaderIcon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSell,
                containerColor = LightGreen
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Crop", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Dashboard Button and Sell Food Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onNavigateToDashboard,
                    colors = ButtonDefaults.buttonColors(containerColor = BackgroundGray),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("My Dashboard", color = Color.Black)
                }
                Button(
                    onClick = onNavigateToSell,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Sell Food", color = Color.White)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip("All", selectedFilter) { selectedFilter = "All" }
                FilterChip("Vegetables", selectedFilter) { selectedFilter = "Vegetables" }
                FilterChip("Grains", selectedFilter) { selectedFilter = "Grains" }
                FilterChip("Oil seeds", selectedFilter) { selectedFilter = "Oil seeds" }
                FilterChip("Fruits", selectedFilter) { selectedFilter = "Fruits" }
            }

            // Crop Listings with SwipeRefresh
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredCrops) { crop ->
                        CropListingCard(
                            crop = crop,
                            onClick = { onCropSelected(crop.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropListingCard(
    crop: ListedCrop,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = crop.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (crop.isOwnListing) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8F5E9),
                                border = BorderStroke(1.dp, Color(0xFF81C784))
                            ) {
                                Text(
                                    text = "Own",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Location: ${crop.location}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rate: ₹${crop.rate}/kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Qty: ${crop.quantity} kg",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    selectedFilter: String,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        color = if (selectedFilter == text) LightGreen else Color.White,
        border = BorderStroke(1.dp, if (selectedFilter == text) LightGreen else Color.LightGray)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (selectedFilter == text) Color.White else Color.Black
        )
    }
}

@Composable
private fun ListedCropItem(
    crop: ListedCrop,
    onCropClick: (ListedCrop) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onCropClick(crop) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = crop.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quantity: ${crop.quantity}kg",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Rate: ₹${crop.rate}/kg",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Location: ${crop.location}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ScrollableRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Start
    ) {
        content()
    }
}

