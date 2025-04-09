package com.example.myapplicationf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplicationf.data.DashboardItem
import com.example.myapplicationf.features.chat.ChatScreen
import com.example.myapplicationf.features.weather.WeatherScreen
import com.example.myapplicationf.features.protection.CropProtectionScreen
import com.example.myapplicationf.features.news.CommunityNewsScreen
import com.example.myapplicationf.features.marketplace.MarketplaceScreen
import com.example.myapplicationf.ui.theme.MyApplicationFTheme
import com.example.myapplicationf.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationFTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                    
                    when (currentScreen) {
                        Screen.Dashboard -> FarmDashboard(
                            onNavigate = { screen -> currentScreen = screen }
                        )
                        Screen.Chat -> ChatScreen(
                            onBackPressed = { currentScreen = Screen.Dashboard }
                        )
                        Screen.Weather -> WeatherScreen(
                            onBackPressed = { currentScreen = Screen.Dashboard }
                        )
                        Screen.Protection -> CropProtectionScreen(
                            onBackPressed = { currentScreen = Screen.Dashboard }
                        )
                        Screen.News -> CommunityNewsScreen(
                            onBackPressed = { currentScreen = Screen.Dashboard }
                        )
                        Screen.Marketplace -> MarketplaceScreen(
                            onBackPressed = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object Dashboard : Screen()
    object Chat : Screen()
    object Weather : Screen()
    object Protection : Screen()
    object News : Screen()
    object Marketplace : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDashboard(
    viewModel: DashboardViewModel = viewModel(),
    onNavigate: (Screen) -> Unit
) {
    val dashboardItems by viewModel.dashboardItems.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farm Marketplace") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            DashboardGrid(
                items = dashboardItems,
                onItemClick = { item ->
                    when (item.title) {
                        "AI Chatbot" -> onNavigate(Screen.Chat)
                        "Weather Planning" -> onNavigate(Screen.Weather)
                        "Crop Protection" -> onNavigate(Screen.Protection)
                        "Community/News" -> onNavigate(Screen.News)
                        "Marketplace" -> onNavigate(Screen.Marketplace)
                    }
                }
            )
        }
    }
}

@Composable
fun DashboardGrid(
    items: List<DashboardItem>,
    onItemClick: (DashboardItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items.size) { index ->
            val item = items[index]
            DashboardCard(
                title = item.title,
                icon = getIconForType(item.iconType),
                onClick = { onItemClick(item) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun getIconForType(iconType: String): ImageVector {
    return when (iconType) {
        "Info" -> Icons.Default.Info
        "Home" -> Icons.Default.Home
        "Settings" -> Icons.Default.Settings
        "Star" -> Icons.Default.Star
        "ShoppingCart" -> Icons.Default.ShoppingCart
        else -> Icons.Default.Info
    }
}