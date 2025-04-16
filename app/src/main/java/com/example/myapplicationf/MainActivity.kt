package com.example.myapplicationf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.myapplicationf.auth.AuthHelper
import com.example.myapplicationf.data.DashboardItem
import com.example.myapplicationf.features.auth.AuthScreen
import com.example.myapplicationf.features.auth.AuthViewModel
import com.example.myapplicationf.features.chat.ChatScreen
import com.example.myapplicationf.features.marketplace.CropDetailScreen
import com.example.myapplicationf.features.marketplace.MarketplaceScreen
import com.example.myapplicationf.features.marketplace.MarketplaceViewModel
import com.example.myapplicationf.features.marketplace.SellCropScreen
import com.example.myapplicationf.features.news.CommunityNewsScreen
import com.example.myapplicationf.features.protection.CropProtectionScreen
import com.example.myapplicationf.features.weather.WeatherScreen
import com.example.myapplicationf.ui.theme.MyApplicationFTheme
import com.example.myapplicationf.viewmodel.DashboardViewModel
import com.example.myapplicationf.ui.theme.HeaderBackground
import com.example.myapplicationf.ui.theme.HeaderText
import com.example.myapplicationf.ui.theme.HeaderIcon
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationFTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val systemUiController = rememberSystemUiController()
    val headerColor = HeaderBackground
    LaunchedEffect(systemUiController, headerColor) {
        systemUiController.setStatusBarColor(
            color = headerColor,
            darkIcons = false
        )
    }

    // Simple state management - only one source of truth
    val isAuthenticated by AuthHelper.isAuthenticated.collectAsState()
    
    // If authenticated, show the main application, otherwise show login
    if (isAuthenticated) {
        AppContent()
    } else {
        LoginContent()
    }
}

@Composable
fun LoginContent() {
    val authViewModel: AuthViewModel = viewModel()
    AuthScreen(
        viewModel = authViewModel,
        onAuthSuccess = { /* No need to do anything, AuthHelper handles state */ }
    )
}

@Composable
fun AppContent() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    
    when (currentScreen) {
        Screen.Dashboard -> {
            FarmDashboard(
                onNavigate = { screen -> currentScreen = screen },
                onSignOut = { 
                    // Use our AuthHelper to ensure signOut is handled consistently
                    AuthHelper.signOut()
                }
            )
        }
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
            onBackPressed = { currentScreen = Screen.Dashboard },
            onNavigateToSell = { currentScreen = Screen.SellCrop },
            onCropSelected = { cropId -> currentScreen = Screen.CropDetail(cropId) },
            viewModel = viewModel()
        )
        Screen.SellCrop -> SellCropScreen(
            onBackPressed = { currentScreen = Screen.Marketplace },
            viewModel = viewModel()
        )
        is Screen.CropDetail -> {
            val cropId = (currentScreen as Screen.CropDetail).cropId
            val marketplaceViewModel: MarketplaceViewModel = viewModel()
            // Trigger loading of the selected crop
            LaunchedEffect(cropId) {
                marketplaceViewModel.selectCrop(cropId)
            }
            
            val selectedCrop by marketplaceViewModel.selectedCrop.collectAsState()
            selectedCrop?.let { crop ->
                CropDetailScreen(
                    crop = crop,
                    onNavigateUp = { currentScreen = Screen.Marketplace },
                    viewModel = marketplaceViewModel
                )
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
    object SellCrop : Screen()
    data class CropDetail(val cropId: String) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDashboard(
    viewModel: DashboardViewModel = viewModel(),
    onNavigate: (Screen) -> Unit,
    onSignOut: () -> Unit
) {
    val dashboardItems by viewModel.dashboardItems.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farm Marketplace", color = HeaderText) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderBackground,
                    titleContentColor = HeaderText
                ),
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = HeaderIcon
                        )
                    }
                }
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