package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.StaysRepository
import com.example.ui.localization.Localization
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// ==========================================
// 1. HOST DASHBOARD
// ==========================================
@Composable
fun HostDashboardScreen(
    repository: StaysRepository,
    onNavigateToAddProperty: () -> Unit,
    onNavigateToManageProperties: () -> Unit,
    onBack: () -> Unit
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val scope = rememberCoroutineScope()
    val user by repository.currentUserState.collectAsState()

    val hostProperties by repository.getPropertiesByHost(user?.email ?: "").collectAsState(initial = emptyList())
    val allReservations by repository.getAllReservations().collectAsState(initial = emptyList())

    // Filter reservations placed on this host's properties
    val hostReservations = remember(allReservations, hostProperties) {
        val propIds = hostProperties.map { it.id }.toSet()
        allReservations.filter { it.propertyId in propIds }
    }

    var subscription by remember { mutableStateOf<SubscriptionEntity?>(null) }
    
    LaunchedEffect(user) {
        val email = user?.email
        if (email != null) {
            subscription = repository.getHostSubscription(email)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxurySand)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = LuxuryDarkBlue)
            }
            Text(
                text = Localization.get("host_dashboard", currentLang),
                style = MaterialTheme.typography.titleLarge,
                color = LuxuryDarkBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Subscription Status Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryDarkBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Box {
                ZelligeBackdropPattern()
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CardMembership, contentDescription = null, tint = LuxuryGold)
                        Text(
                            text = Localization.get("sub_status", currentLang),
                            color = LuxuryWhite,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val subStatus = subscription?.status ?: "Active"
                    val isTrial = subscription?.isFreeTrial ?: true
                    
                    Text(
                        text = if (isTrial && subStatus == "Active") Localization.get("free_trial", currentLang) else "Gold Partner Membership Active",
                        color = LuxuryLightGold,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (subStatus == "Expired") {
                        Text(
                            text = Localization.get("expired", currentLang),
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    user?.email?.let { e ->
                                        repository.activateSubscription(e)
                                        subscription = repository.getHostSubscription(e)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(Localization.get("renew_sub", currentLang), color = LuxuryDarkBlue)
                        }
                    } else {
                        Text(
                            text = "Next payment due in 55 days",
                            color = LuxuryWhite.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Stats Row
        Text(
            text = Localization.get("stats", currentLang),
            style = MaterialTheme.typography.titleMedium,
            color = LuxuryDarkBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Properties Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.HomeWork, contentDescription = null, tint = LuxuryGold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${hostProperties.size}", style = MaterialTheme.typography.displayMedium, color = LuxuryDarkBlue, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    Text(text = Localization.get("total_properties", currentLang), style = MaterialTheme.typography.bodySmall, color = TextLight)
                }
            }

            // Total Reservations Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.EventAvailable, contentDescription = null, tint = LuxuryGold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${hostReservations.size}", style = MaterialTheme.typography.displayMedium, color = LuxuryDarkBlue, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    Text(text = Localization.get("total_bookings", currentLang), style = MaterialTheme.typography.bodySmall, color = TextLight)
                }
            }
        }

        // Action Buttons Grid
        Button(
            onClick = onNavigateToAddProperty,
            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = 4.dp)
                .testTag("host_add_property_btn")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = LuxuryDarkBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = Localization.get("add_property", currentLang), color = LuxuryDarkBlue, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onNavigateToManageProperties,
            colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = 4.dp)
                .testTag("host_manage_properties_btn")
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = LuxuryWhite)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = Localization.get("manage_properties", currentLang), color = LuxuryWhite, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 2. ADD PROPERTY SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyScreen(
    repository: StaysRepository,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val scope = rememberCoroutineScope()
    val user by repository.currentUserState.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var propertyType by remember { mutableStateOf("Riad") }
    var amenities by remember { mutableStateOf("Pool, Wi-Fi, Breakfast, Spa") }
    var imageUrls by remember { mutableStateOf("") }

    var latitude by remember { mutableStateOf(31.6295) }
    var longitude by remember { mutableStateOf(-7.9811) }

    var selectedLocalUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploadingImages by remember { mutableStateOf(false) }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedLocalUris = uris
    }

    val allCitiesList by repository.getAllCities().collectAsState(initial = emptyList())
    val cities = allCitiesList.filter { it.isActive }.map { city -> city.name_en }
    val types = listOf("Riad", "Villa", "Kasbah", "Apartment", "Camp")

    // Update city var safely when language changes or loads
    var city by remember { mutableStateOf("") }
    LaunchedEffect(cities) {
        if (cities.isNotEmpty() && !cities.contains(city)) {
            city = cities.first()
        }
    }

    var isCityExpanded by remember { mutableStateOf(false) }
    var isTypeExpanded by remember { mutableStateOf(false) }


    var isPublishing by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxurySand)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = LuxuryDarkBlue)
            }
            Text(
                text = Localization.get("add_property", currentLang),
                style = MaterialTheme.typography.titleLarge,
                color = LuxuryDarkBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (err != null) {
                    Text(text = err ?: "", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; err = null },
                    label = { Text(Localization.get("add_title", currentLang)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold),
                    modifier = Modifier.fillMaxWidth().testTag("add_property_title_input")
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; err = null },
                    label = { Text(Localization.get("add_desc", currentLang)) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold),
                    modifier = Modifier.fillMaxWidth()
                )

                // Select City Dropdown
                ExposedDropdownMenuBox(
                    expanded = isCityExpanded,
                    onExpandedChange = { isCityExpanded = it }
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.get("add_city", currentLang)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCityExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold)
                    )
                    ExposedDropdownMenu(
                        expanded = isCityExpanded,
                        onDismissRequest = { isCityExpanded = false }
                    ) {
                        cities.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    city = item
                                    isCityExpanded = false
                                }
                            )
                        }
                    }
                }

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; err = null },
                    label = { Text(Localization.get("add_address", currentLang)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold),
                    modifier = Modifier.fillMaxWidth()
                )

                // Price Per Night
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it; err = null },
                    label = { Text(Localization.get("add_price", currentLang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold),
                    modifier = Modifier.fillMaxWidth().testTag("add_property_price_input")
                )

                // Select Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = isTypeExpanded,
                    onExpandedChange = { isTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = propertyType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.get("add_type", currentLang)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold)
                    )
                    ExposedDropdownMenu(
                        expanded = isTypeExpanded,
                        onDismissRequest = { isTypeExpanded = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    propertyType = t
                                    isTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amenities (CSV)
                OutlinedTextField(
                    value = amenities,
                    onValueChange = { amenities = it },
                    label = { Text(Localization.get("add_amenities", currentLang)) },
                    supportingText = { Text("Separate amenities with a comma") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold),
                    modifier = Modifier.fillMaxWidth()
                )

                // Select coordinates on Map using OSMDroid
                Text(
                    text = "Select Coordinates on Map",
                    style = MaterialTheme.typography.titleMedium,
                    color = LuxuryDarkBlue,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap on the map to set the exact longitude and latitude of your stay.",
                    fontSize = 12.sp,
                    color = TextLight
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    OSMMapView(
                        latitude = latitude,
                        longitude = longitude,
                        isEditable = true,
                        onCoordinatesChanged = { lat, lng ->
                            latitude = lat
                            longitude = lng
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Multiple Photo Picker and Upload section
                Text(
                    text = "Property Images & Media (Firebase Storage)",
                    style = MaterialTheme.typography.titleMedium,
                    color = LuxuryDarkBlue,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            multiplePhotoPickerLauncher.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryZelligeGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Images", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (selectedLocalUris.isNotEmpty()) {
                        Button(
                            onClick = {
                                isUploadingImages = true
                                scope.launch {
                                    val uploadedUrls = mutableListOf<String>()
                                    selectedLocalUris.forEach { uri ->
                                        val url = repository.uploadImage(uri)
                                        if (url != null) {
                                            uploadedUrls.add(url)
                                        }
                                    }
                                    if (uploadedUrls.isNotEmpty()) {
                                        val csv = uploadedUrls.joinToString(",")
                                        imageUrls = if (imageUrls.isBlank()) csv else "$imageUrls,$csv"
                                    }
                                    isUploadingImages = false
                                }
                            },
                            enabled = !isUploadingImages,
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isUploadingImages) {
                                CircularProgressIndicator(color = LuxuryDarkBlue, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload to Cloud", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Show Selected Local Images Thumbnails
                if (selectedLocalUris.isNotEmpty()) {
                    Text(
                        text = "Local Previews (${selectedLocalUris.size} selected):",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedLocalUris) { uri ->
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Gray.copy(alpha = 0.1f))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected Local image preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Images Input Field (allows manual overrides or addition)
                OutlinedTextField(
                    value = imageUrls,
                    onValueChange = { imageUrls = it },
                    label = { Text(Localization.get("add_images", currentLang) + " (Comma-Separated URLs)") },
                    placeholder = { Text("https://image1.jpg, https://image2.jpg") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold),
                    modifier = Modifier.fillMaxWidth().testTag("add_property_images_input")
                )
                
                // Prefill images logic for testing
                Button(
                    onClick = {
                        imageUrls = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80,https://images.unsplash.com/photo-1582719508461-905c673771fd?w=600&auto=format&fit=crop&q=80"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryZelligeGreen.copy(alpha = 0.15f), contentColor = LuxuryZelligeGreen),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Auto-Prefill Real Morocco Photos", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isPublishing) {
                    CircularProgressIndicator(color = LuxuryGold, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = {
                            val priceDouble = price.toDoubleOrNull()
                            if (title.isBlank() || description.isBlank() || address.isBlank() || priceDouble == null || priceDouble <= 0) {
                                err = Localization.get("mandatory_field", currentLang)
                                return@Button
                            }
                            
                            isPublishing = true
                            scope.launch {
                                var finalImages = imageUrls
                                if (finalImages.isBlank() && selectedLocalUris.isNotEmpty()) {
                                    val uploadedUrls = mutableListOf<String>()
                                    selectedLocalUris.forEach { uri ->
                                        val url = repository.uploadImage(uri)
                                        if (url != null) {
                                            uploadedUrls.add(url)
                                        }
                                    }
                                    if (uploadedUrls.isNotEmpty()) {
                                        finalImages = uploadedUrls.joinToString(",")
                                        imageUrls = finalImages
                                    }
                                }

                                if (finalImages.isBlank()) {
                                    finalImages = "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=600&auto=format&fit=crop&q=80"
                                }

                                val ok = repository.addProperty(
                                    title = title,
                                    description = description,
                                    city = city,
                                    address = address,
                                    price = priceDouble,
                                    propertyType = propertyType,
                                    amenities = amenities,
                                    imageUrls = finalImages,
                                    hostEmail = user?.email ?: "host@zellige.com",
                                    latitude = latitude,
                                    longitude = longitude
                                )
                                isPublishing = false
                                if (ok) {
                                    onSuccess()
                                } else {
                                    err = "Publishing failed. Please try again."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_property_btn")
                    ) {
                        Text(text = Localization.get("submit_property", currentLang), color = LuxuryDarkBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

// ==========================================
// 3. MANAGE PROPERTIES SCREEN
// ==========================================
@Composable
fun ManagePropertiesScreen(
    repository: StaysRepository,
    onBack: () -> Unit
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val scope = rememberCoroutineScope()
    val user by repository.currentUserState.collectAsState()

    val hostProperties by repository.getPropertiesByHost(user?.email ?: "").collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxurySand)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = LuxuryDarkBlue)
            }
            Text(
                text = Localization.get("manage_properties", currentLang),
                style = MaterialTheme.typography.titleLarge,
                color = LuxuryDarkBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (hostProperties.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.HomeRepairService, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextLight)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No properties listed under your account yet.", color = TextLight)
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                hostProperties.forEach { prop ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            val pic = prop.imageUrls.split(",").firstOrNull() ?: ""
                            AsyncImage(
                                model = pic,
                                contentDescription = prop.title,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = prop.title, style = MaterialTheme.typography.titleMedium, color = TextDark, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(text = prop.city, style = MaterialTheme.typography.bodySmall, color = TextLight)
                                Text(text = "$${prop.price}/night", style = MaterialTheme.typography.labelLarge, color = LuxuryGold)
                            }

                            // Dynamic Delete Icon Action
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        repository.deleteProperty(prop)
                                    }
                                },
                                modifier = Modifier.testTag("delete_property_${prop.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Listing", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}
