package com.example.myapplicationf.features.marketplace.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LocationService {
    suspend fun getSuggestedLocations(query: String): List<String> = withContext(Dispatchers.IO) {
        // Simulated location suggestions
        listOf(
            "Mumbai, Maharashtra",
            "Delhi, NCR",
            "Bangalore, Karnataka",
            "Chennai, Tamil Nadu",
            "Kolkata, West Bengal"
        ).filter { it.contains(query, ignoreCase = true) }
    }

    fun getCurrentLocation(): Flow<String> = flow {
        // Simulated current location
        emit("Mumbai, Maharashtra")
    }.flowOn(Dispatchers.IO)
} 