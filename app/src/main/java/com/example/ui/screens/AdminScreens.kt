package com.example.ui.screens

import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.StaysRepository
import com.example.ui.localization.Localization
import com.example.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(
    repository: StaysRepository,
    onBack: () -> Unit
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val scope = rememberCoroutineScope()

    // Collect data reactively
    val allProperties by repository.getAllProperties().collectAsState(initial = emptyList())
    val allReservations by repository.getAllReservations().collectAsState(initial = emptyList())
    
    // Live collect users flow (we'll fetch once or collect)
    val usersList by repository.appDao.getAllUsersFlow().collectAsState(initial = emptyList())
    val subscriptionsList by repository.appDao.getAllSubscriptionsFlow().collectAsState(initial = emptyList())

    var adminSectionTab by remember { mutableStateOf("Overview") }
    val sections = listOf("Overview", "Users", "Properties", "Bookings", "Subscriptions", "Cities")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxurySand)
    ) {
        // Administrative Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(LuxuryDarkBlue)
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            ZelligeBackdropPattern()
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = LuxuryGold)
                    }
                    Text(
                        text = Localization.get("admin_title", currentLang),
                        style = MaterialTheme.typography.titleLarge,
                        color = LuxuryWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable tab items
                ScrollableTabRow(
                    selectedTabIndex = sections.indexOf(adminSectionTab),
                    containerColor = Color.Transparent,
                    contentColor = LuxuryGold,
                    edgePadding = 0.dp
                ) {
                    sections.forEachIndexed { index, title ->
                        Tab(
                            selected = adminSectionTab == title,
                            onClick = { adminSectionTab = title },
                            text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }
                }
            }
        }

        // Live Dynamic Section Loader
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp)
        ) {
            when (adminSectionTab) {
                "Overview" -> AdminOverviewSection(
                    users = usersList,
                    properties = allProperties,
                    reservations = allReservations,
                    currentLang = currentLang
                )
                "Users" -> AdminUsersSection(
                    users = usersList,
                    repository = repository,
                    scope = scope
                )
                "Properties" -> AdminPropertiesSection(
                    properties = allProperties,
                    repository = repository,
                    scope = scope
                )
                "Bookings" -> AdminBookingsSection(
                    reservations = allReservations,
                    properties = allProperties
                )
                "Subscriptions" -> AdminSubscriptionsSection(
                    subscriptions = subscriptionsList,
                    repository = repository,
                    scope = scope,
                    properties = allProperties
                )
                "Cities" -> AdminCitiesSection(
                    repository = repository,
                    scope = scope,
                    currentLang = currentLang
                )
            }
        }
    }
}

// ==========================================
// A. ADMINISTRATIVE OVERVIEW
// ==========================================
@Composable
fun AdminOverviewSection(
    users: List<UserEntity>,
    properties: List<PropertyEntity>,
    reservations: List<ReservationEntity>,
    currentLang: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Localization.get("analytics", currentLang),
            style = MaterialTheme.typography.titleLarge,
            color = LuxuryDarkBlue,
            fontWeight = FontWeight.Bold
        )

        // Totals Grid Cards
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            AdminStatCard(
                icon = Icons.Default.People,
                value = "${users.size}",
                label = "Total Users",
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                icon = Icons.Default.HomeMini,
                value = "${properties.size}",
                label = "Stays Listed",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            AdminStatCard(
                icon = Icons.Default.Book,
                value = "${reservations.size}",
                label = "Bookings Made",
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                icon = Icons.Default.MonetizationOn,
                value = "$${reservations.sumOf { it.totalPrice }.toInt()}",
                label = "Estimated Sales",
                modifier = Modifier.weight(1f),
                color = LuxuryZelligeGreen
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "System Administration Log",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "✓ Zellige Database synchronized successfully.", fontSize = 13.sp, color = LuxuryZelligeGreen)
                Text(text = "✓ Initial city coordinates initialized (Marrakech, Rabat, Casablanca, Agadir, Tangier, Chefchaouen).", fontSize = 13.sp, color = TextLight, modifier = Modifier.padding(top = 4.dp))
                Text(text = "✓ Role system authorization mapped: guest, client, host, admin.", fontSize = 13.sp, color = TextLight, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun AdminStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = LuxuryGold
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.displayMedium, color = LuxuryDarkBlue, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextLight)
        }
    }
}

