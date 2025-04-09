package com.example.myapplicationf.features.marketplace
// first changes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch as coroutineLaunch
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Box

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onBackPressed: () -> Unit,
    viewModel: MarketplaceViewModel = viewModel()
) {
    var showSellScreen by remember { mutableStateOf(false) }
    var showDashboard by remember { mutableStateOf(false) }
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    if (userRole == UserRole.FARMER) {
        if (showDashboard) {
            FarmerDashboard(
                viewModel = viewModel,
                onBackPressed = { showDashboard = false }
            )
        } else if (showSellScreen) {
        SellProductScreen(
                product = selectedProduct,
            viewModel = viewModel,
            onBackPressed = { showSellScreen = false }
        )
        } else {
        SellerDashboard(
            viewModel = viewModel,
                onBackPressed = onBackPressed,
                onProductSelected = { showSellScreen = true },
                onDashboardClick = { showDashboard = true }
        )
        }
    } else if (userRole == null) {
        RoleSelectionScreen(viewModel)
    } else {
        BuyerDashboard()
    }
}

@Composable
fun RoleSelectionScreen(viewModel: MarketplaceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select your role",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = { viewModel.setUserRole(UserRole.FARMER) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("I am a Farmer")
        }
        
        Button(
            onClick = { viewModel.setUserRole(UserRole.BUYER) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I am a Buyer")
        }
    }
}

@Composable
fun BuyerDashboard() {
    Text("Buyer Dashboard - Coming Soon")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboard(
    viewModel: MarketplaceViewModel,
    onBackPressed: () -> Unit,
    onProductSelected: () -> Unit,
    onDashboardClick: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market.Place", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF084521)
                )
            )
        }
    ) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
                .padding(padding)
        ) {
            // My Dashboard Button
            Surface(
                onClick = onDashboardClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFFE8E8E8),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go to dashboard",
                        tint = Color.Black
                    )
                }
            }

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()g
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
                shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
            )
        )

            // Category Chips
        LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                items(listOf("All", "Vegetables", "Crop", "Oil seeds")) { category ->
                FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.setSelectedCategory(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4CAF50),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color.Black
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.LightGray,
                            selectedBorderColor = Color(0xFF4CAF50)
                    )
                )
            }
        }
        
            // Food Icons Grid
        FoodIconsGrid(
            viewModel = viewModel,
                onProductSelected = { product -> 
                    viewModel.setSelectedProduct(product)
                    viewModel.fetchMarketRate(product.name)
                onProductSelected()
            }
        )
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF1B5E20)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF388E3C)
        )
        }
    }
}

