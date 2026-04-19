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

    // UI State
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) } // <--- HERE
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dynamic Title
        Text(
            text = if (isSignUp) "Create Admin Account" else "Admin Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Error Display
        errorMessage?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(Modifier.height(16.dp))

        // Main Action Button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null // Reset error on new attempt

                    val result = if (isSignUp) {
                        authRepo.signUp(email, password)
                    } else {
                        authRepo.login(email, password)
                    }

                    if (result.isSuccess) {
                        onLoginSuccess()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Processing..." else if (isSignUp) "Sign Up" else "Login")
        }

        // Toggle Button
        TextButton(onClick = { isSignUp = !isSignUp }) {
            Text(if (isSignUp) "Already have an account? Login" else "Need an account? Sign Up")
        }
    }
}
