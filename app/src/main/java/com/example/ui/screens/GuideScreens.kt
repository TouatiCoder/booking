package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dto.GuideDto
import com.example.data.dto.GuideReviewDto
import com.example.data.repository.StaysRepository
import com.example.ui.theme.CardBackground
import com.example.ui.theme.LuxuryDarkBlue
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.TextDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourGuidesScreen(
    repository: StaysRepository,
    onNavigateToDetails: (String) -> Unit
) {
    var guides by remember { mutableStateOf<List<GuideDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        guides = repository.getGuides()
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        TopAppBar(
            title = { Text("Tour Guides", fontWeight = FontWeight.Bold, color = LuxuryDarkBlue) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LuxuryGold)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(guides) { guide ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToDetails(guide.id) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(LuxuryDarkBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = LuxuryDarkBlue)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = guide.name ?: "Guide", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                                    Text(text = " ${(guide.rating ?: "0.0")} (${guide.total_reviews ?: 0} reviews)", fontSize = 14.sp, color = Color.Gray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    Text(text = " ${guide.languages}", fontSize = 14.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "${guide.price_per_day} MAD", fontWeight = FontWeight.Bold, color = LuxuryDarkBlue)
                                Text(text = "per day", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideBookingScreen(
    repository: StaysRepository,
    guideId: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val currentUser by repository.currentUserState.collectAsState()
    var guide by remember { mutableStateOf<GuideDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf("2026-06-10") }

    LaunchedEffect(guideId) {
        guide = repository.getGuideById(guideId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Guide", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LuxuryGold)
            }
        } else if (guide != null) {
            val g = guide!!
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("Booking ${g.name}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = Color.LightGray)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = LuxuryDarkBlue.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Price")
                            Text("${g.price_per_day} MAD", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (currentUser != null && selectedDate.isNotBlank()) {
                            isSubmitting = true
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                val success = repository.bookGuide(
                                    currentUser!!.email, 
                                    guideId, 
                                    selectedDate, 
                                    g.price_per_day
                                )
                                isSubmitting = false
                                if (success) {
                                    onSuccess()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Confirm Booking & Pay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideDetailsScreen(
    repository: StaysRepository,
    guideId: String,
    onBack: () -> Unit,
    onContactGuide: (String, String) -> Unit,
    onBookGuide: (String) -> Unit
) {
    val currentUser by repository.currentUserState.collectAsState()
    var guide by remember { mutableStateOf<GuideDto?>(null) }
    var reviews by remember { mutableStateOf<List<GuideReviewDto>>(emptyList()) }
    var canReview by remember { mutableStateOf(false) }
    var bookingIdForReview by remember { mutableStateOf<String?>(null) }
    var showReviewDialog by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }

    fun refreshData() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            guide = repository.getGuideById(guideId)
            reviews = repository.getGuideReviews(guideId)
            if (currentUser != null) {
                val cr = repository.canReviewGuide(currentUser!!.email, guideId)
                canReview = cr?.can_review ?: false
                bookingIdForReview = cr?.reservation_id // note: in DtoModels we used reservation_id for CanReviewResponseDto
            }
            isLoading = false
        }
    }

    LaunchedEffect(guideId, currentUser) {
        refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(guide?.name ?: "Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        bottomBar = {
            if (guide != null) {
                Surface(shadowElevation = 8.dp, color = Color.White) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { guide?.user_id?.let { onContactGuide(guideId, it) } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Contact", color = LuxuryDarkBlue)
                        }
                        Button(
                            onClick = { onBookGuide(guideId) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold)
                        ) {
                            Text("Book Now", color = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LuxuryGold)
            }
        } else if (guide != null) {
            val g = guide!!
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(80.dp).clip(CircleShape).background(LuxuryDarkBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp), tint = LuxuryDarkBlue)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(g.name ?: "", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(18.dp))
                                    Text(" ${g.rating ?: "0.0"} (${g.total_reviews ?: 0} reviews)", color = Color.DarkGray)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("About", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(g.description ?: "No description provided.", color = Color.DarkGray)

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = LuxuryDarkBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Languages: ${g.languages}")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationCity, contentDescription = null, tint = LuxuryDarkBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("City: ${g.city_name}")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = LuxuryDarkBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Price: ${g.price_per_day} MAD per day")
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Reviews", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                            if (canReview) {
                                TextButton(onClick = { showReviewDialog = true }) {
                                    Text("Leave a Review", color = LuxuryGold, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (reviews.isEmpty()) {
                    item {
                        Text("No reviews yet.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(reviews) { r ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(r.user_name ?: "Traveler", fontWeight = FontWeight.Bold)
                                Row {
                                    for(i in 1..5) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = if (i <= r.rating) LuxuryGold else Color.LightGray, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text(r.comment, modifier = Modifier.padding(top = 8.dp), color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReviewDialog && bookingIdForReview != null && currentUser != null) {
        var rating by remember { mutableIntStateOf(5) }
        var comment by remember { mutableStateOf("") }
        var isSubmittingReview by remember { mutableStateOf(false) }

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
                                tint = if (i <= rating) LuxuryGold else Color.LightGray,
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
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = Color.LightGray)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showReviewDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                if (comment.isNotBlank()) {
                                    isSubmittingReview = true
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                        val success = repository.submitGuideReview(guideId, currentUser!!.email, bookingIdForReview!!, rating, comment)
                                        isSubmittingReview = false
                                        if (success) {
                                            showReviewDialog = false
                                            refreshData()
                                        }
                                    }
                                }
                            },
                            enabled = !isSubmittingReview,
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold)
                        ) {
                            if (isSubmittingReview) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Submit", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
