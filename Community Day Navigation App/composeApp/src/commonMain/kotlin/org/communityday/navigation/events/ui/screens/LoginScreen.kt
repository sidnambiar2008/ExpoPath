package org.communityday.navigation.events.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.launch
import org.communityday.navigation.events.data.AuthRepository


@Composable
fun LoginScreen(
    authRepo: AuthRepository,
    onLoginSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSignUp) "Create Admin Account" else "Admin Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        androidx.compose.material3.OutlinedTextField( // Use Outlined for better Web stability
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
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
            enabled = !isLoading, // Prevent typing while processing
            singleLine = true
        )

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
            Text(if (isSignUp) "Already have an account? Login" else "Need an account? Sign Up")
        }
    }
}
