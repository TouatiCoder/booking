package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.TextLight
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.repository.StaysRepository
import com.example.ui.localization.Localization
import com.example.ui.screens.*
import com.example.ui.theme.ZelligeStaysTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.rememberPermissionState
import android.os.Build

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Retrieve central business logic repository singleton
        val repository = StaysRepository.getInstance(applicationContext)

        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationPermissionState = rememberPermissionState(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                LaunchedEffect(Unit) {
                    if (!notificationPermissionState.status.isGranted) {
                        notificationPermissionState.launchPermissionRequest()
                    }
                }
            }
            val currentLang by repository.currentLanguageState.collectAsState()
            val isFirstLaunch by repository.isFirstLaunchState.collectAsState()
            
            // Dynamic RTL direction selector based on chosen language
            val layoutDirection = Localization.getLayoutDirection(currentLang)

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                ZelligeStaysTheme {
                    val navController = rememberNavController()

                    // Visitor login redirect cache
                    var pendingBookingPropertyId by remember { mutableStateOf<String?>(null) }

                    val startDestination = "splash" // Always show premium splash first

                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. Animated Splash
                        composable("splash") {
                            SplashScreen(
                                repository = repository,
                                onSplashFinished = {
                                    navController.navigate("main") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Central Bottom Navigation Container
                        composable("main") {
                            MainTabContainer(
                                repository = repository,
                                onNavigateToDetails = { propId ->
                                    navController.navigate("details/$propId")
                                },
                                onNavigateToSearchByCity = { cityName ->
                                    navController.navigate("search_results/$cityName")
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login_register")
                                },
                                onNavigateToRegister = {
                                    navController.navigate("login_register")
                                },
                                onNavigateToHostDashboard = {
                                    navController.navigate("host_dashboard")
                                },
                                onNavigateToAdminDashboard = {
                                    navController.navigate("admin_dashboard")
                                },
                                onNavigateToMessages = {
                                    val user = repository.currentUserState.value
                                    if (user == null) {
                                        navController.navigate("login_register")
                                    } else {
                                        navController.navigate("messages_list")
                                    }
                                },
                                onNavigateToGuides = { navController.navigate("guides") }
                            )
                        }

                        // 4. Drill down search results from banner circular city icons
                        composable(
                            route = "search_results/{cityName}",
                            arguments = listOf(navArgument("cityName") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val cName = backStackEntry.arguments?.getString("cityName") ?: ""
                            SearchScreen(
                                repository = repository,
                                initialCitySearch = cName,
                                onNavigateToDetails = { propId ->
                                    navController.navigate("details/$propId")
                                }
                            )
                        }

                        // 5. Property Details View
                        composable(
                            route = "details/{propertyId}",
                            arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val propId = backStackEntry.arguments?.getString("propertyId") ?: ""
                            PropertyDetailsScreen(
                                repository = repository,
                                propertyId = propId,
                                onNavigateToBooking = { targetPropId ->
                                    val user = repository.currentUserState.value
                                    if (user == null) {
                                        // Intercept uninvited visitor, store booking target, force Login
                                        pendingBookingPropertyId = targetPropId
                                        navController.navigate("login_register")
                                    } else {
                                        // Grant authenticated check-out desk
                                        navController.navigate("booking/$targetPropId")
                                    }
                                },
                                onContactHost = { targetPropId, hostId ->
                                    val user = repository.currentUserState.value
                                    if (user == null) {
                                        navController.navigate("login_register")
                                    } else {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val convId = repository.createConversation(targetPropId, user.email, hostId)
                                            if (convId != null) {
                                                navController.navigate("message_thread/$convId/Messages")
                                            }
                                        }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 6. Secure Booking check-out Desk
                        composable(
                            route = "booking/{propertyId}",
                            arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val propId = backStackEntry.arguments?.getString("propertyId") ?: ""
                            ReservationScreen(
                                repository = repository,
                                propertyId = propId,
                                onSuccess = {
                                    // Direct to Reservations view tab
                                    navController.navigate("main") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                },
                                onCancel = { navController.popBackStack() }
                            )
                        }

                        // 7. Login and Register Flow
                        composable("login_register") {
                            var isLoginView by remember { mutableStateOf(true) }
                            
                            val onSuccessAuthCallback = {
                                val destination = pendingBookingPropertyId
                                if (destination != null) {
                                    pendingBookingPropertyId = null
                                    // Return automatically to booked stays configuration
                                    navController.navigate("booking/$destination") {
                                        popUpTo("login_register") { inclusive = true }
                                    }
                                } else {
                                    // Return to profile tab
                                    navController.navigate("main") {
                                        popUpTo("login_register") { inclusive = true }
                                    }
                                }
                            }

                            if (isLoginView) {
                                LoginScreen(
                                    repository = repository,
                                    onSuccessAuth = onSuccessAuthCallback,
                                    onNavigateToRegister = { isLoginView = false },
                                    onBack = { navController.popBackStack() }
                                )
                            } else {
                                RegisterScreen(
                                    repository = repository,
                                    onSuccessAuth = onSuccessAuthCallback,
                                    onNavigateToLogin = { isLoginView = true },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }

                        // Messages Routing
                        composable("messages_list") {
                            com.example.ui.screens.ConversationsListScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() },
                                onConversationSelected = { convId, title ->
                                    val safeTitle = android.net.Uri.encode(title)
                                    navController.navigate("message_thread/$convId/$safeTitle")
                                }
                            )
                        }

                        // Guide Routing
                        composable("guides") {
                            com.example.ui.screens.TourGuidesScreen(
                                repository = repository,
                                onNavigateToDetails = { guideId ->
                                    navController.navigate("guide_details/$guideId")
                                }
                            )
                        }

                        composable(
                            route = "guide_details/{guideId}",
                            arguments = listOf(navArgument("guideId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val guideId = backStackEntry.arguments?.getString("guideId") ?: ""
                            com.example.ui.screens.GuideDetailsScreen(
                                repository = repository,
                                guideId = guideId,
                                onContactGuide = { gId, guideUserId ->
                                    val user = repository.currentUserState.value
                                    if (user == null) {
                                        navController.navigate("login_register")
                                    } else {
                                        // TODO: Message thread logic for guides if requested. Right now just open a dummy or use existing logic.
                                    }
                                },
                                onBookGuide = { gId ->
                                    val user = repository.currentUserState.value
                                    if (user == null) {
                                        navController.navigate("login_register")
                                    } else {
                                        navController.navigate("book_guide/$gId")
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "book_guide/{guideId}",
                            arguments = listOf(navArgument("guideId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val guideId = backStackEntry.arguments?.getString("guideId") ?: ""
                            com.example.ui.screens.GuideBookingScreen(
                                repository = repository,
                                guideId = guideId,
                                onSuccess = {
                                    navController.popBackStack()
                                    // nav to main or guides
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            "message_thread/{convId}/{title}",
                            arguments = listOf(
                                androidx.navigation.navArgument("convId") { type = androidx.navigation.NavType.StringType },
                                androidx.navigation.navArgument("title") { type = androidx.navigation.NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val convId = backStackEntry.arguments?.getString("convId") ?: ""
                            val title = android.net.Uri.decode(backStackEntry.arguments?.getString("title") ?: "")
                            com.example.ui.screens.MessageThreadScreen(
                                repository = repository,
                                conversationId = convId,
                                title = title,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 8. Host Command Center
                        composable("host_dashboard") {
                            HostDashboardScreen(
                                repository = repository,
                                onNavigateToAddProperty = { navController.navigate("add_property") },
                                onNavigateToManageProperties = { navController.navigate("manage_properties") },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 9. Host add stay listing
                        composable("add_property") {
                            AddPropertyScreen(
                                repository = repository,
                                onSuccess = {
                                    navController.navigate("host_dashboard") {
                                        popUpTo("host_dashboard") { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 10. Host inventory manager
                        composable("manage_properties") {
                            ManagePropertiesScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 11. Platform central administration console
                        composable("admin_dashboard") {
                            AdminDashboardScreen(
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// CENTRAL BOTTOM TAB BAR NAV CONTAINER
// ==========================================
@Composable
fun MainTabContainer(
    repository: StaysRepository,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToSearchByCity: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToHostDashboard: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onNavigateToMessages: () -> Unit = {},
    onNavigateToGuides: () -> Unit = {}
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    var currentSelectedTab by remember { mutableStateOf("home") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("app_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = currentSelectedTab == "home",
                    onClick = { currentSelectedTab = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Explore", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = com.example.ui.theme.LuxuryGold, indicatorColor = com.example.ui.theme.LuxuryDarkBlue.copy(alpha = 0.1f)),
                    modifier = Modifier.testTag("tab_home")
                )
                NavigationBarItem(
                    selected = currentSelectedTab == "search",
                    onClick = { currentSelectedTab = "search" },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Search", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = com.example.ui.theme.LuxuryGold, indicatorColor = com.example.ui.theme.LuxuryDarkBlue.copy(alpha = 0.1f)),
                    modifier = Modifier.testTag("tab_search")
                )
                NavigationBarItem(
                    selected = currentSelectedTab == "favorites",
                    onClick = { currentSelectedTab = "favorites" },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("Saved", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = com.example.ui.theme.LuxuryGold, indicatorColor = com.example.ui.theme.LuxuryDarkBlue.copy(alpha = 0.1f)),
                    modifier = Modifier.testTag("tab_favorites")
                )
                NavigationBarItem(
                    selected = currentSelectedTab == "profile",
                    onClick = { currentSelectedTab = "profile" },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = com.example.ui.theme.LuxuryGold, indicatorColor = com.example.ui.theme.LuxuryDarkBlue.copy(alpha = 0.1f)),
                    modifier = Modifier.testTag("tab_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentSelectedTab) {
                "home" -> HomeScreen(
                    repository = repository,
                    onNavigateToDetails = onNavigateToDetails,
                    onNavigateToSearch = onNavigateToSearchByCity,
                    onNavigateToSearchTab = { currentSelectedTab = "search" },
                    onNavigateToGuides = onNavigateToGuides
                )
                "search" -> SearchScreen(
                    repository = repository,
                    onNavigateToDetails = onNavigateToDetails
                )
                "favorites" -> {
                    val user = repository.currentUserState.value
                    if (user == null) {
                        // Secure redirect favorites if visitor attempts entries
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = com.example.ui.theme.LuxuryGold)
                                    Text("Authentication Required", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
                                    Text("Sign In to view and configure favorited accommodations.", textAlign = TextAlign.Center, color = TextLight, modifier = Modifier.padding(bottom = 16.dp))
                                    Button(onClick = onNavigateToLogin, colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.LuxuryDarkBlue)) {
                                        Text("Log In Now", color = Color.White)
                                    }
                                }
                            }
                        }
                    } else {
                        FavoritesScreen(
                            repository = repository,
                            onNavigateToDetails = onNavigateToDetails
                        )
                    }
                }
                "profile" -> ProfileScreen(
                    repository = repository,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                    onNavigateToReservations = {
                        currentSelectedTab = "bookings_shortcut"
                    },
                    onNavigateToFavorites = { currentSelectedTab = "favorites" },
                    onNavigateToHostDashboard = onNavigateToHostDashboard,
                    onNavigateToAdminDashboard = onNavigateToAdminDashboard,
                    onNavigateToMessages = onNavigateToMessages
                )
                
                // Bookings view tab shortcut
                "bookings_shortcut" -> {
                    MyReservationsScreen(repository = repository)
                }
            }
        }
    }
}
