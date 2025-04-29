package com.example.myapplicationf.features.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.myapplicationf.ui.theme.HeaderBackground
import com.example.myapplicationf.ui.theme.HeaderIcon
import com.example.myapplicationf.ui.theme.HeaderText
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplicationf.features.marketplace.models.ListedCrop
import com.example.myapplicationf.features.marketplace.models.PurchaseRequest
import com.example.myapplicationf.features.marketplace.models.BuyerDetail
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import java.io.File
import com.example.myapplicationf.auth.AuthHelper
import java.text.SimpleDateFormat
import java.util.*

private val DarkGreen = Color(0xFF084521)
private val LightGreen = Color(0xFF4CAF50)
private val BackgroundGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboardScreen(
    onBackPressed: () -> Unit,
    onNavigateToCropDetails: (String) -> Unit,
    viewModel: MarketplaceViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val listedCrops by viewModel.myListedCrops.collectAsState()
    val purchaseRequests by viewModel.purchaseRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Dashboard", color = HeaderText) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HeaderIcon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = DarkGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Farmer") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Buy") }
                )
            }

            // Error message
            error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Content
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { viewModel.refresh() }
            ) {
                when (selectedTab) {
                    0 -> FarmerTab(
                        listedCrops = listedCrops,
                        isLoading = isLoading,
                        viewModel = viewModel
                    )
                    1 -> BuyerTab(
                        purchaseRequests = purchaseRequests,
                        isLoading = isLoading,
                        viewModel = viewModel,
                        onNavigateToCropDetails = onNavigateToCropDetails
                    )
                }
            }
        }
    }
}

