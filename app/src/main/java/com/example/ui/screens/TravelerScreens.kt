package com.example.ui.screens

import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.flow.flowOf
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.dto.ReviewDto

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.StaysRepository
import com.example.ui.localization.Localization
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// 1. HOME SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: StaysRepository,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToSearch: (String) -> Unit, // Direct search by city click
    onNavigateToSearchTab: () -> Unit,
    onNavigateToGuides: () -> Unit = {}
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val properties by repository.getActiveProperties().collectAsState(initial = emptyList())
    val allCities by repository.getAllCities().collectAsState(initial = emptyList())
    val cities = allCities.filter { it.isActive && it.isFeatured }

    val user by repository.currentUserState.collectAsState()
    val isFirstLaunch by repository.isFirstLaunchState.collectAsState()
    val branding by repository.brandingState.collectAsState()

    val appLogoUrl = branding["app_logo"]?.takeIf { it.isNotBlank() }
    val primaryColorHex = branding["primary_color"]
    val dynamicGold = if (!primaryColorHex.isNullOrBlank()) {
        try { Color(android.graphics.Color.parseColor(primaryColorHex)) } catch (e: Exception) { LuxuryGold }
    } else LuxuryGold

    val secondaryColorHex = branding["secondary_color"]
    val dynamicBg = if (!secondaryColorHex.isNullOrBlank()) {
        try { Color(android.graphics.Color.parseColor(secondaryColorHex)) } catch (e: Exception) { LuxuryDarkBlue }
    } else LuxuryDarkBlue

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Riad", "Kasbah", "Villa", "Camp", "Apartment")
    
    if (isFirstLaunch) {
        LanguageSelectionDialog(
            repository = repository,
            onLanguageSelected = {} // Internal logic already updates repo
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(LuxurySand)
    ) {
        // Moroccan Brand Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(dynamicBg)
                .padding(top = 24.dp, bottom = 40.dp)
        ) {
            ZelligeBackdropPattern(dynamicGold)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ahlal Wa Sahlan" + if (user != null) ", ${user?.fullName?.split(" ")?.firstOrNull()}" else "",
                            style = MaterialTheme.typography.titleMedium,
                            color = dynamicGold.copy(alpha = 0.8f) // replaced LuxuryLightGold
                        )
                        Text(
                            text = Localization.get("discover", currentLang),
                            style = MaterialTheme.typography.displayMedium,
                            color = LuxuryWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                    }

                    if (appLogoUrl != null) {
                        coil.compose.AsyncImage(
                            model = appLogoUrl,
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(26.dp))
                        )
                    } else {
                        MoroccanStarIcon(
                            modifier = Modifier.size(52.dp),
                            color = dynamicGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Faux Search Bar (Launches Search Screen)
                Card(
                    onClick = onNavigateToSearchTab,
                    colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("home_search_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = Localization.get("search_placeholder", currentLang),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 1. Categories Row
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = Localization.get("categories", currentLang),
                style = MaterialTheme.typography.titleLarge,
                color = LuxuryDarkBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LuxuryGold,
                            selectedLabelColor = LuxuryDarkBlue,
                            containerColor = LuxuryWhite,
                            labelColor = TextDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = BorderLight,
                            selectedBorderColor = LuxuryGold,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        )
                    )
                }
            }
        }

        // 2. Featured Destinations Circular Row
        if (cities.isNotEmpty()) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = Localization.get("featured_destinations", currentLang),
                    style = MaterialTheme.typography.titleLarge,
                    color = LuxuryDarkBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cities) { city ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onNavigateToSearch(city.name_en) }
                                .testTag("city_card_${city.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, LuxuryGold, CircleShape)
                            ) {
                                AsyncImage(
                                    model = city.imageUrl,
                                    contentDescription = city.getLocalizedName(currentLang),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Dark overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.15f))
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = city.getLocalizedName(currentLang),
                                style = MaterialTheme.typography.labelLarge,
                                color = TextDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Explore",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLight
                            )
                        }
                    }
                }
            }
        }

        // Tour Guides Banner
        Card(
            onClick = onNavigateToGuides,
            colors = CardDefaults.cardColors(containerColor = LuxuryDarkBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Find a Tour Guide", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Explore cities with local experts.", color = Color.LightGray, fontSize = 14.sp)
                }
            }
        }

        // 3. Special Offers Grid
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = Localization.get("special_offers", currentLang),
                style = MaterialTheme.typography.titleLarge,
                color = LuxuryDarkBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)
            )

            val filteredProperties = properties.filter {
                (selectedCategory == "All" || it.propertyType.equals(selectedCategory, ignoreCase = true))
            }

            if (filteredProperties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No accommodations listing under $selectedCategory", color = TextLight)
                }
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    filteredProperties.forEach { prop ->
                        AccommodationLargeCard(
                            property = prop,
                            repository = repository,
                            onClick = { onNavigateToDetails(prop.id) }
                        )
                    }
                }
            }
            
            // Decorative green star badge footer
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(LuxuryZelligeGreen.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .border(1.dp, LuxuryZelligeGreen.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite, // star replacement
                    contentDescription = null,
                    tint = LuxuryZelligeGreen,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = Localization.get("moroccan_star", currentLang),
                        style = MaterialTheme.typography.labelLarge,
                        color = LuxuryZelligeGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Every zellige stay conforms to strict standards of local arts preservation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDark
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================
// 2. PROPERTY LARGE CARD COMPONENT
// ==========================================
@Composable
fun AccommodationLargeCard(
    property: PropertyEntity,
    repository: StaysRepository,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val user by repository.currentUserState.collectAsState()
    
    // Dynamic favorite state
    var isFav by remember { mutableStateOf(false) }
    
    LaunchedEffect(user, property.id) {
        val email = user?.email
        if (email != null) {
            isFav = repository.isFavorite(email, property.id)
        }
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, LuxuryDarkBlue.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("property_card_${property.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val firstPic = property.imageUrls.split(",").firstOrNull() ?: ""
                AsyncImage(
                    model = firstPic,
                    contentDescription = property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Type Badge overlay
                Box(
                    modifier = Modifier
                        .padding(14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LuxuryDarkBlue.copy(alpha = 0.8f))
                        .border(1.dp, LuxuryGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = property.propertyType,
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxuryGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Favorite button overlay (Visitable without crash, enforces login if visitor clicks)
                IconButton(
                    onClick = {
                        scope.launch {
                            val email = user?.email
                            if (email != null) {
                                repository.toggleFavorite(email, property.id)
                                isFav = !isFav
                            } else {
                                // Handled in navigation context, triggers Login redirect
                                repository.toggleFavorite("guest", property.id) // Fallback or notification
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .size(38.dp)
                        .align(Alignment.TopEnd)
                        .testTag("favorite_button_${property.id}")
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) Color.Red else LuxuryDarkBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Price tag overlay at the bottom-right corner over a translucent gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )
                
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${property.price}",
                        color = LuxuryGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " / night",
                        color = LuxuryWhite,
                        fontSize = 12.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = property.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextDark,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = String.format("%.2f", property.rating),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${property.address}, ${property.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. SEARCH & ADVANCED FILTERS SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    repository: StaysRepository,
    initialCitySearch: String = "",
    onNavigateToDetails: (String) -> Unit
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val properties by repository.getActiveProperties().collectAsState(initial = emptyList())
    
    var searchCity by remember { mutableStateOf(initialCitySearch) }
    var selectedType by remember { mutableStateOf("Any") }
    var selectedMinPrice by remember { mutableStateOf(0f) }
    var selectedMaxPrice by remember { mutableStateOf(500f) }
    var selectedMinRating by remember { mutableStateOf(4.0f) }

    var isFilterDialogShown by remember { mutableStateOf(false) }

    val propertyTypes = listOf("Any", "Riad", "Kasbah", "Villa", "Camp", "Apartment")

    // Filter properties dynamically
    val filteredResults = properties.filter { prop ->
        val matchCity = searchCity.isBlank() || prop.city.contains(searchCity, ignoreCase = true) || prop.address.contains(searchCity, ignoreCase = true)
        val matchType = selectedType == "Any" || prop.propertyType.equals(selectedType, ignoreCase = true)
        val matchPrice = prop.price >= selectedMinPrice && prop.price <= selectedMaxPrice
        val matchRating = prop.rating >= selectedMinRating
        matchCity && matchType && matchPrice && matchRating
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .background(LuxuryDarkBlue)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = Localization.get("search_screen", currentLang),
                    style = MaterialTheme.typography.displayMedium,
                    color = LuxuryGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchCity,
                        onValueChange = { searchCity = it },
                        placeholder = { Text(Localization.get("search_placeholder", currentLang), color = LuxuryLightGold) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LuxuryGold) },
                        trailingIcon = {
                            if (searchCity.isNotEmpty()) {
                                IconButton(onClick = { searchCity = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = LuxuryGold)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = LuxuryGold.copy(alpha = 0.5f),
                            focusedBorderColor = LuxuryGold,
                            focusedTextColor = LuxuryWhite,
                            unfocusedTextColor = LuxuryWhite
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_input_field")
                    )

                    // Filters Icon
                    IconButton(
                        onClick = { isFilterDialogShown = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(LuxuryGold)
                            .size(52.dp)
                            .testTag("open_filters_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Search Filter",
                            tint = LuxuryDarkBlue
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LuxurySand)
                .padding(innerPadding)
        ) {
            if (filteredResults.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No accommodation",
                        tint = TextLight,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No accommodations found",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try adjusting your price range or search query.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLight,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredResults) { prop ->
                        AccommodationLargeCard(
                            property = prop,
                            repository = repository,
                            onClick = { onNavigateToDetails(prop.id) }
                        )
                    }
                }
            }

            // Filters Dialog Window
            if (isFilterDialogShown) {
                AlertDialog(
                    onDismissRequest = { isFilterDialogShown = false },
                    confirmButton = {
                        Button(
                            onClick = { isFilterDialogShown = false },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue)
                        ) {
                            Text(Localization.get("apply_filters", currentLang), color = LuxuryWhite)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                selectedType = "Any"
                                selectedMinPrice = 0f
                                selectedMaxPrice = 500f
                                selectedMinRating = 4.0f
                                isFilterDialogShown = false
                            }
                        ) {
                            Text(Localization.get("clear_all", currentLang), color = LuxuryDarkBlue)
                        }
                    },
                    title = {
                        Text(
                            text = Localization.get("filter", currentLang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryDarkBlue
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Category Pick
                            Text(Localization.get("property_type", currentLang), fontWeight = FontWeight.Bold, color = TextDark)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(propertyTypes) { type ->
                                    val isSel = selectedType == type
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedType = type },
                                        label = { Text(type) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LuxuryGold,
                                            selectedLabelColor = LuxuryDarkBlue
                                        )
                                    )
                                }
                            }

                            // Price Limits
                            Text("${Localization.get("price_range", currentLang)}: $${selectedMinPrice.toInt()} - $${selectedMaxPrice.toInt()}", fontWeight = FontWeight.Bold, color = TextDark)
                            RangeSlider(
                                value = selectedMinPrice..selectedMaxPrice,
                                onValueChange = { range ->
                                    selectedMinPrice = range.start
                                    selectedMaxPrice = range.endInclusive
                                },
                                valueRange = 0f..500f,
                                colors = SliderDefaults.colors(
                                    thumbColor = LuxuryGold,
                                    activeTrackColor = LuxuryGold,
                                    inactiveTrackColor = BorderLight
                                )
                            )

                            // Minimum Rating
                            Text("${Localization.get("rating_min", currentLang)}: ${String.format("%.1f", selectedMinRating)} ★", fontWeight = FontWeight.Bold, color = TextDark)
                            Slider(
                                value = selectedMinRating,
                                onValueChange = { selectedMinRating = it },
                                valueRange = 4.0f..5.0f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = LuxuryGold,
                                    activeTrackColor = LuxuryGold
                                )
                            )
                        }
                    },
                    containerColor = CardBackground,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

// ==========================================
// 4. PROPERTY DETAILS SCREEN
// ==========================================
@Composable
fun PropertyDetailsScreen(
    repository: StaysRepository,
    propertyId: String,
    onNavigateToBooking: (String) -> Unit,
    onContactHost: (String, String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    var property by remember { mutableStateOf<PropertyEntity?>(null) }
    
    LaunchedEffect(propertyId) {
        property = repository.getPropertyById(propertyId)
    }

    if (property == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LuxuryGold)
        }
        return
    }

    val stay = property!!
    val imageList = stay.imageUrls.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    var selectedImgIndex by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxurySand)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Image Gallery with support for selecting multiple uploaded images
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = imageList.getOrNull(selectedImgIndex) ?: "",
                    contentDescription = stay.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Navigation Floating circles
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .size(44.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LuxuryDarkBlue)
                }

                // Previous Image Arrow overlay
                if (imageList.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                selectedImgIndex = if (selectedImgIndex > 0) selectedImgIndex - 1 else imageList.size - 1
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Image", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                selectedImgIndex = if (selectedImgIndex < imageList.size - 1) selectedImgIndex + 1 else 0
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Image", tint = Color.White)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LuxuryDarkBlue.copy(alpha = 0.8f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        text = "${selectedImgIndex + 1} / ${imageList.size} Photos",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxuryWhite
                    )
                }
            }

            // Interactive Thumbnail bar for multi photos
            if (imageList.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(imageList) { index, imgUrl ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (index == selectedImgIndex) 2.dp else 1.dp,
                                    color = if (index == selectedImgIndex) LuxuryGold else Color.Gray.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedImgIndex = index }
                        ) {
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = "Thumb $index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Stays specs
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stay.propertyType,
                        style = MaterialTheme.typography.labelLarge,
                        color = LuxuryGold,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(18.dp))
                        Text(
                            text = String.format("%.1f", stay.rating) + " (${stay.totalReviews} reviews)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stay.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = LuxuryDarkBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )

                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextLight, modifier = Modifier.size(18.dp))
                    Text(
                        text = stay.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDark
                    )
                }

                Divider(color = BorderLight, modifier = Modifier.padding(vertical = 8.dp))

                // Description
                Text(
                    text = stay.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDark,
                    lineHeight = 24.sp
                )

                Divider(color = BorderLight, modifier = Modifier.padding(vertical = 16.dp))

                // Amenities Flow
                Text(
                    text = Localization.get("amenities", currentLang),
                    style = MaterialTheme.typography.titleLarge,
                    color = LuxuryDarkBlue,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                val serviceArray = stay.amenities.split(",")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    serviceArray.forEach { amen ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = LuxuryZelligeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = amen.trim(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextDark
                            )
                        }
                    }
                }

                Divider(color = BorderLight, modifier = Modifier.padding(vertical = 16.dp))

                // Map Location Section using OSMDroid
                Text(
                    text = "Location on Map",
                    style = MaterialTheme.typography.titleLarge,
                    color = LuxuryDarkBlue,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    OSMMapView(
                        latitude = stay.latitude,
                        longitude = stay.longitude,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Divider(color = BorderLight, modifier = Modifier.padding(vertical = 16.dp))

                // Host Segment
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(LuxuryGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stay.hostEmail.take(2).uppercase(),
                                color = LuxuryDarkBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Hosted by: ${stay.hostEmail}", style = MaterialTheme.typography.titleMedium, color = TextDark, fontWeight = FontWeight.Bold)
                            Text(text = "Zellige Premium Elite Partner", style = MaterialTheme.typography.bodySmall, color = LuxuryZelligeGreen)
                        }
                        IconButton(
                            onClick = { onContactHost(stay.id, stay.hostEmail) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(LuxuryDarkBlue.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = "Contact Host", tint = LuxuryDarkBlue)
                        }
                    }
                }

                Divider(color = BorderLight, modifier = Modifier.padding(vertical = 16.dp))

                // Reviews Section
                PropertyReviewsSection(repository, propertyId)

                Spacer(modifier = Modifier.height(100.dp)) // Padding for floating footer
            }
        }

        // Floating Footer for booking
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total per night", style = MaterialTheme.typography.bodySmall, color = TextLight)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "$${stay.price}", style = MaterialTheme.typography.displayMedium, color = LuxuryDarkBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(text = " USD")
                    }
                }

                Button(
                    onClick = { onNavigateToBooking(stay.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .widthIn(min = 160.dp)
                        .testTag("book_now_button")
                ) {
                    Text(
                        text = Localization.get("book_now", currentLang),
                        color = LuxuryDarkBlue,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. RESERVATION SCREEN
// ==========================================
@Composable
fun ReservationScreen(
    repository: StaysRepository,
    propertyId: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val currentUser by repository.currentUserState.collectAsState()
    val scope = rememberCoroutineScope()

    var property by remember { mutableStateOf<PropertyEntity?>(null) }
    
    var guests by remember { mutableStateOf(1) }
    var stayDurationNights by remember { mutableStateOf(3) } // simulated duration
    
    var checkInDate by remember { mutableStateOf(System.currentTimeMillis() + (1000L * 60 * 60 * 24)) } // Tomorrow
    var checkOutDate by remember { mutableStateOf(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 4)) } // 4 days later

    LaunchedEffect(propertyId) {
        property = repository.getPropertyById(propertyId)
    }

    if (property == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LuxuryGold)
        }
        return
    }

    val stay = property!!
    val totalCost = stay.price * stayDurationNights

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxurySand)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = LuxuryDarkBlue)
                }
                Text(
                    text = Localization.get("confirm_booking", currentLang),
                    style = MaterialTheme.typography.titleLarge,
                    color = LuxuryDarkBlue,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stay brief summary
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    val pic = stay.imageUrls.split(",").firstOrNull() ?: ""
                    AsyncImage(
                        model = pic,
                        contentDescription = stay.title,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = stay.title, style = MaterialTheme.typography.titleMedium, color = TextDark, fontWeight = FontWeight.Bold)
                        Text(text = stay.city, style = MaterialTheme.typography.bodySmall, color = TextLight)
                        Text(text = "$${stay.price} ${Localization.get("price_per_night", currentLang)}", style = MaterialTheme.typography.labelLarge, color = LuxuryGold, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Booking Form Fields
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Dates simulated selection
                    Text(Localization.get("check_in", currentLang), fontWeight = FontWeight.Bold, color = TextDark)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                            .background(LuxurySand)
                            .padding(12.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = LuxuryGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(checkInDate)), color = TextDark)
                    }

                    Text(Localization.get("check_out", currentLang), fontWeight = FontWeight.Bold, color = TextDark)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                            .background(LuxurySand)
                            .padding(12.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = LuxuryGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(checkOutDate)), color = TextDark)
                    }

                    // Guests count slider structure
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Localization.get("guests", currentLang), fontWeight = FontWeight.Bold, color = TextDark)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(onClick = { if (guests > 1) guests-- }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Less", tint = LuxuryGold)
                            }
                            Text(text = "$guests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDark)
                            IconButton(onClick = { guests++ }) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "More", tint = LuxuryGold)
                            }
                        }
                    }

                    Divider(color = BorderLight)

                    // Pricing breakdown list
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "$${stay.price} x $stayDurationNights nights", color = TextLight)
                        Text(text = "$$totalCost", color = TextDark)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Cleaning and Service fee", color = TextLight)
                        Text(text = "$0.00", color = LuxuryZelligeGreen)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = Localization.get("total_price", currentLang), fontWeight = FontWeight.Bold, color = TextDark, style = MaterialTheme.typography.titleMedium)
                        Text(text = "$$totalCost", fontWeight = FontWeight.Bold, color = LuxuryDarkBlue, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    val email = currentUser?.email
                    if (email != null) {
                        scope.launch {
                            val ok = repository.createReservation(
                                propertyId = stay.id,
                                userEmail = email,
                                checkIn = checkInDate,
                                checkOut = checkOutDate,
                                guests = guests,
                                totalPrice = totalCost
                            )
                            if (ok) {
                                com.example.ui.utils.NotificationUtils.showNotification(
                                    context,
                                    "Reservation Confirmed",
                                    "Your reservation for ${stay.title} is confirmed!"
                                )
                                com.example.ui.utils.NotificationUtils.showNotification(
                                    context,
                                    "New Reservation Received by Host",
                                    "Great news! Your property ${stay.title} has a new booking."
                                )
                                onSuccess()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_booking_btn")
            ) {
                Text(
                    text = Localization.get("confirm_booking", currentLang),
                    color = LuxuryDarkBlue,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

// ==========================================
// 6. MY RESERVATIONS SCREEN
// ==========================================
@Composable
fun MyReservationsScreen(
    repository: StaysRepository
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val currentUser by repository.currentUserState.collectAsState()
    
    val allReservations by repository.getAllReservations().collectAsState(initial = emptyList())
    val userReservations = remember(allReservations, currentUser) {
        val email = currentUser?.email
        if (email != null) {
            allReservations.filter { it.userEmail == email }
        } else {
            emptyList()
        }
    }

    var activeTab by remember { mutableStateOf("Upcoming") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxurySand)
            .padding(16.dp)
    ) {
        Text(
            text = Localization.get("reservations", currentLang),
            style = MaterialTheme.typography.displayMedium,
            color = LuxuryDarkBlue,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom tabs row for segregrating Upcoming vs. Completed
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { activeTab = "Upcoming" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "Upcoming") LuxuryGold else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = Localization.get("upcoming", currentLang),
                        color = if (activeTab == "Upcoming") LuxuryDarkBlue else TextLight,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { activeTab = "Completed" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "Completed") LuxuryGold else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = Localization.get("completed", currentLang),
                        color = if (activeTab == "Completed") LuxuryDarkBlue else TextLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val pageSelection = userReservations.filter { it.status.equals(activeTab, ignoreCase = true) }

        if (pageSelection.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Luggage, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextLight)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = Localization.get("no_reservations", currentLang),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextLight,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                pageSelection.forEach { res ->
                    var stayDetails by remember { mutableStateOf<PropertyEntity?>(null) }
                    
                    LaunchedEffect(res.propertyId) {
                        stayDetails = repository.getPropertyById(res.propertyId)
                    }

                    if (stayDetails != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row {
                                    val pic = stayDetails?.imageUrls?.split(",")?.firstOrNull() ?: ""
                                    AsyncImage(
                                        model = pic,
                                        contentDescription = stayDetails?.title,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = stayDetails?.title ?: "", style = MaterialTheme.typography.titleMedium, color = TextDark, fontWeight = FontWeight.Bold)
                                        Text(text = stayDetails?.city ?: "", style = MaterialTheme.typography.bodySmall, color = TextLight)
                                        Text(text = "Total Paid: $${res.totalPrice}", style = MaterialTheme.typography.labelLarge, color = LuxuryZelligeGreen)
                                    }
                                }

                                Divider(color = BorderLight, modifier = Modifier.padding(vertical = 12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Check In", fontSize = 11.sp, color = TextLight)
                                        Text(text = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(res.checkInDate)), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Check Out", fontSize = 11.sp, color = TextLight)
                                        Text(text = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(res.checkOutDate)), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. SAVED FAVORITES SCREEN
// ==========================================
@Composable
fun FavoritesScreen(
    repository: StaysRepository,
    onNavigateToDetails: (String) -> Unit
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val currentUser by repository.currentUserState.collectAsState()
    
    val favProperties = remember(currentUser) {
        val email = currentUser?.email
        if (email != null) {
            repository.getFavorites(email)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxurySand)
            .padding(16.dp)
    ) {
        Text(
            text = Localization.get("favorites", currentLang),
            style = MaterialTheme.typography.displayMedium,
            color = LuxuryDarkBlue,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (favProperties.value.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextLight)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = Localization.get("no_favorites", currentLang),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextLight,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                favProperties.value.forEach { prop ->
                    AccommodationLargeCard(
                        property = prop,
                        repository = repository,
                        onClick = { onNavigateToDetails(prop.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PropertyReviewsSection(repository: StaysRepository, propertyId: String) {
    val currentUser by repository.currentUserState.collectAsState()
    var reviews by remember { mutableStateOf<List<ReviewDto>>(emptyList()) }
    var averageRating by remember { mutableFloatStateOf(0f) }
    
    var canReview by remember { mutableStateOf(false) }
    var reservationId by remember { mutableStateOf<String?>(null) }
    
    var showReviewDialog by remember { mutableStateOf(false) }

    fun refreshData() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            reviews = repository.getReviews(propertyId)
            averageRating = if (reviews.isNotEmpty()) reviews.map { it.rating.toFloat() }.average().toFloat() else 0f
            if (currentUser != null) {
                val cr = repository.canReview(currentUser!!.email, propertyId)
                canReview = cr?.can_review ?: false
                reservationId = cr?.reservation_id
            }
        }
    }

    LaunchedEffect(propertyId, currentUser) {
        refreshData()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Verified Reviews", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDark)
            if (reviews.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(20.dp))
                    Text(text = String.format("%.1f", averageRating), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(text = " (${reviews.size})", style = MaterialTheme.typography.bodyMedium, color = TextLight)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (canReview && reservationId != null) {
            Button(
                onClick = { showReviewDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("Leave a Review", color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else if (reviews.isEmpty()) {
            Text("No reviews yet for this property.", color = TextLight, modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
        }

        reviews.forEach { r ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(r.user_name ?: "Traveler", fontWeight = FontWeight.Bold, color = TextDark)
                        val date = r.created_at?.take(10) ?: ""
                        Text(date, fontSize = 12.sp, color = TextLight)
                    }
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        for (i in 1..5) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = if (i <= r.rating) LuxuryGold else BorderLight, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(r.comment, color = TextDark, fontSize = 14.sp)
                }
            }
        }
    }

    if (showReviewDialog && reservationId != null && currentUser != null) {
        var rating by remember { mutableIntStateOf(5) }
        var comment by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showReviewDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Write a Review", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= rating) LuxuryGold else BorderLight,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { rating = i }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Your comment") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = BorderLight)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showReviewDialog = false }) {
                            Text("Cancel", color = TextLight)
                        }
                        Button(
                            onClick = {
                                if (comment.isNotBlank()) {
                                    isSubmitting = true
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                        val success = repository.submitReview(propertyId, currentUser!!.email, reservationId!!, rating, comment)
                                        isSubmitting = false
                                        if (success) {
                                            showReviewDialog = false
                                            refreshData()
                                        }
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold)
                        ) {
                            if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Submit", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
