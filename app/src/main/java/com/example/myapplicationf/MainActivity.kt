package com.example.myapplicationf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import com.example.myapplicationf.features.about.AboutUsScreen
import com.example.myapplicationf.features.about.AboutUsViewModel

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
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentScreen == Screen.Dashboard,
                    onClick = { currentScreen = Screen.Dashboard }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Marketplace") },
                    label = { Text("Marketplace") },
                    selected = currentScreen == Screen.Marketplace || currentScreen is Screen.CropDetail || currentScreen == Screen.SellCrop,
                    onClick = { currentScreen = Screen.Marketplace }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.WbSunny, contentDescription = "Weather") },
                    label = { Text("Weather") },
                    selected = currentScreen == Screen.Weather,
                    onClick = { currentScreen = Screen.Weather }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("Chat") },
                    selected = currentScreen == Screen.Chat,
                    onClick = { currentScreen = Screen.Chat }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
    when (currentScreen) {
        Screen.Dashboard -> {
            FarmDashboard(
                onNavigate = { screen -> currentScreen = screen },
                        onSignOut = { AuthHelper.signOut() }
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
                Screen.AboutUs -> {
                    AboutUsScreen(
                        onBackPressed = { currentScreen = Screen.Dashboard },
                        viewModel = viewModel()
                    )
                }
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Us", color = Color(0xFF1B5E20)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1B5E20)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF1F8E9),
                border = BorderStroke(1.dp, Color(0xFFAED581))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Farm Marketplace",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "We are dedicated to revolutionizing agriculture through technology. Our platform connects farmers with buyers, provides essential farming tools, and builds a supportive agricultural community.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF2E7D32)
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Features:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeatureItem(
                            icon = Icons.Default.ShoppingCart,
                            text = "Direct marketplace for agricultural products"
                        )
                        FeatureItem(
                            icon = Icons.Default.Security,
                            text = "Crop protection and disease management"
                        )
                        FeatureItem(
                            icon = Icons.Default.People,
                            text = "Community support and knowledge sharing"
                        )
                        FeatureItem(
                            icon = Icons.Default.WbSunny,
                            text = "Weather updates and forecasting"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF2E7D32)
            )
        )
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
    data class CropDetail(val cropId: String) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDashboard(
    viewModel: DashboardViewModel = viewModel(),
    onNavigate: (Screen) -> Unit,
    onSignOut: () -> Unit
) {
    val error by viewModel.error.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember {
        when (currentHour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }
    
    // Get current user's name
    var userName by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        
        auth.currentUser?.let { user ->
            try {
                // Get name from Firestore
                val userDoc = db.collection("users").document(user.uid).get().await()
                userName = userDoc.getString("firstName") ?: ""
                if (userName.isEmpty()) {
                    // Fallback to Firebase Auth display name if Firestore name is empty
                    userName = user.displayName?.split(" ")?.firstOrNull() ?: "User"
                }
            } catch (e: Exception) {
                // Fallback to Firebase Auth display name if Firestore fails
                userName = user.displayName?.split(" ")?.firstOrNull() ?: "User"
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1B5E20),
                                    Color(0xFF2E7D32)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(2.dp, Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(48.dp),
                                tint = Color(0xFF1B5E20)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "$greeting,",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Enhanced Menu Items
                NavigationDrawerItem(
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Market Place",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF2E7D32)
                        )
                    },
                    label = { Text("Market Place") },
                    selected = false,
                    onClick = { 
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.Marketplace)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    )
                )
                
                NavigationDrawerItem(
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Crop Protection",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF2E7D32)
                        )
                    },
                    label = { Text("Crop Protection") },
                    selected = false,
                    onClick = { 
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.Protection)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    )
                )
                
                NavigationDrawerItem(
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Community Support",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF2E7D32)
                        )
                    },
                    label = { Text("Community Support") },
                    selected = false,
                    onClick = { 
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.News)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    )
                )

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = Color(0xFFE8F5E9)
                )

                NavigationDrawerItem(
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Us",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF2E7D32)
                        )
                    },
                    label = { Text("About Us") },
                    selected = false,
                    onClick = { 
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.AboutUs)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    )
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color(0xFF1B5E20)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = Color(0xFF1B5E20)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF1B5E20)
                    )
                )
            }
        ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
        ) {
            error?.let { errorMsg ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Text(
                    text = "$greeting,",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "User",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF1F8E9),
                    border = BorderStroke(1.dp, Color(0xFFAED581))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Your one-stop solution for agricultural marketplace and farming assistance",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF1B5E20)
                            )
                        )
                    }
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
        else -> Icons.Default.Info
    }
}