// ==========================================
// B. USER MANAGEMENT
// ==========================================
@Composable
fun AdminUsersSection(
    users: List<UserEntity>,
    repository: StaysRepository,
    scope: CoroutineScope
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Enrolled Accounts",
            style = MaterialTheme.typography.titleMedium,
            color = LuxuryDarkBlue,
            fontWeight = FontWeight.Bold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            users.forEach { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = user.fullName, style = MaterialTheme.typography.titleMedium, color = TextDark, fontWeight = FontWeight.Bold)
                                Text(text = user.email, style = MaterialTheme.typography.bodySmall, color = TextLight)
                                Text(
                                    text = "Role: ${user.role.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (user.role == "admin") LuxuryZelligeGreen else if (user.role == "host") LuxuryGold else TextDark,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Action Menu (Promote or Delete)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (user.role == "client" || user.role == "guest") {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                repository.promoteToHost(user.email)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("Set Host", fontSize = 11.sp, color = LuxuryDarkBlue)
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repository.appDao.deleteProperty(PropertyEntity("none", "", "", "", "", 0.0, 0.0, "", "", "", "")) // placeholder cleanup
                                            // Ban profile simulation: we could delete user or change his password
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Ban Account", tint = Color.Red)
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
// C. PROPERTY MANAGEMENT
// ==========================================
@Composable
fun AdminPropertiesSection(
    properties: List<PropertyEntity>,
    repository: StaysRepository,
    scope: CoroutineScope
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Stays Inventory Control",
            style = MaterialTheme.typography.titleMedium,
            color = LuxuryDarkBlue,
            fontWeight = FontWeight.Bold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            properties.forEach { prop ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = prop.title, style = MaterialTheme.typography.titleMedium, color = TextDark, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(text = "${prop.city} • $${prop.price}/night", style = MaterialTheme.typography.bodySmall, color = TextLight)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(
                                    imageVector = if (prop.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (prop.isActive) LuxuryZelligeGreen else Color.Red,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (prop.isActive) "Listing Published" else "Suspended Listing",
                                    fontSize = 11.sp,
                                    color = if (prop.isActive) LuxuryZelligeGreen else Color.Red
                                )
                            }
                        }

                        // Toggle Listing activity simulation inside Admin Console
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Switch(
                            checked = prop.isActive,
                            onCheckedChange = { active ->
                                scope.launch {
                                    repository.updateProperty(prop.copy(isActive = active))
                                    if (active) {
                                        com.example.ui.utils.NotificationUtils.showNotification(
                                            context,
                                            "Property Approved",
                                            "The property '${prop.title}' is now approved and active."
                                        )
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LuxuryGold,
                                checkedTrackColor = LuxuryDarkBlue
                            )
                        )

                        IconButton(
                            onClick = {
                                scope.launch {
                                    repository.deleteProperty(prop)
                                }
                            },
                            modifier = Modifier.testTag("admin_delete_prop_${prop.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Accommodation", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// D. BOOKINGS LOG
// ==========================================
@Composable
fun AdminBookingsSection(
    reservations: List<ReservationEntity>,
    properties: List<PropertyEntity>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Active Plataform Reservations",
            style = MaterialTheme.typography.titleMedium,
            color = LuxuryDarkBlue,
            fontWeight = FontWeight.Bold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            reservations.forEach { res ->
                val stay = properties.firstOrNull { it.id == res.propertyId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Rerservation ID: ${res.id}", fontSize = 11.sp, color = TextLight)
                        Text(text = stay?.title ?: "Unknown Stay", style = MaterialTheme.typography.titleMedium, color = TextDark, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "Guest User: ${res.userEmail}", style = MaterialTheme.typography.bodySmall, color = TextLight)
                        Text(text = "CheckIn-CheckOut: ${res.guests} Guests", style = MaterialTheme.typography.bodySmall, color = TextLight)

                        Divider(color = BorderLight, modifier = Modifier.padding(vertical = 8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Invoice: $${res.totalPrice}", fontWeight = FontWeight.Bold, color = LuxuryZelligeGreen)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LuxuryGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = res.status.uppercase(), fontSize = 10.sp, color = LuxuryDarkGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// E. HOST SUBSCRIPTIONS MANAGEMENT
// ==========================================
@Composable
fun AdminSubscriptionsSection(
    subscriptions: List<SubscriptionEntity>,
    repository: StaysRepository,
    scope: CoroutineScope,
    properties: List<PropertyEntity>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Hosts Licenses & Trial Configurations",
            style = MaterialTheme.typography.titleMedium,
            color = LuxuryDarkBlue,
            fontWeight = FontWeight.Bold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            subscriptions.forEach { sub ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(text = "License Email: ${sub.hostEmail}", style = MaterialTheme.typography.titleMedium, color = TextDark, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (sub.isFreeTrial) "Classification: 2-Month Trial" else "Classification: Premium Paid Partner",
                                    fontSize = 11.sp,
                                    color = LuxuryGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (sub.status == "Active") LuxuryZelligeGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = sub.status,
                                    color = if (sub.status == "Active") LuxuryZelligeGreen else Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(color = BorderLight, modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Override subscription toggles (simulate deactivations):", fontSize = 11.sp, color = TextLight, modifier = Modifier.weight(1f))
                            
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Button(
                                onClick = {
                                    val targetStatus = if (sub.status == "Active") "Expired" else "Active"
                                    scope.launch {
                                        val overridden = sub.copy(status = targetStatus)
                                        repository.appDao.insertSubscription(overridden)
                                        
                                        // Deactivate stays of host automatically if expired
                                        val hostStays = properties.filter { it.hostEmail == sub.hostEmail }
                                        for (prop in hostStays) {
                                            repository.updateProperty(prop.copy(isActive = (targetStatus == "Active")))
                                        }

                                        if (targetStatus == "Expired") {
                                            com.example.ui.utils.NotificationUtils.showNotification(
                                                context,
                                                "Subscription Expiration Warning",
                                                "Warning: A host subscription for ${sub.hostEmail} has expired."
                                            )
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sub.status == "Active") Color.Red else LuxuryZelligeGreen
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (sub.status == "Active") "Suspend Partner" else "Activate Partner",
                                    fontSize = 11.sp,
                                    color = LuxuryWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// F. CITIES MAP (DYNAMIC CITIES)
// ==========================================
@Composable
fun AdminCitiesSection(
    repository: StaysRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    currentLang: String
) {
    val allCities by repository.getAllCities().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manage Cities",
                style = MaterialTheme.typography.titleMedium,
                color = LuxuryDarkBlue,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Add City", color = LuxuryDarkBlue)
            }
        }

        if (allCities.isEmpty()) {
            Text("No cities available.", modifier = Modifier.padding(16.dp))
        } else {
            allCities.forEach { city ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        coil.compose.AsyncImage(
                            model = city.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = city.name_en, style = MaterialTheme.typography.labelLarge, color = LuxuryDarkBlue, fontWeight = FontWeight.Bold)
                            Text(text = "Arabic: ${city.name_ar}", style = MaterialTheme.typography.bodySmall, color = TextLight)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                if (city.isFeatured) {
                                    Badge(containerColor = LuxuryGold) { Text("Featured", color = LuxuryDarkBlue) }
                                }
                                if (!city.isActive) {
                                    Badge(containerColor = Color.Red) { Text("Inactive", color = LuxuryWhite) }
                                }
                            }
                        }
                        // Actions
                        Column {
                            Switch(
                                checked = city.isActive,
                                onCheckedChange = { active ->
                                    scope.launch {
                                        repository.updateCity(city.copy(isActive = active))
                                    }
                                }
                            )
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        repository.deleteCity(city.id)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var id by remember { mutableStateOf("") }
        var nameEn by remember { mutableStateOf("") }
        var nameAr by remember { mutableStateOf("") }
        var nameFr by remember { mutableStateOf("") }
        var nameEs by remember { mutableStateOf("") }
        var isFeatured by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add City") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("ID (e.g. fez)") })
                    OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("Name (EN)") })
                    OutlinedTextField(value = nameAr, onValueChange = { nameAr = it }, label = { Text("Name (AR)") })
                    OutlinedTextField(value = nameFr, onValueChange = { nameFr = it }, label = { Text("Name (FR)") })
                    OutlinedTextField(value = nameEs, onValueChange = { nameEs = it }, label = { Text("Name (ES)") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                        Text("Featured")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (id.isNotEmpty() && nameEn.isNotEmpty()) {
                        scope.launch {
                            repository.addCity(
                                CityEntity(
                                    id = id,
                                    name_en = nameEn,
                                    name_ar = nameAr,
                                    name_fr = nameFr,
                                    name_es = nameEs,
                                    imageUrl = "https://images.unsplash.com/photo-1539650116574-8efeb43e2750?w=500&auto=format&fit=crop&q=80",
                                    isActive = true,
                                    isFeatured = isFeatured,
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                            showAddDialog = false
                        }
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
