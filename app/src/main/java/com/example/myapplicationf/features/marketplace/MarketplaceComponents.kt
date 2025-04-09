package com.example.myapplicationf.features.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image with icon overlay
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
            ) {
                // Product image background
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.LightGray
                ) {
                    // Image would go here
                }
                
                // Category icon overlay (half on image, half off)
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp),
                    shape = CircleShape,
                    color = when(product.category) {
                        "Fruits" -> Color(0xFFFFB74D) // Orange
                        "Vegetables" -> Color(0xFF81C784) // Green
                        "Grain Crop" -> Color(0xFFFFD54F) // Yellow
                        "Oilseed" -> Color(0xFFFF8A65) // Orange-red
                        else -> Color.Gray
                    }
                ) {
                    // Category icon would go here
                    Icon(
                        imageVector = when(product.category) {
                            "Fruits" -> Icons.Default.Favorite
                            "Vegetables" -> Icons.Default.Star
                            "Grain Crop" -> Icons.Default.ShoppingCart
                            "Oilseed" -> Icons.Default.Info
                            else -> Icons.Default.Home
                        },
                        contentDescription = product.category,
                        modifier = Modifier.padding(8.dp),
                        tint = Color.White
                    )
                }
            }
            
            // Product details
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Price with trend indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "₹${product.rate}/kg",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF024902) // Dark green
                    )
                    
                    // Price trend indicator
                    Icon(
                        imageVector = when(product.priceTrend) {
                            PriceTrend.UP -> Icons.Default.KeyboardArrowUp
                            PriceTrend.DOWN -> Icons.Default.KeyboardArrowDown
                            else -> Icons.Outlined.Remove
                        },
                        contentDescription = "Price trend",
                        tint = when(product.priceTrend) {
                            PriceTrend.UP -> Color(0xFF4CAF50) // Green
                            PriceTrend.DOWN -> Color(0xFFF44336) // Red
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "View details",
                modifier = Modifier.padding(end = 16.dp),
                tint = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedProductItem(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            // Product image
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                color = Color.LightGray.copy(alpha = 0.3f)
            ) {
                // Product image placeholder
            }
            
            // Category icon overlay (half on image, half below)
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 20.dp),
                shape = CircleShape,
                color = when(product.category) {
                    "Fruits" -> Color(0xFFFFB74D) // Orange
                    "Vegetables" -> Color(0xFF81C784) // Green
                    "Grain Crop" -> Color(0xFFFFD54F) // Yellow
                    "Oilseed" -> Color(0xFFFF8A65) // Orange-red
                    else -> Color.Gray
                }
            ) {
                // Category icon
                Icon(
                    imageVector = when(product.category) {
                        "Fruits" -> Icons.Default.Favorite
                        "Vegetables" -> Icons.Default.Star
                        "Grain Crop" -> Icons.Default.ShoppingCart
                        "Oilseed" -> Icons.Default.Info
                        else -> Icons.Default.Home
                    },
                    contentDescription = product.category,
                    modifier = Modifier.padding(8.dp),
                    tint = Color.White
                )
            }
        }
        
        // Product details
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Space for the overlapping icon
            
            Text(
                text = product.name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            // Price with trend indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "₹${product.rate}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF024902) // Dark green
                )
                
                // Price trend indicator
                Icon(
                    imageVector = when(product.priceTrend) {
                        PriceTrend.UP -> Icons.Default.KeyboardArrowUp
                        PriceTrend.DOWN -> Icons.Default.KeyboardArrowDown
                        else -> Icons.Outlined.Remove
                    },
                    contentDescription = "Price trend",
                    tint = when(product.priceTrend) {
                        PriceTrend.UP -> Color(0xFF4CAF50) // Green
                        PriceTrend.DOWN -> Color(0xFFF44336) // Red
                        else -> Color.Gray
                    },
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Enhanced category chip group for horizontal scrolling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedChipGroup(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF81C784),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
