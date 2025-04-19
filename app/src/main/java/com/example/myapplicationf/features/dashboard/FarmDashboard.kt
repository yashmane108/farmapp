package com.example.myapplicationf.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplicationf.Screen
import com.example.myapplicationf.ui.theme.HeaderBackground
import com.example.myapplicationf.ui.theme.HeaderText

private val LightPurple = Color(0xFFE1D5E7)
private val DarkPurple = Color(0xFF311B92)

data class DashboardItem(val title: String, val icon: ImageVector, val screen: Screen)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDashboard(
    onNavigate: (Screen) -> Unit
) {
    val dashboardItems = listOf(
        DashboardItem("AI Chatbot", Icons.Default.Info, Screen.Chat), // Assuming Chat is AI Chatbot
        DashboardItem("Community/News", Icons.Default.Home, Screen.News),
        DashboardItem("Crop Protection", Icons.Default.Settings, Screen.Protection),
        DashboardItem("Weather Planning", Icons.Default.Star, Screen.Weather),
        DashboardItem("Marketplace", Icons.Default.ShoppingCart, Screen.Marketplace)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farm Marketplace", color = HeaderText) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderBackground
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dashboardItems) { item ->
                DashboardGridItem(item = item, onClick = { onNavigate(item.screen) })
            }
        }
        // Removed bottom logout button
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardGridItem(
    item: DashboardItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightPurple),
        modifier = Modifier.aspectRatio(1f) // Make items square
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(48.dp),
                tint = DarkPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                textAlign = TextAlign.Center,
                color = DarkPurple,
                fontSize = 14.sp
            )
        }
    }
} 