package com.example.myapplicationf.features.marketplace.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class BuyerDetail(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val requestedQuantity: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val rating: Double = 0.0,
    val totalRatings: Int = 0,
    val totalPurchases: Int = 0,
    val contactInfo: String = "",
    val status: String = "PENDING"
) 