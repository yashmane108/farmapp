package com.example.myapplicationf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
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
import com.example.myapplicationf.features.about.AboutUsScreen
import com.example.myapplicationf.features.chat.ChatScreen
import com.example.myapplicationf.features.marketplace.CropDetailScreen
import com.example.myapplicationf.features.marketplace.MarketplaceScreen
import com.example.myapplicationf.features.marketplace.MarketplaceViewModel
import com.example.myapplicationf.features.marketplace.SellCropScreen
import com.example.myapplicationf.features.marketplace.FarmerDashboardScreen
import com.example.myapplicationf.features.news.CommunityNewsScreen
import com.example.myapplicationf.features.protection.CropProtectionScreen
import com.example.myapplicationf.features.weather.WeatherScreen
import com.example.myapplicationf.ui.theme.MyApplicationFTheme
import com.example.myapplicationf.viewmodel.DashboardViewModel
import com.example.myapplicationf.ui.theme.HeaderBackground
import com.example.myapplicationf.ui.theme.HeaderText
import com.example.myapplicationf.ui.theme.HeaderIcon
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

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
            onNavigateToDashboard = { currentScreen = Screen.FarmerDashboard },
            onCropSelected = { cropId -> currentScreen = Screen.CropDetail(cropId) },
            viewModel = viewModel()
        )
        Screen.SellCrop -> SellCropScreen(
            onBackPressed = { currentScreen = Screen.Marketplace },
            viewModel = viewModel()
        )
        Screen.FarmerDashboard -> FarmerDashboardScreen(
            onBackPressed = { currentScreen = Screen.Marketplace },
            viewModel = viewModel()
        )
        Screen.AboutUs -> AboutUsScreen(
            onBackPressed = { currentScreen = Screen.Dashboard },
            viewModel = viewModel()
        )
        is Screen.CropDetail -> {
            val cropId = (currentScreen as Screen.CropDetail).cropId
            val marketplaceViewModel: MarketplaceViewModel = viewModel()
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
    object AboutUs : Screen()
    object FarmerDashboard : Screen()
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
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Farm App Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Divider()
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "AI Chat") },
                    label = { Text("AI Chat") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.Chat)
                        }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.WbSunny, contentDescription = "Weather") },
                    label = { Text("Weather") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.Weather)
                        }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Protection") },
                    label = { Text("Crop Protection") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.Protection)
                        }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Article, contentDescription = "News") },
                    label = { Text("Community News") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.News)
                        }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                    label = { Text("About Us") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.AboutUs)
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("", color = HeaderText) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = HeaderBackground,
                        titleContentColor = HeaderText
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = HeaderIcon
                            )
                        }
                    },
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
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "Good Afternoon,",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "User",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Agriculture,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Your one-stop solution for agricultural marketplace and farming assistance",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                error?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                NavigationBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = true,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Marketplace") },
                        label = { Text("Marketplace") },
                        selected = false,
                        onClick = { onNavigate(Screen.Marketplace) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.WbSunny, contentDescription = "Weather") },
                        label = { Text("Weather") },
                        selected = false,
                        onClick = { onNavigate(Screen.Weather) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                        label = { Text("Chat") },
                        selected = false,
                        onClick = { onNavigate(Screen.Chat) }
                    )
                }
            }
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
        "About" -> Icons.Default.Info
        else -> Icons.Default.Info
    }
}