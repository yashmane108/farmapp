package com.example.myapplicationf.features.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import androidx.compose.animation.AnimatedVisibility
import com.example.myapplicationf.features.marketplace.models.Category
import com.example.myapplicationf.auth.AuthHelper
import com.example.myapplicationf.ui.theme.HeaderBackground
import com.example.myapplicationf.ui.theme.HeaderText
import com.example.myapplicationf.ui.theme.HeaderIcon
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

private val DarkGreen = Color(0xFF084521)
private val LightGreen = Color(0xFF236C24)
private val BackgroundGray = Color(0xFFF5F5F5)
private val SuccessGreen = Color(0xFF1B5E20)
private val SuccessLightGreen = Color(0xFFE8F5E9)
private val WarningAmber = Color(0xFFE65100)
private val WarningLightAmber = Color(0xFFFFF3E0)
private val ErrorRed = Color(0xFFB71C1C)
private val ErrorLightRed = Color(0xFFFFEBEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCropScreen(
    onBackPressed: () -> Unit,
    viewModel: MarketplaceViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedCrop by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf(0) }
    var location by remember { mutableStateOf("") }
    var locationExpanded by remember { mutableStateOf(false) }
    var customRate by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var cropExpanded by remember { mutableStateOf(false) }

    // Location selection state
    var showTalukaDropdown by remember { mutableStateOf(false) }
    var showVillageDropdown by remember { mutableStateOf(false) }
    var talukaSearchQuery by remember { mutableStateOf("") }
    var villageSearchQuery by remember { mutableStateOf("") }
    var selectedVillage by remember { mutableStateOf<String?>(null) }
    
    // Add state for seller name
    var sellerName by remember { mutableStateOf(getUserInfo().name) }
    
    val foodsByCategory by viewModel.foodsByCategory.collectAsState(initial = emptyMap())
    val availableCrops = selectedCategory?.let { foodsByCategory[it] } ?: emptyList()
    val talukas by viewModel.talukas.collectAsState()
    val selectedTaluka by viewModel.selectedTaluka.collectAsState()
    val villages by viewModel.villages.collectAsState()
    val filteredVillages by viewModel.filteredVillages.collectAsState()
    
    // Calculate total amount based on custom rate and quantity
    val rate = customRate.toDoubleOrNull() ?: 0.0
    val totalAmount = rate * quantity

    // Create a scroll state
    val scrollState = rememberScrollState()

    LaunchedEffect(selectedCrop) {
        selectedCrop?.let { crop ->
            viewModel.fetchTodaysRate(crop)
        } ?: viewModel.clearTodaysRate()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sell Crop", color = HeaderText) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HeaderIcon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderBackground
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Main Content - Now scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Food Category Dropdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.displayName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Food Category") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Expand",
                                modifier = Modifier.clickable { categoryExpanded = !categoryExpanded }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = !categoryExpanded },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )

                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        listOf(Category.GRAINS, Category.VEGETABLES, Category.FRUITS, Category.OILSEEDS).forEach { category ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCategory = category
                                        selectedCrop = null
                                        categoryExpanded = false
                                    }
                                    .background(Color.White)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Food/Crop Dropdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedCrop ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Food/Crop") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Expand",
                                modifier = Modifier.clickable(enabled = selectedCategory != null) { 
                                    if (selectedCategory != null) {
                                        cropExpanded = !cropExpanded 
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = selectedCategory != null) { 
                                if (selectedCategory != null) {
                                    cropExpanded = !cropExpanded 
                                }
                            },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        ),
                        enabled = selectedCategory != null
                    )

                    DropdownMenu(
                        expanded = cropExpanded,
                        onDismissRequest = { cropExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        availableCrops.forEach { crop ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCrop = crop
                                        cropExpanded = false
                                    }
                                    .background(Color.White)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = crop,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // TodaysRateSection
                TodaysRateSection(
                    selectedCrop = selectedCrop,
                    viewModel = viewModel,
                    onCustomRateChange = { customRate = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quantity Section
                Text("Quantity:", fontWeight = FontWeight.Medium)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (quantity > 0) quantity-- },
                            modifier = Modifier
                                .size(36.dp)
                                .background(LightGreen, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, "Decrease", tint = Color.White)
                        }
                        Text(
                            String.format("%02d Kg", quantity),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier
                                .size(36.dp)
                                .background(LightGreen, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, "Increase", tint = Color.White)
                        }
                    }
                }

                // Weight Chips using LazyVerticalGrid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = {
                        items(listOf(5, 15, 25, 35, 45, 55, 65, 75)) { kg ->
                            OutlinedButton(
                                onClick = { quantity = kg },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = BackgroundGray
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(width = 0.dp)
                            ) {
                                Text(
                                    "$kg Kg",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Contact Number
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { newValue ->
                        // Only allow digits and limit to 10 characters
                        if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                            contactNumber = newValue
                        }
                    },
                    label = { Text("Contact Number") },
                    placeholder = { Text("Enter your 10-digit mobile number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                // Add this after the contact number field and before the location section
                OutlinedTextField(
                    value = sellerName,
                    onValueChange = { sellerName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = { Text("Seller Name") },
                    placeholder = { Text("Enter your name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Taluka Selection
                    OutlinedTextField(
                        value = selectedTaluka ?: "",
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTalukaDropdown = true },
                        placeholder = { Text("Select Taluka") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (showTalukaDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Taluka",
                                modifier = Modifier.clickable { showTalukaDropdown = !showTalukaDropdown }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    // Taluka Dropdown
                    if (showTalukaDropdown) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Search field for talukas
                                OutlinedTextField(
                                    value = talukaSearchQuery,
                                    onValueChange = { query ->
                                        talukaSearchQuery = query
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    placeholder = { Text("Search Taluka") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                
                                // Filtered taluka list
                                val filteredTalukas = if (talukaSearchQuery.isBlank()) {
                                    talukas.map { it.name }
                                } else {
                                    talukas.map { it.name }.filter { 
                                        it.contains(talukaSearchQuery, ignoreCase = true) 
                                    }
                                }
                                
                                filteredTalukas.forEach { taluka ->
                                    Text(
                                        text = taluka,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setSelectedTaluka(taluka)
                                                showTalukaDropdown = false
                                                talukaSearchQuery = ""
                                            }
                                            .padding(16.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Village Selection (only enabled if taluka is selected)
                    OutlinedTextField(
                        value = selectedVillage ?: "",
                        onValueChange = { },
                        readOnly = true,
                        enabled = selectedTaluka != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (selectedTaluka != null) showVillageDropdown = true },
                        placeholder = { Text(if (selectedTaluka != null) "Select Village" else "Select Taluka first") },
                        trailingIcon = {
                            if (selectedTaluka != null) {
                                Icon(
                                    imageVector = if (showVillageDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Village",
                                    modifier = Modifier.clickable { showVillageDropdown = !showVillageDropdown }
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                    
                    // Village Dropdown
                    if (showVillageDropdown && selectedTaluka != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Search field for villages
                                OutlinedTextField(
                                    value = villageSearchQuery,
                                    onValueChange = { query ->
                                        villageSearchQuery = query
                                        viewModel.searchVillage(query)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    placeholder = { Text("Search Village") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                
                                // Filtered village list
                                filteredVillages.forEach { village ->
                                    Text(
                                        text = village,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedVillage = village
                                                location = "$selectedTaluka, $village"
                                                showVillageDropdown = false
                                                villageSearchQuery = ""
                                            }
                                            .padding(16.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Section - Fixed at the bottom
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Total Amount: Rs. ${String.format("%.2f", totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (selectedCrop != null && quantity > 0 && selectedVillage != null && contactNumber.length == 10 && sellerName.isNotBlank()) {
                                viewModel.addCrop(
                                    name = selectedCrop!!,
                                    quantity = quantity,
                                    rate = rate,
                                    location = "$selectedTaluka, $selectedVillage",
                                    category = selectedCategory!!,
                                    sellerName = sellerName,
                                    sellerContact = contactNumber
                                )
                                onBackPressed()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                        enabled = selectedCrop != null && quantity > 0 && selectedVillage != null && contactNumber.length == 10 && sellerName.isNotBlank()
                    ) {
                        Text("Sell")
                    }
                }
            }
        }
    }
}

@Composable
fun TodaysRateSection(
    selectedCrop: String?,
    viewModel: MarketplaceViewModel,
    onCustomRateChange: (String) -> Unit
) {
    var useCustomRate by remember { mutableStateOf(false) }
    var customRate by remember { mutableStateOf("") }

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
            Text(
                text = "Rate:",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = useCustomRate,
                onCheckedChange = { 
                    useCustomRate = it
                    if (!it) {
                        customRate = ""
                        onCustomRateChange("")
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Enter Your Rate:",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        AnimatedVisibility(visible = useCustomRate) {
            OutlinedTextField(
                value = customRate,
                onValueChange = { newValue ->
                    // Only allow numbers and decimal point
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        customRate = newValue
                        onCustomRateChange(newValue)
                    }
                },
                label = { Text("Custom Rate (Rs.)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = BackgroundGray,
                    focusedContainerColor = BackgroundGray,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun StatusTag(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "available" -> SuccessLightGreen to SuccessGreen
        "pending" -> WarningLightAmber to WarningAmber
        "unavailable", "sold" -> ErrorLightRed to ErrorRed
        else -> SuccessLightGreen to SuccessGreen
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}

// Add this function at the end of the file, outside the SellCropScreen composable
private data class UserInfo(
    val name: String,
    val contact: String
)

private fun getUserInfo(): UserInfo {
    val userEmail = AuthHelper.getCurrentUserEmail() ?: ""
    return UserInfo(
        name = userEmail.substringBefore('@'),
        contact = userEmail
    )
} 