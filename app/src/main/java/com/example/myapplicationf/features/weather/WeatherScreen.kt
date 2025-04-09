package com.example.myapplicationf.features.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onBackPressed: () -> Unit,
    viewModel: WeatherViewModel = viewModel()
) {
    val weatherState by viewModel.weatherState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = weatherState.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Search for crop weather planning...") },
                    trailingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    },
                    singleLine = true
                )
            }

            item {
                Text(
                    text = "Current Weather",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                weatherState.currentWeather?.let { weather ->
                    CurrentWeatherCard(weather)
                }
            }

            if (weatherState.searchQuery.isNotEmpty()) {
                weatherState.selectedCrop?.let { cropPlan ->
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Crop Weather Planning",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CropWeatherPlanCard(cropPlan)
                    }
                } ?: item {
                    // Show no results message
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "No Results",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No results found for '${weatherState.searchQuery}'",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Available crops:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Sugarcane, Ginger, Turmeric\n• Rice, Wheat, Cotton\n• Tomato, Potato, Onion\n• Chili, Soybean, Groundnut",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try searching for one of these crops to see weather planning information",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Show available crops when no search
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Available Crops for Weather Planning",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    "Field Crops" to "Rice, Wheat, Cotton, Sugarcane",
                                    "Spices" to "Ginger, Turmeric, Chili",
                                    "Vegetables" to "Tomato, Potato, Onion",
                                    "Legumes" to "Soybean, Groundnut"
                                ).forEach { (category, crops) ->
                                    Text(
                                        text = "• $category: $crops",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Search for any crop to see weather planning details",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "5-Day Forecast",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(weatherState.forecast) { forecast ->
                ForecastCard(forecast)
            }

            // Add some bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(weather: WeatherInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${weather.temperature}°C",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = weather.description,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Icon(
                    imageVector = when (weather.description.lowercase()) {
                        "sunny" -> Icons.Outlined.LightMode
                        "partly cloudy" -> Icons.Outlined.FilterDrama
                        "cloudy" -> Icons.Outlined.CloudQueue
                        "light rain" -> Icons.Outlined.Grain
                        "heavy rain" -> Icons.Outlined.Opacity
                        "thunderstorm" -> Icons.Outlined.Bolt
                        "clear sky" -> Icons.Outlined.WbTwilight
                        "overcast" -> Icons.Outlined.Cloud
                        else -> Icons.Outlined.Cloud
                    },
                    contentDescription = weather.description,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Humidity: ${weather.humidity}%",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Date: ${weather.date}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun CropWeatherPlanCard(cropPlan: CropWeatherPlan) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = cropPlan.cropName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Ideal Conditions:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Temperature: ${cropPlan.idealTemperature}")
            Text("Humidity: ${cropPlan.idealHumidity}")
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recommendations:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                cropPlan.recommendations.forEach { recommendation ->
                    Text("• $recommendation")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Weather Precautions:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                cropPlan.precautions.forEach { precaution ->
                    Text("• $precaution")
                }
            }
        }
    }
}

@Composable
fun ForecastCard(weather: WeatherInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = weather.date,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = weather.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Humidity: ${weather.humidity}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${weather.temperature}°C",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = when (weather.description.lowercase()) {
                        "sunny" -> Icons.Outlined.LightMode
                        "partly cloudy" -> Icons.Outlined.FilterDrama
                        "cloudy" -> Icons.Outlined.CloudQueue
                        "light rain" -> Icons.Outlined.Grain
                        "heavy rain" -> Icons.Outlined.Opacity
                        "thunderstorm" -> Icons.Outlined.Bolt
                        "clear sky" -> Icons.Outlined.WbTwilight
                        "overcast" -> Icons.Outlined.Cloud
                        else -> Icons.Outlined.Cloud
                    },
                    contentDescription = weather.description,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
