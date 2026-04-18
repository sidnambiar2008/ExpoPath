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
            onClick = onDeleteAccount,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.2f))
        ) {
            Text("Delete Account", color = DangerRed)
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
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A4D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Community Day 2026", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Version 1.0.0", color = Silver, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("© 2026 All Rights Reserved", color = Silver.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}