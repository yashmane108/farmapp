package com.example.myapplicationf.features.marketplace.models

import com.example.myapplicationf.features.marketplace.PriceTrend

data class Product(
    val name: String,
    val basePrice: Double,
    val category: Category,
    val iconImagePath: String = "food_img/food Icon/${name.lowercase().replace(" ", "_")}.png",
    val fullImagePath: String = "food_img/food Full Img/${name.lowercase().replace(" ", "_")}_full.jpg",
    val rate: Double = basePrice,
    val priceTrend: PriceTrend = PriceTrend.STABLE,
    val imageUrl: String? = null
)

enum class PriceTrend {
    UP,
    DOWN,
    STABLE
} 