@Composable
private fun FarmerTab(
    listedCrops: List<ListedCrop>,
    isLoading: Boolean,
    viewModel: MarketplaceViewModel
) {
    val sellerRequests by viewModel.sellerPurchaseRequests.collectAsState()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (listedCrops.isEmpty() && sellerRequests.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No crops or purchase requests yet",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            if (listedCrops.isNotEmpty()) {
                item {
                    Text(
                        text = "My Listed Crops",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(listedCrops) { crop ->
                    ListedCropItem(crop = crop, viewModel = viewModel)
        }
            }

            if (sellerRequests.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
            Text(
                        text = "Purchase Requests",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Group requests by crop name
                val groupedRequests = sellerRequests.groupBy { it.cropName }
                items(groupedRequests.keys.toList()) { cropName ->
                    val requests = groupedRequests[cropName] ?: emptyList()
                    GroupedRequestsItem(
                        cropName = cropName,
                        requests = requests,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupedRequestsItem(
    cropName: String,
    requests: List<PurchaseRequest>,
    viewModel: MarketplaceViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    // Get the food icon path based on crop name
    val iconFileName = "$cropName icon.jpg"
    // Get category from the first request's crop
    val category = viewModel.myListedCrops.collectAsState().value
        .find { it.id == requests.firstOrNull()?.cropId }?.category?.displayName ?: "GRAINS"
    
    // Calculate accepted requests
    val acceptedCount = requests.count { it.status == "accepted" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular food icon with border
                    Surface(
                        modifier = Modifier
                            .size(50.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = "file:///android_asset/food_img/food Icon/$iconFileName"
                            ),
                            contentDescription = "$cropName icon",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = cropName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${requests.size} Request${if (requests.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                            if (acceptedCount > 0) {
                                Text(
                                    text = "$acceptedCount Accepted",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                    Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                    )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                requests.forEach { request ->
                    RequestDetailItem(
                        request = request,
                        viewModel = viewModel
                    )
                    if (request != requests.last()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestDetailItem(
    request: PurchaseRequest,
    viewModel: MarketplaceViewModel
) {
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var selectedQuantity by remember { mutableStateOf(request.requestedQuantity) }
    var cancelReason by remember { mutableStateOf("") }

    // Main request item (basic info)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetailsDialog = true }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Buyer: ${request.buyerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Requested: ${request.requestedQuantity} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Status: ${request.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (request.status) {
                        "accepted" -> MaterialTheme.colorScheme.primary
                        "rejected" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Detailed popup dialog
    if (showDetailsDialog) {
        Dialog(onDismissRequest = { showDetailsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header
                    Text(
                        text = "Buyer Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Buyer Information
                    BuyerDetailRow("Name", request.buyerName)
                    BuyerDetailRow("Contact", request.buyerContact)
                    BuyerDetailRow("Email", request.buyerEmail)
                    BuyerDetailRow("Address", request.deliveryAddress)
                    BuyerDetailRow("Requested Quantity", "${request.requestedQuantity} kg")
                    BuyerDetailRow("Status", request.status.capitalize())
                    if (request.status == "accepted") {
                        BuyerDetailRow("Accepted Quantity", "${request.acceptedQuantity} kg")
                        BuyerDetailRow("Total Amount", "₹${request.totalAmount}")
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    if (request.status == "pending") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { 
                                    showAcceptDialog = true
                                    showDetailsDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Accept Request")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { 
                                    showCancelDialog = true
                                    showDetailsDialog = false
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel Request")
                            }
                        }
                    } else {
                        Button(
                            onClick = { showDetailsDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    // Accept Dialog with Slider
    if (showAcceptDialog) {
        Dialog(onDismissRequest = { showAcceptDialog = false }) {
            Card(
                    modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                            shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Accept Purchase Request",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Buyer: ${request.buyerName}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Requested: ${request.requestedQuantity} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Select Quantity to Accept",
                        style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "0")
                        Slider(
                            value = selectedQuantity.toFloat(),
                            onValueChange = { selectedQuantity = it.toInt() },
                            valueRange = 0f..request.requestedQuantity.toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Text(text = "${request.requestedQuantity}")
                    }
                    Text(
                        text = "Selected: $selectedQuantity kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAcceptDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.acceptBuyerRequest(request.id, selectedQuantity)
                                showAcceptDialog = false
                            },
                            enabled = selectedQuantity > 0
                        ) {
                            Text("Accept")
                        }
                    }
                }
            }
        }
    }

    // Cancel Dialog with Suggestions
    if (showCancelDialog) {
        Dialog(onDismissRequest = { showCancelDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Cancel Request",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Select a reason or write your own:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Predefined reasons
                    val reasons = listOf(
                        "Insufficient quantity available",
                        "Price negotiation failed",
                        "Quality requirements not met",
                        "Delivery location not serviceable",
                        "Other"
                    )
                    
                    reasons.forEach { reason ->
                        OutlinedButton(
                            onClick = { cancelReason = reason },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (cancelReason == reason) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(reason)
                        }
                    }
                    
                    if (cancelReason == "Other") {
                        OutlinedTextField(
                            value = if (cancelReason == "Other") "" else cancelReason,
                            onValueChange = { cancelReason = it },
                            label = { Text("Enter reason") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCancelDialog = false }) {
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.cancelPurchaseRequest(request.id, selectedQuantity, cancelReason)
                                showCancelDialog = false
                            },
                            enabled = cancelReason.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Confirm Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuyerDetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        Text(
            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                            Text(
            text = value,
                                style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ListedCropItem(
    crop: ListedCrop,
    viewModel: MarketplaceViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Get the food icon path based on crop name
    val iconFileName = "${crop.name} icon.jpg"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular food icon with border
                    Surface(
                        modifier = Modifier
                            .size(50.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = "file:///android_asset/food_img/food Icon/$iconFileName"
                            ),
                            contentDescription = "${crop.name} icon",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                            Text(
                            text = crop.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                            text = crop.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Quantity: ${crop.quantity} kg",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            Text(
                                text = "₹${crop.rate}/kg",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                StatusTag(
                    status = crop.status,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (crop.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = crop.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                        Text(
                        text = crop.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                }
            }
        }
    }
}

@Composable
private fun BuyerTab(
    purchaseRequests: List<PurchaseRequest>,
    isLoading: Boolean,
    viewModel: MarketplaceViewModel,
    onNavigateToCropDetails: (String) -> Unit
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (purchaseRequests.isEmpty()) {
            item {
                        Text(
                    text = "No purchase requests yet",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // Group requests by crop name
            val groupedRequests = purchaseRequests.groupBy { it.cropName }
            
            items(groupedRequests.keys.toList()) { cropName ->
                val requests = groupedRequests[cropName] ?: emptyList()
                ConsolidatedCropRequestCard(
                    cropName = cropName ?: "Unknown Crop",
                    requests = requests,
                    viewModel = viewModel,
                    onNavigateToCropDetails = onNavigateToCropDetails
                )
            }
        }
    }
}

@Composable
private fun ConsolidatedCropRequestCard(
    cropName: String,
    requests: List<PurchaseRequest>,
    viewModel: MarketplaceViewModel,
    onNavigateToCropDetails: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var selectedRequest by remember { mutableStateOf<PurchaseRequest?>(null) }
    var cancelReason by remember { mutableStateOf("") }
    var selectedQuantity by remember { mutableStateOf(1) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cropName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Show less" else "Show more"
                    )
                }
            }

            // Summary
            val totalQuantity = requests.sumOf { it.requestedQuantity }
            val acceptedQuantity = requests.sumOf { it.acceptedQuantity }
            
            Text(
                text = "Total Requested: $totalQuantity kg",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Total Accepted: $acceptedQuantity kg",
                style = MaterialTheme.typography.bodyMedium
            )

            // Expanded content
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                requests.forEach { request ->
                    RequestItem(
                        request = request,
                        onCancelClick = {
                            selectedRequest = request
                            selectedQuantity = request.requestedQuantity - request.acceptedQuantity
                            showCancelDialog = true
                        },
                        viewModel = viewModel,
                        onNavigateToCropDetails = onNavigateToCropDetails
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Cancel Dialog
    if (showCancelDialog && selectedRequest != null) {
        AlertDialog(
            onDismissRequest = { 
                showCancelDialog = false
                selectedRequest = null
                cancelReason = ""
            },
            title = { Text("Cancel Request") },
            text = {
                Column {
                    Text("Are you sure you want to cancel this request?")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Quantity to cancel:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = selectedQuantity.toFloat(),
                        onValueChange = { selectedQuantity = it.toInt() },
                        valueRange = 1f..(selectedRequest!!.requestedQuantity - selectedRequest!!.acceptedQuantity).toFloat(),
                        steps = (selectedRequest!!.requestedQuantity - selectedRequest!!.acceptedQuantity) - 1
                    )
                    Text("Selected: $selectedQuantity kg")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason for cancellation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedRequest?.let { request ->
                        viewModel.cancelPurchaseRequest(request.id, selectedQuantity, cancelReason)
                        }
                        showCancelDialog = false
                        selectedRequest = null
                        cancelReason = ""
                    },
                    enabled = cancelReason.isNotBlank()
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCancelDialog = false
                    selectedRequest = null
                    cancelReason = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RequestItem(
    request: PurchaseRequest,
    onCancelClick: () -> Unit,
    viewModel: MarketplaceViewModel,
    onNavigateToCropDetails: (String) -> Unit
) {
    var showDetailDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDetailDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
            Text(
                        text = "Seller: ${request.sellerName}",
                        style = MaterialTheme.typography.bodyMedium
            )
            Text(
                        text = "Requested: ${request.requestedQuantity} kg",
                style = MaterialTheme.typography.bodyMedium
            )
                    if (request.acceptedQuantity > 0) {
            Text(
                            text = "Accepted: ${request.acceptedQuantity} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
            )
                    }
                    StatusTag(
                        status = request.status,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                Text(
                        text = "Total Amount: ₹${request.totalAmount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                }

                // Only show cancel button for pending requests
                if (request.status.lowercase() == "pending" && 
                    request.requestedQuantity > request.acceptedQuantity) {
                    TextButton(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    if (showDetailDialog) {
        DetailedRequestDialog(
            request = request,
            onDismiss = { showDetailDialog = false },
            onBuyAgain = { cropId -> 
                onNavigateToCropDetails(cropId)
            }
        )
    }
}

@Composable
private fun DetailedRequestDialog(
    request: PurchaseRequest,
    onDismiss: () -> Unit,
    onBuyAgain: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = request.cropName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Seller Information Section
                Text(
                    text = "Seller Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                DetailRow("Name", request.sellerName)
                DetailRow("Email", request.sellerEmail)
                
                Spacer(modifier = Modifier.height(16.dp))

                // Buyer Details Section
                Text(
                    text = "Buyer Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                DetailRow("Name", request.buyerName)
                DetailRow("Contact", request.buyerContact)
                DetailRow("Email", request.buyerEmail)
                DetailRow("Delivery Address", request.deliveryAddress)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Request Details Section
                Text(
                    text = "Request Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                DetailRow("Requested Quantity", "${request.requestedQuantity} kg")
                DetailRow("Accepted Quantity", "${request.acceptedQuantity} kg")
                DetailRow("Total Amount", "₹${request.totalAmount}")
                DetailRow("Status", request.status.capitalize())
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Dates Section
                Text(
                    text = "Dates",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                
                DetailRow(
                    "Request Date", 
                    request.createdAt?.toDate()?.let { date ->
                        dateFormatter.format(date)
                    } ?: "Not available"
                )
                
                if (request.status.lowercase() == "accepted") {
                    DetailRow(
                        "Accepted Date", 
                        request.updatedAt?.toDate()?.let { date ->
                            dateFormatter.format(date)
                        } ?: "Not available"
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                ) {
                        Text("Close")
                }
                Button(
                        onClick = { 
                            onBuyAgain(request.cropId)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = request.cropId.isNotEmpty()
                ) {
                        Text("Buy Again")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CropItem(
    crop: ListedCrop,
    onClick: () -> Unit,
    onStatusUpdate: (String) -> Unit,
    viewModel: MarketplaceViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedBuyer by remember { mutableStateOf<BuyerDetail?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = crop.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quantity: ${crop.quantity} kg",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Rate: ₹${crop.rate}/kg",
                style = MaterialTheme.typography.bodyMedium
            )
            StatusTag(
                status = crop.status,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (crop.buyerDetails.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Buyer Requests:",
                    style = MaterialTheme.typography.titleMedium
                )
                crop.buyerDetails.forEach { buyer ->
                    BuyerRequestItem(
                        buyer = buyer,
                        onAccept = {
                            selectedBuyer = buyer
                            showDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDialog && selectedBuyer != null) {
        AcceptRequestDialog(
            buyer = selectedBuyer!!,
            onDismiss = {
                showDialog = false
                selectedBuyer = null
            },
            onAccept = { quantity ->
                // Find the matching purchase request
                val matchingRequest = viewModel.purchaseRequests.value.firstOrNull { request ->
                    request.cropId == crop.id && 
                    request.buyerName == selectedBuyer!!.name &&
                    request.status == "pending"
                }
                matchingRequest?.let { request ->
                    viewModel.acceptBuyerRequest(request.id, quantity)
                }
                showDialog = false
                selectedBuyer = null
            }
        )
    }
}

@Composable
private fun BuyerRequestItem(
    buyer: BuyerDetail,
    onAccept: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = buyer.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Requested: ${buyer.requestedQuantity} kg",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Button(
            onClick = onAccept,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Accept")
        }
    }
}

@Composable
private fun AcceptRequestDialog(
    buyer: BuyerDetail,
    onDismiss: () -> Unit,
    onAccept: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(buyer.requestedQuantity.toFloat()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Accept Request from ${buyer.name}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Requested Quantity: ${buyer.requestedQuantity} kg",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Adjust Quantity: ${quantity.toInt()} kg",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = quantity,
                    onValueChange = { quantity = it },
                    valueRange = 1f..buyer.requestedQuantity.toFloat(),
                    steps = buyer.requestedQuantity - 2
                )
                Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = { onAccept(quantity.toInt()) }) {
                    Text("Accept")
                }
                }
            }
        }
    }
}