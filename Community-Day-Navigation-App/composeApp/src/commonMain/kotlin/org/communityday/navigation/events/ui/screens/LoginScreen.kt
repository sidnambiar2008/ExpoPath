package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_back_arrow
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import org.communityday.navigation.events.data.AuthRepository
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authRepo: AuthRepository,
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val NavyBlue = Color(0xFF000033)
    val Turquoise = Color(0xFF40E0D0)
    val Silver = Color(0xFFC0C0C0)
    val ActionOrange = Color(0xFFFF8C00)
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isCheckingAuth by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            // User is already logged in! Skip the login form.
            onLoginSuccess()
        } else {
            isCheckingAuth = false // No user found, show the login form
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Back to Home Page", color = Color.White) }, // Added white text for visibility
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_back_arrow),
                            contentDescription = "Back",
                            tint = ActionOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyBlue // Match your background
                )
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(NavyBlue).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSignUp) "Create Admin Account" else "Admin Login",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(Modifier.height(16.dp))

            androidx.compose.material3.OutlinedTextField( // Use Outlined for better Web stability
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    // BRIGHTEN THE TEXT
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Silver, // Much lighter than the default dark gray

                    // BRIGHTEN THE BORDER
                    focusedBorderColor = Turquoise,
                    unfocusedBorderColor = Silver.copy(alpha = 0.7f), // A crisp Silver outline

                    // BRIGHTEN THE LABEL (the hint text)
                    focusedLabelColor = Turquoise,
                    unfocusedLabelColor = Silver.copy(alpha = 0.8f),

                    // OPTIONAL: Background color (container)
                    // If you want the box itself to be slightly lighter than the Navy background:
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color.Transparent
                ),
                enabled = !isLoading, // Prevent typing while processing
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            androidx.compose.material3.OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    // BRIGHTEN THE TEXT
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Silver, // Much lighter than the default dark gray

                    // BRIGHTEN THE BORDER
                    focusedBorderColor = Turquoise,
                    unfocusedBorderColor = Silver.copy(alpha = 0.7f), // A crisp Silver outline

                    // BRIGHTEN THE LABEL (the hint text)
                    focusedLabelColor = Turquoise,
                    unfocusedLabelColor = Silver.copy(alpha = 0.8f),

                    // OPTIONAL: Background color (container)
                    // If you want the box itself to be slightly lighter than the Navy background:
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color.Transparent
                ),
                enabled = !isLoading, // Prevent typing while processing
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        if (email.isBlank()) {
                            errorMessage = "Please enter your email first."
                        } else {
                            scope.launch {
                                isLoading = true
                                val result = authRepo.sendPasswordResetEmail(email)
                                isLoading = false
                                if (result.isSuccess) {
                                    successMessage = "Reset email sent! Check your inbox."
                                    errorMessage = null
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to send reset email."
                                }
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Forgot Password?", color = Turquoise, style = MaterialTheme.typography.bodySmall)
                }
            }

// Display success message if it exists
            successMessage?.let {
                Text(it, color = Turquoise, modifier = Modifier.padding(vertical = 8.dp))
            }

            errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    // Focus: Ensure the keyboard/focus is cleared or at least not looping
                    scope.launch {
                        try {
                            isLoading = true
                            errorMessage = null

                            val result = if (isSignUp) {
                                authRepo.signUp(email, password)
                            } else {
                                authRepo.login(email, password)
                            }

                            if (result.isSuccess) {
                                onLoginSuccess()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Unknown Error"
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message
                        } finally {
                            isLoading = false // Always reset in finally block
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                // Only enable if fields aren't empty AND not loading
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                Text(if (isLoading) "Processing..." else if (isSignUp) "Sign Up" else "Login")
            }

            TextButton(
                onClick = {
                    isSignUp = !isSignUp
                    errorMessage = null // Clear errors when switching modes
                },
                enabled = !isLoading
            ) {
                Text(if (isSignUp) "Already have an account? Login" else "Need an account? Sign Up", color = Color.White)
            }
        }
    }
}
