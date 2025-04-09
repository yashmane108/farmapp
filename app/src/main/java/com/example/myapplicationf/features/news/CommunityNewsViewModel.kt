package com.example.myapplicationf.features.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class NewsItem(
    val category: String,
    val title: String,
    val content: String,
    val date: String
)

data class CommunityEvent(
    val title: String,
    val date: String,
    val location: String,
    val description: String
)

data class MarketPrice(
    val cropName: String,
    val price: Double,
    val trend: String  // e.g., "+2.5%", "-1.2%"
)

data class NewsState(
    val newsItems: List<NewsItem> = emptyList(),
    val events: List<CommunityEvent> = emptyList(),
    val marketPrices: List<MarketPrice> = emptyList(),
    val isLoading: Boolean = false
)

class CommunityNewsViewModel : ViewModel() {
    private val _newsState = MutableStateFlow(NewsState())
    val newsState: StateFlow<NewsState> = _newsState

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val news = listOf(
            NewsItem(
                "Policy Update",
                "New Government Scheme for Organic Farming",
                "The government has announced a new scheme providing subsidies for organic farming certification and training programs.",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            ),
            NewsItem(
                "Market Insight",
                "Rice Export Demand Increases",
                "Global demand for Indian rice varieties has shown significant growth in the past quarter.",
                LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE)
            ),
            NewsItem(
                "Technology",
                "Smart Irrigation Systems Launch",
                "New IoT-based irrigation systems are now available with government subsidies for small farmers.",
                LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_DATE)
            )
        )

        val events = listOf(
            CommunityEvent(
                "Farmer's Training Workshop",
                LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE),
                "District Agricultural Center",
                "Learn about modern farming techniques and pest control methods."
            ),
            CommunityEvent(
                "Agricultural Trade Fair",
                LocalDate.now().plusDays(14).format(DateTimeFormatter.ISO_DATE),
                "City Exhibition Ground",
                "Annual trade fair featuring latest farming equipment and technologies."
            )
        )

        val prices = listOf(
            MarketPrice("Rice", 40.50, "+2.5%"),
            MarketPrice("Wheat", 30.75, "-1.2%"),
            MarketPrice("Corn", 25.00, "+0.8%"),
            MarketPrice("Soybeans", 45.25, "+3.1%")
        )

        _newsState.value = NewsState(
            newsItems = news,
            events = events,
            marketPrices = prices
        )
    }

    fun refreshNews() {
        viewModelScope.launch {
            _newsState.value = _newsState.value.copy(isLoading = true)
            
            // Simulate API call delay
            delay(1000)

            // Add a new news item
            val newNews = NewsItem(
                "Weather Alert",
                "Monsoon Update",
                "Meteorological Department predicts early monsoon arrival this year.",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            )

            val currentNews = _newsState.value.newsItems.toMutableList()
            currentNews.add(0, newNews)

            // Update market prices
            val updatedPrices = _newsState.value.marketPrices.map { price ->
                val randomFactor = 1.0 + Random.nextDouble(-0.05, 0.05)
                price.copy(
                    price = price.price * randomFactor,
                    trend = if (Random.nextBoolean()) 
                        "+${Random.nextInt(1, 6)}.${Random.nextInt(0, 10)}%" 
                    else 
                        "-${Random.nextInt(1, 6)}.${Random.nextInt(0, 10)}%"
                )
            }

            _newsState.value = _newsState.value.copy(
                newsItems = currentNews,
                marketPrices = updatedPrices,
                isLoading = false
            )
        }
    }
}
