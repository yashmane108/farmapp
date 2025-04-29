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
import androidx.lifecycle.viewmodel.compose.viewModel
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
    onBackPressed: () -> Unit,
    viewModel: MarketplaceViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var selectedQuantity by remember { mutableStateOf(1f) }
    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var isPhoneValid by remember { mutableStateOf(true) }
    
    // Validation
    val maxQuantity = crop.quantity.toFloat()
    val isQuantityValid = try {
        selectedQuantity in 1f..maxQuantity
    } catch (e: NumberFormatException) {
        false
    }
    
    val isFormValid = buyerName.isNotBlank() && buyerPhone.isNotBlank() && deliveryAddress.isNotBlank() && isQuantityValid
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Details") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        value = buyerPhone,
                        onValueChange = { newValue ->
                            // Only allow digits and limit to 10 characters
                            if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                                buyerPhone = newValue
                            }
                        },
                        label = { Text("Phone Number") },
                        supportingText = { Text("Enter 10-digit mobile number") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isPhoneValid && buyerPhone.isNotBlank()
                    )
                    
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Delivery Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Slider(
                        value = selectedQuantity,
                        onValueChange = { selectedQuantity = it },
                        valueRange = 1f..crop.quantity.toFloat(),
                        steps = crop.quantity - 1
                    )
                    
                    Text("Selected Quantity: ${selectedQuantity.toInt()} kg")
                    
                    Button(
                        onClick = { showPurchaseDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isFormValid
                    ) {
                        Text("Send Request to Seller")
                    }
                }
            }
        }
    }
    
    if (showPurchaseDialog) {
        AlertDialog(
            onDismissRequest = { showPurchaseDialog = false },
            title = { Text("Purchase Request") },
            text = {
                Column {
                    TextField(
                        value = buyerName,
                        onValueChange = { buyerName = it },
                        label = { Text("Your Name") }
                    )
                    TextField(
                        value = buyerPhone,
                        onValueChange = { buyerPhone = it },
                        label = { Text("Phone Number") }
                    )
                    TextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Delivery Address") }
                    )
                    Slider(
                        value = selectedQuantity,
                        onValueChange = { selectedQuantity = it },
                        valueRange = 1f..crop.quantity.toFloat(),
                        steps = crop.quantity - 1
                    )
                    Text("Selected Quantity: ${selectedQuantity.toInt()} kg")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val buyerDetail = BuyerDetail(
                            name = buyerName,
                            contactInfo = buyerPhone,
                            address = deliveryAddress,
                            requestedQuantity = selectedQuantity.toInt(),
                            status = "PENDING"
                        )
                        viewModel.sendPurchaseRequest(crop.id, buyerDetail)
                        showPurchaseDialog = false
                        onBackPressed()
                    },
                    enabled = isFormValid
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDialog = false }) {
                    Text("Cancel")
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