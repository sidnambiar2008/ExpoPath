package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_back_arrow
import communitydaynavigationapp.composeapp.generated.resources.ic_visibility
import communitydaynavigationapp.composeapp.generated.resources.ic_visibilityoff
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.delay
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
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    /*
    LaunchedEffect(Unit) {
        // 1. Give Firebase a tiny moment to initialize the token
        delay(500)

        // 2. Check if a user exists
        val user = Firebase.auth.currentUser
        if (user != null) {
            // 3. User found! Proceed to the dashboard
            onLoginSuccess()
        } else {
            // 4. No user, stay on this screen and show the login fields
            isCheckingAuth = false
        }
    }
    */
    LaunchedEffect(Unit) {
        delay(500)
        val user = Firebase.auth.currentUser

        // The Logic Gate:
        // We only skip IF there is a user AND they aren't a guest (they have an email)
        if (user != null && !user.isAnonymous) {
            onLoginSuccess()
        } else {
            // Otherwise, show the login form so they can sign in as an Admin
            isCheckingAuth = false
        }
    }
    if (isCheckingAuth) {
        // Show NOTHING or a Loading Spinner while we check the session
        // This prevents the login screen from ever appearing if a user is found
        Box(
            modifier = Modifier.fillMaxSize().background(NavyBlue),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Turquoise)
        }
    }
    else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar( // The title to be centered
                    title = {
                    },
                    navigationIcon = {
                        // We use a Row inside a clickable Box or TextButton to make the whole area touchable
                        TextButton(
                            onClick = onBackClick,
                            contentPadding = PaddingValues(start = 8.dp) // Align it closer to the edge
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.IconButton(onClick = onBackClick) {
                                    Icon(
                                        imageVector = vectorResource(Res.drawable.ic_back_arrow),
                                        contentDescription = "Back",
                                        tint = Turquoise
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Return to Home",
                                    color = Silver,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = NavyBlue
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
                    text = if (isSignUp) "Create Account" else "Login",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Turquoise
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
                    // DYNAMIC TRANSFORMATION: Show dots if NOT visible, show text if visible
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            vectorResource(Res.drawable.ic_visibility) // You'll need these icons in Res
                        else
                            vectorResource(Res.drawable.ic_visibilityoff)

                        val description = if (passwordVisible) "Hide password" else "Show password"

                        androidx.compose.material3.IconButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Icon(imageVector = image, contentDescription = description, tint = Silver)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Silver,
                        focusedBorderColor = Turquoise,
                        unfocusedBorderColor = Silver.copy(alpha = 0.7f),
                        focusedLabelColor = Turquoise,
                        unfocusedLabelColor = Silver.copy(alpha = 0.8f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedContainerColor = Color.Transparent
                    ),
                    enabled = !isLoading,
                    singleLine = true
                )
                if (isSignUp) {
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        // Use the same toggle state here!
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            // Add the icon here too so the user doesn't have to reach up to the first box
                            androidx.compose.material3.IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        vectorResource(Res.drawable.ic_visibility)
                                    else
                                        vectorResource(Res.drawable.ic_visibilityoff),
                                    contentDescription = null,
                                    tint = Silver.copy(alpha = 0.7f)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Silver,
                            focusedBorderColor = Turquoise,
                            unfocusedBorderColor = Silver.copy(alpha = 0.7f),
                            focusedLabelColor = Turquoise,
                            unfocusedLabelColor = Silver.copy(alpha = 0.8f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                            focusedContainerColor = Color.Transparent
                        ),
                        enabled = !isLoading,
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            if (email.isBlank()) {
                                successMessage = null
                                errorMessage = "Please enter your email first."
                            } else {
                                scope.launch {
                                    isLoading = true
                                    val result = authRepo.sendPasswordResetEmail(email)
                                    isLoading = false
                                    if (result.isSuccess) {
                                        successMessage = "Reset email sent! Check your inbox or spam."
                                        errorMessage = null
                                    } else {
                                        val rawError = result.exceptionOrNull()?.message
                                        errorMessage = mapFirebaseError(rawError)
                                        successMessage = null
                                    }
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            "Forgot Password?",
                            color = Turquoise,
                            style = MaterialTheme.typography.bodySmall
                        )
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
                            if (!email.contains("@") || !email.contains(".")) {
                                errorMessage = "Please enter a valid email address."
                                return@launch
                            }

                            // 2. Length check (Primary for Sign Up)
                            if (isSignUp && password.length < 6) {
                                errorMessage = "Password must be at least 6 characters."
                                return@launch
                            }

                            // 3. Match check
                            if (isSignUp && password != confirmPassword) {
                                errorMessage = "Passwords do not match."
                                return@launch
                            }
                            try {
                                isLoading = true
                                errorMessage = null
                                successMessage = null

                                val result = if (isSignUp) {
                                    authRepo.signUp(email, password)
                                } else {
                                    authRepo.login(email, password)
                                }

                                if (result.isSuccess) {
                                    onLoginSuccess()
                                } else {
                                    val rawError = result.exceptionOrNull()?.message
                                    errorMessage = mapFirebaseError(rawError)
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message
                            } finally {
                                isLoading = false // Always reset in finally block
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionOrange, // Sets the background to Turquoise
                        contentColor = Color.White   // Sets the text color (optional, but better for contrast)
                    ),
                    // Only enable if fields aren't empty AND not loading
                    enabled = !isLoading &&
                            email.contains("@") &&
                            email.contains(".") && // Added a check for the dot (e.g., .com)
                            password.length >= 6 &&
                            (!isSignUp || (confirmPassword.length >= 6 && confirmPassword == password))) {
                    Text(if (isLoading) "Processing..." else if (isSignUp) "Sign Up" else "Login")
                }

                TextButton(
                    onClick = {
                        isSignUp = !isSignUp
                        errorMessage = null // Clear errors when switching modes
                        confirmPassword = "" // Reset this so it's clean for the user
                        successMessage = null // Clear this too
                       // email = ""
                       // password = ""
                        //confirmPassword = ""
                    },
                    enabled = !isLoading
                ) {
                    Text(
                        if (isSignUp) "Already have an account? Login" else "Need an account? Sign Up",
                        color = Color.White
                    )
                }
            }
        }
    }
}fun mapFirebaseError(message: String?): String {
    val error = message ?: ""
    return when {
        // Credential issues
        error.contains("invalid-credential") || error.contains("wrong-password") ||
                error.contains("ERROR_INVALID_CREDENTIAL") ->
            "Invalid email or password. Please try again."

        // Account issues
        error.contains("user-not-found") -> "No account found with this email."
        error.contains("email-already-in-use") -> "An account already exists with this email."

        // Password strength (The one you were missing)
        error.contains("weak-password") -> "Password must be at least 6 characters."

        // Input format
        error.contains("invalid-email") -> "Please enter a valid email address."

        // System/Network issues
        error.contains("network-request-failed") -> "Connection error. Check your internet."
        error.contains("too-many-requests") -> "Too many attempts. Please try again later."

        else -> "Something went wrong. Please try again."
    }
}
