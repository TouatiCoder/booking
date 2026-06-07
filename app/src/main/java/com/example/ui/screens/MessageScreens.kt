package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dto.ConversationDto
import com.example.data.dto.MessageDto
import com.example.data.repository.StaysRepository
import com.example.ui.theme.CardBackground
import com.example.ui.theme.LuxuryDarkBlue
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.TextDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsListScreen(
    repository: StaysRepository,
    onBack: () -> Unit,
    onConversationSelected: (String, String) -> Unit // convId, title
) {
    val user by repository.currentUserState.collectAsState()
    var conversations by remember { mutableStateOf<List<ConversationDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user) {
        user?.let {
            conversations = repository.getConversations(it.email).sortedByDescending { c -> c.created_at }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold, color = LuxuryDarkBlue) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LuxuryDarkBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LuxuryGold)
            }
        } else if (conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No conversations yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FA))
            ) {
                items(conversations) { conv ->
                    val otherParty = if (user?.email == conv.host_id) conv.client_name else conv.host_name
                    val roleLabel = if (user?.email == conv.host_id) "Client" else "Host"
                    val title = conv.property_title ?: "Property"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { onConversationSelected(conv.id, "$otherParty - $title") },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(LuxuryDarkBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = LuxuryDarkBlue)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "$otherParty ($roleLabel)", fontWeight = FontWeight.Bold, color = TextDark)
                                Text(text = title, fontSize = 14.sp, color = Color.Gray, maxLines = 1)
                            }
                            conv.created_at?.let {
                                Text(text = it.take(10), fontSize = 12.sp, color = Color.Gray)
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
fun MessageThreadScreen(
    repository: StaysRepository,
    conversationId: String,
    title: String,
    onBack: () -> Unit
) {
    val user by repository.currentUserState.collectAsState()
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var inputMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isSending by remember { mutableStateOf(false) }

    // Polling mechanism
    LaunchedEffect(conversationId) {
        while (true) {
            val fetched = repository.getMessages(conversationId)
            if (fetched != messages) {
                messages = fetched
            }
            delay(3000) // Poll every 3 seconds
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = LuxuryDarkBlue, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LuxuryDarkBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            if (inputMessage.isNotBlank() && user != null && !isSending) {
                                isSending = true
                                val msgToSend = inputMessage
                                inputMessage = ""
                                scope.launch {
                                    val success = repository.sendMessage(conversationId, user!!.email, msgToSend)
                                    if (success) {
                                        messages = repository.getMessages(conversationId)
                                    }
                                    isSending = false
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(LuxuryGold, CircleShape)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA)),
            contentPadding = PaddingValues(16.dp),
            reverseLayout = true // Messages typically flow from bottom up, so list reversed
        ) {
            items(messages.reversed()) { msg ->
                val isMe = msg.sender_id == user?.email
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ))
                            .background(if (isMe) LuxuryGold else CardBackground)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.message,
                            color = if (isMe) Color.White else TextDark,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