@Composable
fun FoodIconsGrid(
    viewModel: MarketplaceViewModel,
    onProductSelected: (Product) -> Unit
) {
    val context = LocalContext.current
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        items(viewModel.filterProducts(viewModel.getProducts())) { product ->
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
        ) {
                // Circular icon container
            Box(
                modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                        .clickable { 
                            onProductSelected(product)
                            viewModel.fetchMarketRate(product.name)
                        },
                contentAlignment = Alignment.Center
            ) {
                    var isError by remember { mutableStateOf(false) }
                    
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("file:///android_asset/${product.iconImagePath}")
                            .crossfade(true)
                            .build(),
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        onError = { 
                            isError = true
                            println("Failed to load icon: ${product.iconImagePath}")
                        }
                    )
                    
                    if (isError) {
            Text(
                            text = product.name.first().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
            )
                    }
                }
            
                // Product Name
            Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellProductScreen(
    product: Product?,
    viewModel: MarketplaceViewModel,
    onBackPressed: () -> Unit
) {
    var useCurrentRate by remember { mutableStateOf(true) }
    var customRate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedVariety by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var isDecrementPressed by remember { mutableStateOf(false) }
    var isIncrementPressed by remember { mutableStateOf(false) }

    // Get varieties from the selected product
    val varieties = remember(product) {
        if (product != null) {
            viewModel.getProductVarieties(product.name).map { it.name }
        } else {
            emptyList<String>()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market.Place", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF084521)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            Spacer(modifier = Modifier.height(16.dp))  // Add spacing after app bar
            
            // Product Image Section with Circular Icon Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(180.dp)
            ) {
                // Main image container with rounded corners
                    Box(
                modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                    ) {
                    // Background Image
                    if (product != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("file:///android_asset/${product.fullImagePath}")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Product Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                        
                    // Product Name Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                                .background(
                                Brush.verticalGradient(
                                        colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Transparent,
                                            Color.Black.copy(alpha = 0.4f)
                                        )
                                    )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product?.name ?: "",
                            style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        )
                    }
                }

                // Circular Icon Overlay (outside the rounded corner clip)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                                .offset(y = 40.dp)
                                .size(80.dp)
                            .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                        ) {
                            AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("file:///android_asset/${product?.iconImagePath}")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Product Icon",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                }
            }

            // Add spacing to account for the overlapped icon (increased to prevent cutoff)
            Spacer(modifier = Modifier.height(60.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Today's Rate Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = useCurrentRate,
                        onCheckedChange = { useCurrentRate = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF084521),
                            checkedTrackColor = Color(0xFF084521).copy(alpha = 0.5f)
                        )
                    )
                            Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Today's Rate:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                            Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rs. 100",
                        style = MaterialTheme.typography.bodyLarge
                    )
                            Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Trending Up",
                        tint = Color(0xFF084521),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Enter Your Rate Switch and TextField
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = !useCurrentRate,
                        onCheckedChange = { useCurrentRate = !it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF084521),
                            checkedTrackColor = Color(0xFF084521).copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                Text(
                            text = "Enter Your Rate:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(
                                Color(0xFFF5F5F5),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (customRate.isEmpty() && !useCurrentRate) {
                            Text(
                                text = "Enter here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                        BasicTextField(
                            value = customRate,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.all { char -> char.isDigit() }) {
                                    customRate = newValue
                                }
                            },
                            enabled = !useCurrentRate,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (!useCurrentRate) Color.Black else Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Product Variety Dropdown
                        Text(
                    text = "${product?.name ?: ""} Variety:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                    onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                        value = selectedVariety ?: "Select variety",
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            .height(48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            disabledContainerColor = Color(0xFFF5F5F5),
                            disabledBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                        if (varieties.isEmpty()) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = "No varieties available",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = { expanded = false }
                            )
                        } else {
                                    varieties.forEach { variety ->
                                        DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = variety,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                            onClick = {
                                        selectedVariety = variety
                                                expanded = false
                                            }
                                        )
                        }
                    }
                }
            }
            
                Spacer(modifier = Modifier.height(16.dp))
            
                    // Quantity Section
                Surface(
                            modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Quantity:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                                )
                            Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.decrementQuantity() },
                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFF084521), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease quantity",
                                        tint = Color.White
                                    )
                                }

                    Text(
                                    text = "${viewModel.quantity.collectAsState().value} Kg",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.widthIn(min = 80.dp),
                        textAlign = TextAlign.Center
                    )
                    
                                IconButton(
                                    onClick = { viewModel.incrementQuantity() },
                        modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFF084521), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase quantity",
                                        tint = Color.White
                                    )
            }
        }
    }
}

                    // Weight Chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                        .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val weights = listOf(5, 15, 25, 35, 45, 55, 65, 75)
                        items(weights) { weight ->
                            Surface(
                                onClick = { viewModel.setQuantity(weight.toString()) },
                            modifier = Modifier.height(32.dp),
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$weight Kg",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Location TextField
                        Text(
                            text = "Location:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                                    modifier = Modifier
                                        .fillMaxWidth()
                        .height(48.dp),
                    placeholder = { Text("Type here") },
                                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Section with Amount and Sell Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF084521),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val quantity = viewModel.quantity.collectAsState().value.toIntOrNull() ?: 0
                    val rate = if (useCurrentRate) 100 else customRate.toIntOrNull() ?: 0
                    val amount = quantity * rate
                    
                    Text(
                        text = "Amount: Rs. $amount",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Button(
                        onClick = { /* Handle sell action */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Sell",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboard(
    viewModel: MarketplaceViewModel,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Dashboard", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF084521)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Dashboard Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DashboardStatCard(
                    title = "Total Sales",
                    value = "₹45,000",
                    icon = Icons.Default.TrendingUp
                )
                DashboardStatCard(
                    title = "Active Listings",
                    value = "12",
                    icon = Icons.Default.ShoppingCart
                )
            }
            
            // Recent Activity
        Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            
            // Add your dashboard content here
        }
    }
}
