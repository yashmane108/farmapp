package com.example.myapplicationf.features.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.SwitchDefaults

private val MarketplaceGreen = Color(0xFF4CAF50)
private val LightGreen = Color(0xFFE8F5E9)
private val DarkGreen = Color(0xFF388E3C)
private val DisabledGreen = Color(0xFFA5D6A7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSellProductScreen(
    viewModel: MarketplaceViewModel,
    onBackClick: () -> Unit,
    onDashboardClick: () -> Unit
) {
    val product = viewModel.selectedProduct.value
    var quantity by remember { mutableStateOf(0) }
    var location by remember { mutableStateOf("") }
    var isRateEditable by remember { mutableStateOf(false) }
    
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Market.Place") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onDashboardClick) {
                        Icon(Icons.Filled.Home, contentDescription = "Dashboard")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MarketplaceGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product Image with Name and Icon Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    // Full size background image
                    AsyncImage(
                        model = product?.fullImagePath,
                        contentDescription = product?.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Product name overlay
                    Text(
                        text = product?.name ?: "",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    // Circular icon overlay at bottom center
                    AsyncImage(
                        model = product?.iconImagePath,
                        contentDescription = "${product?.name} icon",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .align(Alignment.BottomCenter)
                            .offset(y = 40.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Today's Rate with Switch
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Today's Rate:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹${product?.rate ?: 0}/kg",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = "Trending",
                                tint = MarketplaceGreen,
                                modifier = Modifier.padding(start = 4.dp)
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
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Enter Your Rate:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isRateEditable) {
                                OutlinedTextField(
                                    value = viewModel.customRate.collectAsState().value,
                                    onValueChange = { viewModel.setCustomRate(it) },
                                    modifier = Modifier
                                        .height(40.dp)
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

                    // Quantity Selector
                    Spacer(modifier = Modifier.height(16.dp))
            
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                    // Reduced spacing between quantity and weight chips
                    Spacer(modifier = Modifier.height(16.dp))

                    // Weight Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("5 Kg", "15 Kg", "25 Kg", "35 Kg", "45 Kg", "55 Kg", "65 Kg", "75 Kg").forEach { weight ->
                            WeightChip(weight) { 
                                quantity = weight.substring(0, weight.indexOf(" ")).toInt() 
                            }
                        }
                    }

                    // Location
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            "Location:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Location Input Field with improved styling
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (location.isNotEmpty()) MarketplaceGreen else Color.Gray,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            placeholder = { 
                                Text(
                                    "Type location here",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray
                                    )
                                ) 
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.Black
                            )
                        )

                        // Suggestions Box with improved styling
                        if (location.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 150.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.Gray),
                                color = Color.White,
                                shadowElevation = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    listOf("Suggestion 1", "Suggestion 2").forEach { suggestion ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { location = suggestion },
                                            color = Color.White
                                        ) {
                                            Text(
                                                text = suggestion,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Amount and Sell Button - Fixed at bottom with increased top padding
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = LightGreen,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 24.dp,
                            bottom = 16.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Amount: Rs. ${viewModel.getEffectiveRate() * quantity}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
            
                    Button(
                        onClick = { /* Handle sell action */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (location.isNotBlank()) DarkGreen else DisabledGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = location.isNotBlank()
                    ) {
                        Text("Sell", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WeightChip(
    weight: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        border = ButtonDefaults.outlinedButtonBorder,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.Black,
            containerColor = Color.White
        )
    ) {
        Text(weight)
    }
} 