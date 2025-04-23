package com.example.myapplicationf.features.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplicationf.auth.AuthHelper
import com.example.myapplicationf.features.marketplace.models.BuyerDetail
import com.example.myapplicationf.features.marketplace.models.ListedCrop
import com.example.myapplicationf.ui.theme.HeaderBackground
import com.example.myapplicationf.ui.theme.HeaderIcon
import com.example.myapplicationf.ui.theme.HeaderText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDetailScreen(
    crop: ListedCrop,
    onNavigateUp: () -> Unit,
    viewModel: MarketplaceViewModel
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    // Form state
    var buyerName by remember { mutableStateOf(AuthHelper.getCurrentUserEmail()?.substringBefore('@') ?: "") }
    var address by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var contactNumber by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Validation
    val maxQuantity = crop.quantity
    val isQuantityValid = try {
        quantity.toInt() in 1..maxQuantity
    } catch (e: NumberFormatException) {
        false
    }
    
    val isContactNumberValid = contactNumber.length == 10 && contactNumber.all { it.isDigit() }
    val isFormValid = buyerName.isNotBlank() && address.isNotBlank() && isQuantityValid && isContactNumberValid
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(crop.name, color = HeaderText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = HeaderIcon
                        )
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Crop Details
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Crop Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Divider()
                    DetailRow("Name", crop.name)
                    DetailRow("Quantity", "${crop.quantity} kg")
                    DetailRow("Rate", "₹${crop.rate}/kg")
                    DetailRow("Location", crop.location)
                    DetailRow("Category", crop.category.toString())
                    DetailRow("Seller", crop.sellerName ?: "Unknown")
                    DetailRow("Contact", crop.sellerContact ?: "Not available")
                }
            }
            
            // Purchase Request Form
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Purchase Request",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Divider()
                    
                    OutlinedTextField(
                        value = buyerName,
                        onValueChange = { buyerName = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Delivery Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity (kg)") },
                        supportingText = { Text("Maximum available: $maxQuantity kg") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isQuantityValid && quantity.isNotBlank()
                    )
                    
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { newValue ->
                            // Only allow digits and limit to 10 characters
                            if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                                contactNumber = newValue
                            }
                        },
                        label = { Text("Contact Number") },
                        supportingText = { Text("Enter 10-digit mobile number") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isContactNumberValid && contactNumber.isNotBlank()
                    )
                    
                    val totalAmount = try {
                        "₹${quantity.toInt() * crop.rate}"
                    } catch (e: NumberFormatException) {
                        "₹0"
                    }
                    
                    Text(
                        text = "Total Amount: $totalAmount",
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = {
                            if (isFormValid) {
                                coroutineScope.launch {
                                    val buyerDetail = BuyerDetail(
                                        name = buyerName,
                                        contactInfo = contactNumber,
                                        address = address,
                                        requestedQuantity = quantity.toInt()
                                    )
                                    
                                    viewModel.sendPurchaseRequest(crop.id, buyerDetail)
                                    showSuccessDialog = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isFormValid
                    ) {
                        Text("Send Request to Seller")
                    }
                }
            }
        }
    }
    
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onNavigateUp()
            },
            title = { Text("Success") },
            text = { Text("Your purchase request has been sent to the seller. They will contact you shortly.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateUp()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
} 