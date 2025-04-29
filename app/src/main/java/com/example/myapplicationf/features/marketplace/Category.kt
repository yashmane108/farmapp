package com.example.myapplicationf.features.marketplace

enum class Category {
    FRUITS,
    VEGETABLES,
    GRAINS,
    OILSEEDS;

    override fun toString(): String {
        return when (this) {
            FRUITS -> "Fruits"
            VEGETABLES -> "Vegetables"
            GRAINS -> "Grains"
            OILSEEDS -> "Oilseeds"
        }
    }
} 