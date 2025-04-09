package com.example.myapplicationf.features.marketplace

data class Product(
    val name: String,
    val rate: Int,
    val category: String = "",
    val fullImagePath: String = "food_img/food Full Img/${if (name == "Soybean") "Soybeans" else name} Fullimg.jpg",
    val iconImagePath: String = "food_img/food Icon/${if (name == "Soybean") "Soybeans" else name} icon.${if (name in listOf("Soybean", "Rice", "Wheat")) "jpeg" else "jpg"}",
    val priceTrend: PriceTrend = PriceTrend.STABLE
)

data class Variety(
    val name: String,
    val description: String,
    val isAvailable: Boolean = true
)

enum class ProductCategory {
    VEGETABLES,
    FRUITS,
    OILSEEDS,
    GRAINS
}

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

    // Add more product varieties as needed
} 