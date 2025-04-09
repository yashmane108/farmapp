package com.example.myapplicationf.features.marketplace

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MarketplaceViewModel : ViewModel() {
    private val locationService = LocationService()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    private val _currentMarketRate = MutableStateFlow<MarketRate?>(null)
    val currentMarketRate: StateFlow<MarketRate?> = _currentMarketRate.asStateFlow()

    private val _useCustomRate = MutableStateFlow(false)
    val useCustomRate: StateFlow<Boolean> = _useCustomRate.asStateFlow()

    private val _customRate = MutableStateFlow("")
    val customRate: StateFlow<String> = _customRate.asStateFlow()

    private val _selectedVariety = MutableStateFlow<ProductVariety?>(null)
    val selectedVariety: StateFlow<ProductVariety?> = _selectedVariety.asStateFlow()

    private val _quantity = MutableStateFlow("00")
    val quantity: StateFlow<String> = _quantity.asStateFlow()

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location.asStateFlow()

    private val _locationSuggestions = MutableStateFlow<List<String>>(emptyList())
    val locationSuggestions: StateFlow<List<String>> = _locationSuggestions.asStateFlow()

    val categories = listOf("All", "Vegetables", "Fruits", "Oilseeds", "Grains")

    val _products = listOf(
        // Oilseeds
        Product("Soybean", 120, "Oilseed", priceTrend = PriceTrend.UP),
        
        // Fruits
        Product("Grapes", 90, "Fruits", priceTrend = PriceTrend.DOWN),
        Product("Banana", 35, "Fruits", priceTrend = PriceTrend.UP),
        Product("Watermelon", 250, "Fruits", priceTrend = PriceTrend.SAME),
        Product("Strawberry", 60, "Fruits", priceTrend = PriceTrend.UP),
        
        // Vegetables
        Product("Potato", 25, "Vegetables", priceTrend = PriceTrend.DOWN),
        Product("Tomato", 180, "Vegetables", priceTrend = PriceTrend.UP),
        Product("Ginger", 40, "Vegetables", priceTrend = PriceTrend.DOWN),
        
        // Grain Crops
        Product("Makka", 30, "Grain Crop", priceTrend = PriceTrend.DOWN),
        Product("Jawar", 35, "Grain Crop", priceTrend = PriceTrend.UP),
        Product("Rice", 65, "Grain Crop", priceTrend = PriceTrend.UP),
        Product("Wheat", 40, "Grain Crop", priceTrend = PriceTrend.SAME),
        Product("Turmeric", 220, "Grain Crop", priceTrend = PriceTrend.UP),
        Product("Sugarcane", 35, "Grain Crop", priceTrend = PriceTrend.DOWN)
    )

    private val _listedCrops = MutableStateFlow<List<ListedCrop>>(
        listOf(
            ListedCrop("Wheat", 100, "Parali, Satara", 10),
            ListedCrop("Bajra", 300, "Wal, Satara", 20),
            ListedCrop("Wheat", 100, "Parali, Satara", 10),
            ListedCrop("Bajra", 300, "Wal, Satara", 20),
            ListedCrop("Bajra", 300, "Wal, Satara", 20),
            ListedCrop("Potato", 10, "Wal, Satara", 10),
            ListedCrop("Rice", 150, "Wal, Satara", 15)
        )
    )
    val listedCrops: StateFlow<List<ListedCrop>> = _listedCrops.asStateFlow()

    fun getProducts(): List<Product> = _products

    fun setUseCustomRate(useCustom: Boolean) {
        _useCustomRate.value = useCustom
    }

    fun setCustomRate(rate: String) {
        _customRate.value = rate
    }

    fun selectVariety(variety: ProductVariety) {
        _selectedVariety.value = variety
    }

    fun setQuantity(value: String) {
        _quantity.value = value.padStart(2, '0')
    }

    fun incrementQuantity() {
        val currentQty = _quantity.value.toIntOrNull() ?: 0
        setQuantity((currentQty + 1).toString())
    }

    fun decrementQuantity() {
        val currentQty = _quantity.value.toIntOrNull() ?: 0
        if (currentQty > 0) {
            setQuantity((currentQty - 1).toString())
        }
    }

    fun setLocation(location: String) {
        _location.value = location
        updateLocationSuggestions(location)
    }

    fun selectLocationSuggestion(suggestion: String) {
        _location.value = suggestion
        _locationSuggestions.value = emptyList()
    }

    private fun updateLocationSuggestions(query: String) {
        viewModelScope.launch {
            if (query.length >= 1) {
                val suggestions = locationService.getSuggestions(query)
                _locationSuggestions.value = suggestions
            } else {
                _locationSuggestions.value = emptyList()
            }
        }
    }

    fun getEffectiveRate(): Double {
        return if (_useCustomRate.value && _customRate.value.isNotEmpty()) {
            _customRate.value.toDoubleOrNull() ?: 0.0
        } else {
            _currentMarketRate.value?.price ?: 0.0
        }
    }

    fun getProductVarieties(productName: String): List<ProductVariety> {
        return when (productName.lowercase()) {
            "banana" -> listOf(
                ProductVariety("Robusta"),
                ProductVariety("Grand Naine"),
                ProductVariety("Red Banana"),
                ProductVariety("Safed Velchi"),
                ProductVariety("Ardhapuri"),
                ProductVariety("Deshi"),
                ProductVariety("Maskati")
            )
            "grapes" -> listOf(
                ProductVariety("Black Grapes"),
                ProductVariety("White Grapes"),
                ProductVariety("Thompson Seedless"),
                ProductVariety("Bhokri"),
                ProductVariety("Gulabi")
            )
            "strawberry" -> listOf(
                ProductVariety("Mahabaleshwar Strawberry")
            )
            "ginger" -> listOf(
                ProductVariety("Sona"),
                ProductVariety("Satara Mahim"),
                ProductVariety("Aurangabadi"),
                ProductVariety("Godhra"),
                ProductVariety("Varada"),
                ProductVariety("Nadia")
            )
            "jawar" -> listOf(
                ProductVariety("Mangalwedha Jowar")
            )
            "wheat" -> listOf(
                ProductVariety("Lokwan")
            )
            "rice" -> listOf(
                ProductVariety("Basmati"),
                ProductVariety("Indrayani"),
                ProductVariety("Kolam"),
                ProductVariety("Ambemohar"),
                ProductVariety("HMT")
            )
            // Add more products and their varieties as needed
            else -> emptyList()
        }
    }

    fun fetchMarketRate(productName: String) {
        viewModelScope.launch {
            // Simulated market rate fetch
            _currentMarketRate.value = MarketRate(
                price = when (productName.lowercase()) {
                    "banana" -> 40.0
                    "tomato" -> 30.0
                    "wheat" -> 25.0
                    "groundnut" -> 60.0
                    else -> 0.0
                },
                trend = PriceTrend.STABLE
            )
        }
    }

    fun filterProducts(products: List<Product>): List<Product> {
        return products.filter { product ->
            val matchesSearch = searchQuery.value.isEmpty() ||
                    product.name.contains(searchQuery.value, ignoreCase = true)
            val matchesCategory = selectedCategory.value == "All" ||
                    product.category == selectedCategory.value
            matchesSearch && matchesCategory
        }
    }

    fun formatMarketRate(rate: MarketRate): String {
        return String.format("₹%.2f/kg", rate.price)
    }

    fun getLastUpdatedTime(rate: MarketRate): String {
        val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return "Last updated: ${formatter.format(Date())}"
    }

    fun getTrendIcon(trend: PriceTrend): String {
        return when (trend) {
            PriceTrend.UP -> "↑"
            PriceTrend.DOWN -> "↓"
            PriceTrend.SAME, PriceTrend.STABLE -> "→"
        }
    }

    fun getTrendColor(trend: PriceTrend): Long {
        return when (trend) {
            PriceTrend.UP -> 0xFFE57373    // Red
            PriceTrend.DOWN -> 0xFF81C784  // Green
            PriceTrend.SAME, PriceTrend.STABLE -> 0xFF78909C  // Blue Grey
        }
    }

    fun addListedCrop(crop: ListedCrop) {
        _listedCrops.value = _listedCrops.value + crop
    }

    fun getFilteredListedCrops(category: String): List<ListedCrop> {
        return when (category) {
            "All" -> _listedCrops.value
            "Vegetables" -> _listedCrops.value.filter { crop ->
                _products.find { it.name == crop.cropName }?.category == "Vegetables"
            }
            "Crop" -> _listedCrops.value.filter { crop ->
                _products.find { it.name == crop.cropName }?.category == "Grain Crop"
            }
            "Oil seeds" -> _listedCrops.value.filter { crop ->
                _products.find { it.name == crop.cropName }?.category == "Oilseed"
            }
            else -> _listedCrops.value
        }
    }

    fun setUserRole(role: UserRole) {
        _userRole.value = role
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedProduct(product: Product) {
        _selectedProduct.value = product
        _selectedVariety.value = null  // Reset variety when product changes
        _customRate.value = ""  // Clear custom rate when product changes
        _useCustomRate.value = false  // Reset custom rate toggle
    }

    data class ProductVariety(val name: String)
}

enum class UserRole {
    BUYER,
    FARMER
}

enum class PriceTrend {
    UP,
    DOWN,
    SAME,
    STABLE
}

data class ListedCrop(
    val cropName: String,
    val ratePerKg: Int,
    val location: String,
    val quantityToSell: Int
)

data class MarketRate(
    val price: Double,
    val trend: PriceTrend
)
