package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.repository.StaysRepository
import com.example.ui.localization.Localization
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionDialog(
    repository: StaysRepository,
    onLanguageSelected: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedLang by remember { mutableStateOf("") }

    val languages = listOf(
        "ar" to "العربية",
        "fr" to "Français",
        "en" to "English",
        "es" to "Español"
    )

    AlertDialog(
        onDismissRequest = { /* Force selection */ },
        modifier = Modifier.fillMaxWidth().testTag("language_selection_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = LuxuryDarkBlue,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                MoroccanStarIcon(modifier = Modifier.size(48.dp).padding(bottom = 8.dp), color = LuxuryGold)
                Text(
                    text = "Pick Your Language\nاختر لغة الإقامة",
                    style = MaterialTheme.typography.titleLarge,
                    color = LuxuryWhite,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    Button(
                        onClick = {
                            selectedLang = code
                            coroutineScope.launch {
                                repository.saveLanguage(code)
                                repository.completeFirstLaunch()
                                onLanguageSelected()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedLang == code) LuxuryGold else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (selectedLang == code) LuxuryGold else LuxuryWhite.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedLang == code) LuxuryDarkBlue else LuxuryWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun SplashScreen(
    repository: StaysRepository,
    onSplashFinished: () -> Unit
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val branding by repository.brandingState.collectAsState()
    
    val appTitle = branding["app_display_name"]?.takeIf { it.isNotBlank() } ?: Localization.get("app_title", currentLang)
    val appSlogan = branding["app_slogan"]?.takeIf { it.isNotBlank() } ?: Localization.get("splash_sub", currentLang)
    val appLogoUrl = branding["app_logo"]?.takeIf { it.isNotBlank() }
    
    val primaryColorHex = branding["primary_color"]
    val dynamicGold = if (!primaryColorHex.isNullOrBlank()) {
        try { Color(android.graphics.Color.parseColor(primaryColorHex)) } catch (e: Exception) { LuxuryGold }
    } else LuxuryGold

    val secondaryColorHex = branding["secondary_color"]
    val dynamicBg = if (!secondaryColorHex.isNullOrBlank()) {
        try { Color(android.graphics.Color.parseColor(secondaryColorHex)) } catch (e: Exception) { LuxuryDarkBlue }
    } else LuxuryDarkBlue

    // Smooth pulse/fade animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )
    
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(1500))
        // Show splash screen for 5 seconds as mandated
        delay(5000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dynamicBg),
        contentAlignment = Alignment.Center
    ) {
        ZelligeBackdropPattern(dynamicGold)

        Column(
            modifier = Modifier
                .padding(24.dp)
                .alpha(alphaAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = dynamicGold,
                        radius = size.minDimension / 2.3f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                
                if (appLogoUrl != null) {
                    AsyncImage(
                        model = appLogoUrl,
                        contentDescription = "Dynamic Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(50.dp))
                    )
                } else {
                    // Inside custom moroccan geometric pattern
                    MoroccanStarIcon(
                        modifier = Modifier.size(100.dp),
                        color = dynamicGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = appTitle,
                style = MaterialTheme.typography.displayLarge,
                color = dynamicGold,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 36.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = appSlogan,
                style = MaterialTheme.typography.titleMedium,
                color = dynamicGold.copy(alpha = 0.8f),
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = dynamicGold,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun MoroccanStarIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val path = Path()
        val width = size.width
        val height = size.height
        val cx = width / 2
        val cy = height / 2
        val r1 = width * 0.45f
        val r2 = width * 0.22f

        // Draw standard Moroccan 8-pointed geometric star
        // Combined two overlaying squares rotated 45 degrees
        for (i in 0 until 8) {
            val angle = i * Math.PI / 4
            val x = (cx + r1 * Math.cos(angle)).toFloat()
            val y = (cy + r1 * Math.sin(angle)).toFloat()
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            // Intermediary points for star structure
            val angleMid = angle + Math.PI / 8
            val mx = (cx + r2 * Math.cos(angleMid)).toFloat()
            val my = (cy + r2 * Math.sin(angleMid)).toFloat()
            path.lineTo(mx, my)
        }
        path.close()
        drawPath(path = path, color = color)
    }
}

@Composable
fun ZelligeBackdropPattern(color: Color = LuxuryGold) {
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.12f)) {
        val spacing = 20.dp.toPx()
        val columns = (size.width / spacing).toInt() + 2
        val rows = (size.height / spacing).toInt() + 2
        
        for (col in 0 until columns) {
            for (row in 0 until rows) {
                drawCircle(
                    color = color,
                    radius = 1.2f.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(col * spacing, row * spacing)
                )
            }
        }
    }
}
