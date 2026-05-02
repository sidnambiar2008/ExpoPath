package org.communityday.navigation.events.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.communityday.navigation.events.data.EventRepository
import org.communityday.navigation.events.mapDirectory.openMap

@Composable
fun SettingsScreen(
    onDeleteAccount: () -> Unit,
    showSecurityWarning: Boolean,
    onDismissSecurityWarning: () -> Unit,
    isDeleting: Boolean
) {
    val NavyBlue = Color(0xFF000033)
    val CardBlue = Color(0xFF1A1A4D)
    val Silver = Color(0xFFC0C0C0)
    val Turquoise = Color(0xFF40E0D0)
    val DangerRed = Color(0xFFCF6679)

    var showDeleteConfirmation by remember { mutableStateOf(false) }


    Box(modifier = Modifier.fillMaxSize().background(NavyBlue)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("App Settings", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            // --- Legal Section ---
            SettingsSectionHeader("Legal", Turquoise)
            LegalCard(Silver)

            Spacer(modifier = Modifier.height(24.dp))

            // --- Account Section ---
            SettingsSectionHeader("Account Management", Turquoise)

            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                enabled = !isDeleting, // Disable button while deleting
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed.copy(alpha = 0.2f),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.1f)
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(color = DangerRed, modifier = Modifier.size(20.dp))
                } else {
                    Text("Delete Account", color = DangerRed)
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
    val baseUrl = "https://github.com/sidnambiar2008/Community-Day-Navigation-App/blob/main/"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A4D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ExpoPath", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Version 1.0.0", color = Silver, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Map the buttons to your GitHub files
            LegalLink("Privacy Policy", Turquoise) {
                uriHandler.openUri("$baseUrl/PRIVACY.md")
            }
            LegalLink("Terms of Service", Turquoise) {
                uriHandler.openUri("$baseUrl/TERMS.md")
            }
            LegalLink("Open Source Licenses", Turquoise) {
                uriHandler.openUri("$baseUrl/LICENSES.md")
            }

            // 3. Contact Support (Opens the user's default email app)
            LegalLink("Contact Support", Turquoise) {
                //uriHandler.openUri("mailto:your-email@usc.edu?subject=ExpoPath%20Support")
                uriHandler.openUri("$baseUrl/SUPPORT.md")
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
