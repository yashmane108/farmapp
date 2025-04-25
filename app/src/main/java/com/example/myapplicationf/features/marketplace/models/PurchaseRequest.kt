package com.example.myapplicationf.features.marketplace.models

data class PurchaseRequest(
    val id: String = "",
    val cropId: String = "",
    val cropName: String = "",
    val sellerName: String = "",
    val buyerEmail: String = "",
    val buyerName: String = "",
    val status: String = "PENDING",
    val quantity: Int = 0,
    val acceptedQuantity: Int = 0,
    val totalAmount: Double = 0.0,
    val date: String = "",
    val createdAt: com.google.firebase.Timestamp? = null,
    val acceptedAt: com.google.firebase.Timestamp? = null,
    val cancelReason: String? = null,
    val cancelledAt: com.google.firebase.Timestamp? = null
) {
    // Add a constructor that can create from Firestore data
    companion object {
        fun fromMap(data: Map<String, Any>): PurchaseRequest {
            return PurchaseRequest(
                id = data["id"] as? String ?: "",
                cropId = data["cropId"] as? String ?: "",
                cropName = data["cropName"] as? String ?: "",
                sellerName = data["sellerName"] as? String ?: "",
                buyerEmail = data["buyerEmail"] as? String ?: "",
                buyerName = data["buyerName"] as? String ?: "",
                status = data["status"] as? String ?: "PENDING",
                quantity = (data["quantity"] as? Long)?.toInt() ?: 0,
                acceptedQuantity = (data["acceptedQuantity"] as? Long)?.toInt() ?: 0,
                totalAmount = when (val amount = data["totalAmount"]) {
                    is Long -> amount.toDouble()
                    is Double -> amount
                    else -> 0.0
                },
                date = data["date"] as? String ?: "",
                createdAt = data["createdAt"] as? com.google.firebase.Timestamp,
                acceptedAt = data["acceptedAt"] as? com.google.firebase.Timestamp,
                cancelReason = data["cancelReason"] as? String,
                cancelledAt = data["cancelledAt"] as? com.google.firebase.Timestamp
            )
        }
    }
} 