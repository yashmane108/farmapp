package com.example.myapplicationf.features.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.input.KeyboardType
import coil.request.ImageRequest
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.TextFieldValue
import com.example.myapplicationf.features.marketplace.models.Category
import com.example.myapplicationf.features.marketplace.models.Product
import com.example.myapplicationf.ui.theme.HeaderBackground
import com.example.myapplicationf.ui.theme.HeaderText
import com.example.myapplicationf.ui.theme.HeaderIcon

private val MarketplaceGreen = Color(0xFF4CAF50)
private val LightGreen = Color(0xFFE8F5E9)
private val DarkGreen = Color(0xFF388E3C)
private val DisabledGreen = Color(0xFFA5D6A7)

@Composable
fun WeightChip(
    weight: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MarketplaceGreen),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = MarketplaceGreen
        )
    ) {
        Text(
            text = weight,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSellProductScreen(
    viewModel: MarketplaceViewModel,
    onBackClick: () -> Unit,
    onDashboardClick: () -> Unit
) {
    val listedCrop by viewModel.selectedProduct.collectAsState()
    // Convert ListedCrop to Product
    val product = listedCrop?.let { crop ->
        Product(
            name = crop.name,
            basePrice = crop.rate.toDouble(),
            category = crop.category,
            rate = crop.rate.toDouble(),
            priceTrend = PriceTrend.STABLE
        )
    }
    
    var quantity by remember { mutableStateOf(0) }
    var location by remember { mutableStateOf("") }
    var isRateEditable by remember { mutableStateOf(false) }
    var customRate by rememberSaveable { mutableStateOf("") }
    val effectiveRate = viewModel.getEffectiveRate()
    val scrollState = rememberScrollState()
    
    // Calculate effective rate based on custom rate or product's original rate
    val getEffectiveRate = {
        customRate.toDoubleOrNull() ?: product?.rate ?: 0.0
    }

    // Function to update custom rate
    val setCustomRate = { value: String ->
        customRate = value
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sell Product", color = HeaderText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HeaderIcon)
                    }
                },
                actions = {
                    IconButton(onClick = onDashboardClick) {
                        Icon(Icons.Filled.Home, contentDescription = "Dashboard")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderBackground
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color(0xFFE8F5E9),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Amount: Rs. ${getEffectiveRate() * quantity}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Black
                    )
                    
                    Button(
                        onClick = { /* Handle sell action */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF084521),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Sell",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Product Image Section
            item {
                ProductImage(product = product ?: Product(
                    name = "",
                    basePrice = 0.0,
                    category = Category.GRAINS
                ))
                Spacer(modifier = Modifier.height(45.dp))
            }

            // Rate Options
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Today's Rate Option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = !isRateEditable,
                                onCheckedChange = { isRateEditable = !it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MarketplaceGreen,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.LightGray
                                ),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = "Today's Rate:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "₹${product?.rate ?: 0}/kg",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = "Trending",
                                tint = MarketplaceGreen,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }

                        // Enter Your Rate Option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = isRateEditable,
                                onCheckedChange = { isRateEditable = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MarketplaceGreen,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.LightGray
                                ),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = "Enter Your Rate:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            if (isRateEditable) {
                                OutlinedTextField(
                                    value = customRate,
                                    onValueChange = { setCustomRate(it) },
                                    modifier = Modifier
                                        .height(36.dp)
                                        .background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp)),
                                    placeholder = { Text("Enter here") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Quantity Selector
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Quantity:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledIconButton(
                                onClick = { if (quantity > 0) quantity-- },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MarketplaceGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Remove, "Decrease")
                            }

                            Text(
                                text = String.format("%02d Kg", quantity),
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White)
                                    .padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            FilledIconButton(
                                onClick = { quantity++ },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MarketplaceGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Add, "Increase")
                            }
                        }
                    }
                }
            }

            // Weight Chips
            item {
                SingleRowScrollableChips(
                    weights = listOf("5 Kg", "15 Kg", "25 Kg", "35 Kg", "45 Kg", "55 Kg", "65 Kg", "75 Kg"),
                    onWeightSelected = { weight ->
                        quantity = weight.substring(0, weight.indexOf(" ")).toInt() 
                    }
                )
            }

            // Location Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Location:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { newLocation -> 
                                location = newLocation
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp),
                            placeholder = { 
                                Text(
                                    "Type location here",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray
                                    )
                                ) 
                            },
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = MarketplaceGreen,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        if (location.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val filteredSuggestions = listOf(
                                "Mumbai, Maharashtra",
                                "Delhi, New Delhi",
                                "Bangalore, Karnataka",
                                "Chennai, Tamil Nadu",
                                "Kolkata, West Bengal",
                                "Hyderabad, Telangana",
                                "Pune, Maharashtra",
                                "Ahmedabad, Gujarat"
                            ).filter { 
                                it.lowercase().contains(location.lowercase()) 
                            }
                            if (filteredSuggestions.isNotEmpty()) {
                                LocationSuggestions(
                                    suggestions = filteredSuggestions,
                                    onSuggestionSelected = { suggestion ->
                                        location = suggestion
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SingleRowScrollableChips(
    weights: List<String>,
    onWeightSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            weights.forEach { weight ->
                WeightChip(weight = weight) {
                    onWeightSelected(weight)
                }
            }
        }
    }
}

@Composable
private fun LocationSuggestions(
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
        ) {
            suggestions.forEach { suggestion ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionSelected(suggestion) },
                    color = Color.White
                ) {
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun ProductImage(
    product: Product,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/food_img/food Full Img/${if (product.name == "Soybean") "Soybeans" else product.name} Fullimg.jpg")
                .crossfade(true)
                .build(),
            contentDescription = product.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            onError = { isError = true }
        )

        if (isError) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Image placeholder",
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun ProductIcon(
    product: Product,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/food_img/food Icon/${if (product.name == "Soybean") "Soybeans" else product.name} icon.${if (product.name in listOf("Soybean", "Rice", "Wheat")) "jpeg" else "jpg"}")
                .crossfade(true)
                .build(),
            contentDescription = product.name,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            onError = { isError = true }
        )

        if (isError) {
            Text(
                text = product.name.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 