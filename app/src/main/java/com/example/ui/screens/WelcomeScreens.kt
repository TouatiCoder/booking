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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.StaysRepository
import com.example.ui.localization.Localization
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LanguageSelectionScreen(
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(LuxuryDarkBlue, Color(0xFF0F2C46))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative geometric backdrop (Zellige geometric circles/stars)
        ZelligeBackdropPattern()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant star motif
            MoroccanStarIcon(
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp),
                color = LuxuryGold
            )

            Text(
                text = "ZELLIGE STAYS",
                style = MaterialTheme.typography.displayMedium,
                color = LuxuryGold,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                text = "Marhaban • Welcome",
                style = MaterialTheme.typography.titleMedium,
                color = LuxuryWhite,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13324E).copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LuxuryGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pick Your Language\nاختر لغة الإقامة الأدعم",
                        style = MaterialTheme.typography.titleLarge,
                        color = LuxuryWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    languages.forEach { (code, name) ->
                        Button(
                            onClick = {
                                selectedLang = code
                                coroutineScope.launch {
                                    repository.saveLanguage(code)
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
            }
        }
    }
}

@Composable
fun SplashScreen(
    repository: StaysRepository,
    onSplashFinished: () -> Unit
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    
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
        repository.completeFirstLaunch()
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryDarkBlue),
        contentAlignment = Alignment.Center
    ) {
        ZelligeBackdropPattern()

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
                        color = LuxuryGold,
                        radius = size.minDimension / 2.3f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                
                // Inside custom moroccan geometric pattern
                MoroccanStarIcon(
                    modifier = Modifier.size(100.dp),
                    color = LuxuryGold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = Localization.get("app_title", currentLang),
                style = MaterialTheme.typography.displayLarge,
                color = LuxuryGold,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 36.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Localization.get("splash_sub", currentLang),
                style = MaterialTheme.typography.titleMedium,
                color = LuxuryLightGold,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = LuxuryGold,
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
fun ZelligeBackdropPattern() {
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.12f)) {
        val spacing = 20.dp.toPx()
        val columns = (size.width / spacing).toInt() + 2
        val rows = (size.height / spacing).toInt() + 2
        
        for (col in 0 until columns) {
            for (row in 0 until rows) {
                drawCircle(
                    color = LuxuryGold,
                    radius = 1.2f.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(col * spacing, row * spacing)
                )
            }
        }
    }
}
