package com.example.myapplicationf.features.marketplace.models

import kotlinx.datetime.LocalDateTime

data class ListedCrop(
    val id: String,
    val name: String,
    val quantity: Int,
    val rate: Int,
    val location: String,
    val category: Category,
    val sellerName: String? = null,
    val sellerContact: String? = null,
    val timestamp: LocalDateTime? = null,
    val buyerDetails: List<BuyerDetail> = emptyList(),
    val isOwnListing: Boolean = false
)

data class BuyerDetail(
    val name: String,
    val contactInfo: String,
    val address: String,
    val requestedQuantity: Int,
    val status: String = "PENDING"
)

enum class Category {
    GRAINS, VEGETABLES, FRUITS, OILSEEDS
} 