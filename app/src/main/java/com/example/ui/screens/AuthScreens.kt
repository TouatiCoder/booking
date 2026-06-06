package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.StaysRepository
import com.example.ui.localization.Localization
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: StaysRepository,
    onSuccessAuth: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            Spacer(modifier = Modifier.height(48.dp))

            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .testTag("auth_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = LuxuryDarkBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MoroccanStarIcon(
                    modifier = Modifier.size(64.dp),
                    color = LuxuryGold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = Localization.get("login", currentLang),
                    style = MaterialTheme.typography.displayMedium,
                    color = LuxuryDarkBlue,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = Localization.get("visitors_can", currentLang),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text(Localization.get("email", currentLang)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LuxuryGold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            focusedLabelColor = LuxuryGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text(Localization.get("password", currentLang)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LuxuryGold) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = LuxuryGold
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            focusedLabelColor = LuxuryGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = LuxuryGold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = Localization.get("mandatory_field", currentLang)
                                    return@Button
                                }
                                isLoading = true
                                focusManager.clearFocus()
                                scope.launch {
                                    val success = repository.loginUser(email.trim(), password)
                                    isLoading = false
                                    if (success) {
                                        onSuccessAuth()
                                    } else {
                                        errorMessage = "Invalid email or password. Please try again."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_button")
                        ) {
                            Text(
                                text = Localization.get("login", currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                color = LuxuryWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("go_to_register_button")
            ) {
                Text(
                    text = Localization.get("no_account", currentLang),
                    color = LuxuryDarkBlue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Helpful shortcuts for testing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, LuxuryGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Demo Sign In Shortcuts",
                    color = LuxuryDarkBlue,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(
                        onClick = { email = "host@zellige.com"; password = "host123" },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Host Login", fontSize = 11.sp, color = LuxuryDarkBlue)
                    }
                    Button(
                        onClick = { email = "admin@zellige.com"; password = "admin123" },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryZelligeGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Admin Login", fontSize = 11.sp, color = LuxuryWhite)
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    repository: StaysRepository,
    onSuccessAuth: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val currentLang by repository.currentLanguageState.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectHostRole by remember { mutableStateOf(false) } // Toggle user role directly

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            Spacer(modifier = Modifier.height(48.dp))

            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = LuxuryDarkBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MoroccanStarIcon(
                    modifier = Modifier.size(64.dp),
                    color = LuxuryGold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = Localization.get("register", currentLang),
                    style = MaterialTheme.typography.displayMedium,
                    color = LuxuryDarkBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text(Localization.get("full_name", currentLang)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LuxuryGold) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            focusedLabelColor = LuxuryGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_name_input")
                    )

                    // Phone Number
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; errorMessage = null },
                        label = { Text(Localization.get("phone", currentLang)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = LuxuryGold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            focusedLabelColor = LuxuryGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_phone_input")
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text(Localization.get("email", currentLang)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LuxuryGold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            focusedLabelColor = LuxuryGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email_input")
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text(Localization.get("password", currentLang)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LuxuryGold) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = LuxuryGold
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            focusedLabelColor = LuxuryGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input")
                    )

                    // Select User Account Mode: Host Setup Included Directly
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectHostRole,
                            onCheckedChange = { selectHostRole = it },
                            colors = CheckboxDefaults.colors(checkedColor = LuxuryGold)
                        )
                        Text(
                            text = "Register as an accommodation Host",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = LuxuryGold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Button(
                            onClick = {
                                if (name.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank()) {
                                    errorMessage = Localization.get("mandatory_field", currentLang)
                                    return@Button
                                }
                                isLoading = true
                                focusManager.clearFocus()
                                scope.launch {
                                    val role = if (selectHostRole) "host" else "client"
                                    val success = repository.registerUser(
                                        fullName = name.trim(),
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        passwordHash = password,
                                        role = role
                                    )
                                    isLoading = false
                                    if (success) {
                                        onSuccessAuth()
                                    } else {
                                        errorMessage = "An account with this email already exists."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("register_button")
                        ) {
                            Text(
                                text = Localization.get("register", currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                color = LuxuryWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("go_to_login_button")
            ) {
                Text(
                    text = Localization.get("have_account", currentLang),
                    color = LuxuryDarkBlue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
