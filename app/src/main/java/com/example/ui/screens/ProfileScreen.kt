package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.repository.StaysRepository
import com.example.ui.localization.Localization
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    repository: StaysRepository,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToReservations: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToHostDashboard: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val scope = rememberCoroutineScope()
    val user by repository.currentUserState.collectAsState()

    val languages = listOf(
        "ar" to "العربية",
        "fr" to "Français",
        "en" to "English",
        "es" to "Español"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxurySand)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = Localization.get("profile", currentLang),
            style = MaterialTheme.typography.displayMedium,
            color = LuxuryDarkBlue,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 24.dp)
        )

        if (user == null) {
            // UNAUTHENTICATED VISITOR VIEW
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(LuxuryGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(56.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Access Luxury Accommodations",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Create an account to book Riads, list properties as a host, save favorites, and manage reservations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLight,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Button(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("visitor_profile_login_btn")
                    ) {
                        Text(Localization.get("login", currentLang), color = LuxuryWhite)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onNavigateToRegister,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("visitor_profile_register_btn")
                    ) {
                        Text(Localization.get("register", currentLang), color = LuxuryDarkBlue)
                    }
                }
            }
        } else {
            // AUTHENTICATED USER EXPANSION
            val currentUser = user!!
            
            // User Meta Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(LuxuryGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.fullName.take(2).uppercase(),
                            color = LuxuryDarkBlue,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = currentUser.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = currentUser.email, style = MaterialTheme.typography.bodyMedium, color = TextLight)
                        Text(text = currentUser.phone, style = MaterialTheme.typography.bodySmall, color = TextLight)
                        
                        // Role tag badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(LuxuryGold.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = LuxuryDarkGold, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ROLE: " + currentUser.role.uppercase(),
                                fontSize = 10.sp,
                                color = LuxuryDarkGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actionable Lists: My Bookings, My Favorites, Admin controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Portal Access buttons (Admin or Host specific)
                if (currentUser.role == "admin") {
                    ProfileMenuRow(
                        icon = Icons.Default.AdminPanelSettings,
                        title = Localization.get("admin_dashboard", currentLang),
                        color = LuxuryZelligeGreen,
                        onClick = onNavigateToAdminDashboard,
                        tag = "admin_portal_row"
                    )
                }

                if (currentUser.role == "host") {
                    ProfileMenuRow(
                        icon = Icons.Default.HomeWork,
                        title = Localization.get("host_dashboard", currentLang),
                        color = LuxuryGold,
                        onClick = onNavigateToHostDashboard,
                        tag = "host_portal_row"
                    )
                }

                ProfileMenuRow(
                    icon = Icons.Default.Luggage,
                    title = Localization.get("reservations", currentLang),
                    onClick = onNavigateToReservations,
                    tag = "reservations_portal_row"
                )

                ProfileMenuRow(
                    icon = Icons.Default.Favorite,
                    title = Localization.get("favorites", currentLang),
                    onClick = onNavigateToFavorites,
                    tag = "favorites_portal_row"
                )

                // BECOME HOST CTA (Prompted only for normal Client accounts)
                if (currentUser.role == "client" || currentUser.role == "guest") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LuxuryZelligeGreen.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, LuxuryZelligeGreen.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = Localization.get("become_host", currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                color = LuxuryZelligeGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = Localization.get("become_host_desc", currentLang),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDark,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.promoteToHost(currentUser.email)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryZelligeGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("become_host_btn_click")
                            ) {
                                Text("Become a Host & Unlock Trial", color = LuxuryWhite, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dynamic On-The-Fly Language Selector Widget (accessible to visitors as well)
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Application Language",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { (code, name) ->
                        val isSel = currentLang == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) LuxuryGold else LuxurySand)
                                .clickable {
                                    scope.launch {
                                        repository.saveLanguage(code)
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) LuxuryDarkBlue else TextDark
                            )
                        }
                    }
                }
            }
        }

        // Logout
        if (user != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { repository.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_logout_btn")
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(Localization.get("logout", currentLang), fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color = LuxuryGold,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = if (color == LuxuryGold) LuxuryDarkBlue else color, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextDark, fontWeight = FontWeight.Medium)
            }
            
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextLight)
        }
    }
}
