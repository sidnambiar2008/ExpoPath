package org.communityday.navigation.events.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.communityday.navigation.events.data.Booth
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_close
import communitydaynavigationapp.composeapp.generated.resources.ic_warning
import org.communityday.navigation.events.data.AuthRepository
import org.communityday.navigation.events.data.EventRepository
import org.communityday.navigation.events.mapDirectory.openMap
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SettingsScreen(
    onDeleteAccount: () -> Unit,
    showSecurityWarning: Boolean,
    onDismissSecurityWarning: () -> Unit,
    isDeleting: Boolean,
    authRepository: AuthRepository
) {
    val NavyBlue = Color(0xFF000033)
    val CardBlue = Color(0xFF1A1A4D)
    val Silver = Color(0xFFC0C0C0)
    val Turquoise = Color(0xFF40E0D0)
    val DangerRed = Color(0xFFCF6679)
    val uriHandler = LocalUriHandler.current // Moved here to fix scope issue
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState() // For small screens
    val user by authRepository.currentUser.collectAsState(initial = null)



    Box(modifier = Modifier.fillMaxSize().background(NavyBlue)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            Text("App Settings", color = Silver, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            // --- Safety & Moderation Section ---
            SettingsSectionHeader("Safety & Moderation", Turquoise)
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SelectionContainer {
                        Text(
                            "Help us keep ExpoPath safe. Report inappropriate content. Feel free to email at expopath.info@gmail.com",
                            color = Silver,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 1: Report a Conference
                    SafetyLink("Report Inappropriate Conference", DangerRed) {
                        val formUrl = "https://docs.google.com/forms/d/e/1FAIpQLSdFmVVjKJtAMHQvL-NESv7hXMxnjnmwvf0hJGWt8K7GmC6hYw/viewform?usp=dialog"
                        uriHandler.openUri(formUrl)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Legal Section ---
            SettingsSectionHeader("Legal", Turquoise)
            LegalCard(Silver)

            Spacer(modifier = Modifier.height(24.dp))

            // --- Account Section ---
            if (user != null && user?.isAnonymous == false){
                SettingsSectionHeader("Account Management", Turquoise)

                Button(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    enabled = !isDeleting, // Disable button while deleting
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed.copy(alpha = 0.2f),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f))
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            color = DangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Delete Account", color = DangerRed, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Permanent action for ${user?.email}",
                                color = DangerRed.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    containerColor = CardBlue, // Match your theme!
                    titleContentColor = Color.White,
                    textContentColor = Silver,
                    title = { Text("Delete Account?", fontWeight = FontWeight.Bold) },
                    text = { Text("This will permanently remove your registration data and any conferences you've created. This cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirmation = false
                                onDeleteAccount()
                            }
                        ) { Text("Delete Permanently", color = DangerRed, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text("Cancel", color = Turquoise)
                        }
                    }
                )
            }
            if (showSecurityWarning) {
                AlertDialog(
                    onDismissRequest = { onDismissSecurityWarning() },
                    containerColor = CardBlue,
                    titleContentColor = Color.White,
                    textContentColor = Silver,
                    title = { Text("Security Check Required") },
                    text = { Text("For your protection, you must have logged in recently to delete your account. Please log out and log back in, then try again.") },
                    confirmButton = {
                        TextButton(onClick = { onDismissSecurityWarning() }) {
                            Text("Got it", color = Turquoise)
                        }
                    }
                )
            }
        }

        // Optional: Full screen overlay if you want to block all interactions during delete
        if (isDeleting) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Turquoise)
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(text: String, color: Color) {
    Text(text = text, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun LegalCard(Silver: Color) {
    val Turquoise = Color(0xFF40E0D0)
    val uriHandler = LocalUriHandler.current // 1. Grab the handler

    // Replace with your actual GitHub Pages or Repo link
    val baseUrl = "https://sidnambiar2008.github.io/ExpoPath/"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A4D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ExpoPath", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Version 1.4", color = Silver, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Map the buttons to your GitHub files
            LegalLink("Privacy Policy", Turquoise) {
                uriHandler.openUri("$baseUrl/PRIVACY")
            }
            LegalLink("Terms of Service", Turquoise) {
                uriHandler.openUri("$baseUrl/TERMS")
            }
            LegalLink("Open Source Licenses", Turquoise) {
                uriHandler.openUri("$baseUrl/LICENSES")
            }

            // 3. Contact Support (Opens the user's default email app)
            LegalLink("Contact Support", Turquoise) {
                //uriHandler.openUri("mailto:your-email@usc.edu?subject=ExpoPath%20Support")
                uriHandler.openUri("$baseUrl/SUPPORT")
            }
        }
    }
}

@Composable
fun LegalLink(text: String, color: Color, onClick: () -> Unit) {
    Text(
        text = text,
        color = color,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    )
}

@Composable
fun SafetyLink(text: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.ic_warning), // Add a close icon to Res
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
            )
        Spacer(Modifier.width(8.dp))
        Text(text = text, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}