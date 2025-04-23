package com.example.myapplicationf.features.marketplace.models

data class PurchaseRequest(
    val id: String,
    val cropName: String,
    val sellerName: String,
    val status: String,
    val quantity: Int,
    val totalAmount: Double,
    val date: String
) 