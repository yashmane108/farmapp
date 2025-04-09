package com.example.myapplicationf.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationf.data.DashboardItem
import com.example.myapplicationf.service.DashboardService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val dashboardService: DashboardService = DashboardService()
) : ViewModel() {
    private val _dashboardItems = MutableStateFlow<List<DashboardItem>>(emptyList())
    val dashboardItems: StateFlow<List<DashboardItem>> = _dashboardItems

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadDashboardItems()
    }

    private fun loadDashboardItems() {
        viewModelScope.launch {
            dashboardService.getDashboardItems()
                .catch { e ->
                    _error.value = e.message
                }
                .collect { items ->
                    _dashboardItems.value = items
                }
        }
    }

    fun getDashboardItem(id: Int) {
        viewModelScope.launch {
            dashboardService.getDashboardItem(id)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { _ ->
                    // Handle single item retrieval if needed
                }
        }
    }
}
