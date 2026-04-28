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
import androidx.compose.ui.text.style.TextOverflow
import org.communityday.navigation.events.data.EventRepository
import org.communityday.navigation.events.mapDirectory.openMap

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    val Turquoise = Color(0xFF40E0D0)
    val DangerRed = Color(0xFFCF6679)
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBlue)
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

        // Delete Account Button (Legal Requirement)
        Button(
            onClick = { showDeleteConfirmation = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.2f))
        ) {
            Text("Delete Account", color = DangerRed)
        }
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Account?") },
                text = { Text("This will permanently remove your registration data and any conferences you've created. This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = false
                            onDeleteAccount() // 👈 Now actually call it
                        }
                    ) { Text("Delete Permanently", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Back Button ---
        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
        ) {
            Text("Return", color = NavyBlue, fontWeight = FontWeight.Bold)
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

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A4D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ExpoPath", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Version 1.0.0", color = Silver, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // These should trigger a URI Intent to open your website
            LegalLink("Privacy Policy", Turquoise) { /* Open URL */ }
            LegalLink("Terms of Service", Turquoise) { /* Open URL */ }
            LegalLink("Open Source Licenses", Turquoise) { /* Open List */ }
            LegalLink("Contact Support", Turquoise) { /* Open Email */ }
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
