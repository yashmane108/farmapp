package com.example.myapplicationf.features.marketplace.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ListedCrop(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val category: Category = Category.OTHER,
    val imageUrl: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val createdAt: Timestamp? = null,
    val status: String = "available",
    val rating: Double = 0.0,
    val totalRatings: Int = 0,
    val rate: Double = 0.0,
    val location: String = "",
    val sellerName: String = "",
    val sellerContact: String = "",
    val isOwnListing: Boolean = false,
    val buyerDetails: List<BuyerDetail> = emptyList(),
    val timestamp: Timestamp? = null
) 