package com.example.myapplicationf.service

import com.example.myapplicationf.data.DashboardItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DashboardService {
    private val dashboardItems = listOf(
        DashboardItem(1, "AI Chatbot", "Get instant farming advice", "Info"),
        DashboardItem(2, "Community/News", "Stay updated with farming news", "Home"),
        DashboardItem(3, "Crop Protection", "Manage crop health", "Settings"),
        DashboardItem(4, "Weather Planning", "Check weather forecasts", "Star"),
        DashboardItem(5, "Marketplace", "Buy and sell farm products", "ShoppingCart"),
        DashboardItem(6, "About Us", "Learn more about our app", "About")
    )

    fun getDashboardItems(): Flow<List<DashboardItem>> = flow {
        emit(dashboardItems)
    }

    fun getDashboardItem(id: Int): Flow<DashboardItem?> = flow {
        emit(dashboardItems.find { it.id == id })
    }
}
