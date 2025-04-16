package com.example.myapplicationf.features.marketplace

import com.example.myapplicationf.features.marketplace.models.Category

enum class PriceTrend {
    UP,
    DOWN,
    STABLE
}

data class Product(
    val id: String = "",
    val name: String,
    val basePrice: Double = 0.0,
    val category: Category = Category.VEGETABLES,
    val priceTrend: PriceTrend = PriceTrend.STABLE,
    val description: String = "",
    val imageUrl: String = ""
)

data class Variety(
    val name: String,
    val description: String,
    val isAvailable: Boolean = true
)

// Predefined varieties for different products
object ProductVarieties {
    val bananaVarieties = listOf(
        Variety("Grand Naine", "Most popular variety in Maharashtra"),
        Variety("Robusta", "High yielding variety"),
        Variety("Red Banana", "Sweet and nutritious")
    )

    val tomatoVarieties = listOf(
        Variety("Hybrid Tomato", "High yielding hybrid variety"),
        Variety("Local Tomato", "Traditional variety"),
        Variety("Cherry Tomato", "Small and sweet")
    )

    val wheatVarieties = listOf(
        Variety("Lokwan", "Popular in Maharashtra"),
        Variety("Sharbati", "Premium quality wheat"),
        Variety("MP Wheat", "High yielding variety")
    )

    val groundnutVarieties = listOf(
        Variety("TG-37A", "High yielding variety"),
        Variety("JL-24", "Drought resistant"),
        Variety("TMV-2", "Early maturing variety")
    )
} 