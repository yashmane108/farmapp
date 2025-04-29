package com.example.myapplicationf.features.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.myapplicationf.features.marketplace.models.Category
import com.example.myapplicationf.features.marketplace.models.ListedCrop

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
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.LightGray
                ) {
                    // Image would go here
                }
                
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp),
                    shape = CircleShape,
                    color = when(product.category) {
                        Category.GRAINS -> Color(0xFF8D6E63)
                        Category.VEGETABLES -> Color(0xFF4CAF50)
                        Category.FRUITS -> Color(0xFFE91E63)
                        Category.DAIRY -> Color(0xFF2196F3)
                        Category.MEAT -> Color(0xFFF44336)
                        Category.OILSEEDS -> Color(0xFFFFC107)
                        Category.OTHER -> Color(0xFF9E9E9E)
                    }
                ) {
                    Icon(
                        imageVector = when(product.category) {
                            Category.GRAINS -> Icons.Filled.Grain
                            Category.VEGETABLES -> Icons.Filled.LocalFlorist
                            Category.FRUITS -> Icons.Filled.LocalDining
                            Category.DAIRY -> Icons.Filled.LocalDining
                            Category.MEAT -> Icons.Filled.Restaurant
                            Category.OILSEEDS -> Icons.Filled.OilBarrel
                            Category.OTHER -> Icons.Filled.Category
                        },
                        contentDescription = product.category.name,
                        modifier = Modifier.padding(8.dp),
                        tint = Color.White
                    )
                }
            }
            
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
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "₹${product.basePrice}/kg",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF024902)
                    )
                    
                    Icon(
                        imageVector = when(product.priceTrend) {
                            PriceTrend.UP -> Icons.Default.KeyboardArrowUp
                            PriceTrend.DOWN -> Icons.Default.KeyboardArrowDown
                            else -> Icons.Outlined.Remove
                        },
                        contentDescription = "Price trend",
                        tint = when(product.priceTrend) {
                            PriceTrend.UP -> Color(0xFF4CAF50)
                            PriceTrend.DOWN -> Color(0xFFF44336)
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = when(product.category) {
                        Category.GRAINS -> "Grains"
                        Category.VEGETABLES -> "Vegetables"
                        Category.FRUITS -> "Fruits"
                        Category.DAIRY -> "Dairy"
                        Category.MEAT -> "Meat"
                        Category.OILSEEDS -> "Oilseeds"
                        Category.OTHER -> "Other"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                color = Color.LightGray.copy(alpha = 0.3f)
            ) {
                // Product image placeholder
            }
            
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 20.dp),
                shape = CircleShape,
                color = when(product.category) {
                    Category.GRAINS -> Color(0xFF8D6E63)
                    Category.VEGETABLES -> Color(0xFF4CAF50)
                    Category.FRUITS -> Color(0xFFE91E63)
                    Category.DAIRY -> Color(0xFF2196F3)
                    Category.MEAT -> Color(0xFFF44336)
                    Category.OILSEEDS -> Color(0xFFFFC107)
                    Category.OTHER -> Color(0xFF9E9E9E)
                }
            ) {
                Icon(
                    imageVector = when(product.category) {
                        Category.GRAINS -> Icons.Filled.Grain
                        Category.VEGETABLES -> Icons.Filled.LocalFlorist
                        Category.FRUITS -> Icons.Filled.LocalDining
                        Category.DAIRY -> Icons.Filled.LocalDining
                        Category.MEAT -> Icons.Filled.Restaurant
                        Category.OILSEEDS -> Icons.Filled.OilBarrel
                        Category.OTHER -> Icons.Filled.Category
                    },
                    contentDescription = product.category.name,
                    modifier = Modifier.padding(8.dp),
                    tint = Color.White
                )
            }
        }
        
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = product.name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "₹${product.basePrice}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF024902)
                )
                
                Icon(
                    imageVector = when(product.priceTrend) {
                        PriceTrend.UP -> Icons.Default.KeyboardArrowUp
                        PriceTrend.DOWN -> Icons.Default.KeyboardArrowDown
                        else -> Icons.Outlined.Remove
                    },
                    contentDescription = "Price trend",
                    tint = when(product.priceTrend) {
                        PriceTrend.UP -> Color(0xFF4CAF50)
                        PriceTrend.DOWN -> Color(0xFFF44336)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilter(
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4CAF50),
                    selectedLabelColor = Color.White
                )
            )
        }

        items(Category.values()) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4CAF50),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Base Price: ₹${product.basePrice}/kg",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Category: ${product.category.name}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ProductGrid(
    products: List<Product>,
    onProductClick: (Product) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                onClick = { onProductClick(product) }
            )
        }
    }
}

@Composable
fun ListedCropCard(
    crop: ListedCrop,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
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
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Category: ${crop.category.name}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
