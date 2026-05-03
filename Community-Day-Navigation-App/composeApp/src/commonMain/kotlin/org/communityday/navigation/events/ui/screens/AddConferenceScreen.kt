package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_daterange
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import org.communityday.navigation.events.data.Conference // Import your Conference model
import org.communityday.navigation.events.data.EventRepository // Import your Repository
import org.jetbrains.compose.resources.vectorResource
import kotlin.time.ExperimentalTime
//import kotlin.time.Instant
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime
import org.communityday.navigation.events.data.AuthRepository

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddConferenceScreen(
    repository: EventRepository,
    authRepository: AuthRepository,
    onConferenceCreated: (String) -> Unit,
    onBack: () -> Unit,
    onSwitchAccount: () -> Unit
) {
    // State variables
    var name by remember { mutableStateOf("") }
    var confId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) } // Privacy Toggle
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) } // Date State
    var showError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Date Formatter Helper (Simple for demo)
    val dateText = selectedDateMillis?.let { millis ->
        val instant = Instant.fromEpochMilliseconds(millis)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val date = localDateTime.date // Extract the date object
        "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}" // dayOfMonth is deprecated and apparently became day
    } ?: "Select Conference Date"

    // Colors
    val NavyBlue = Color(0xFF000033)
    val Turquoise = Color(0xFF40E0D0)
    val Silver = Color(0xFFC0C0C0)

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = Color.Black) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(NavyBlue).padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                scope.launch {
                    val result = authRepository.performSignOut()
                    if (result.isSuccess) {
                        // This is the callback that sends them back to the LoginScreen
                        onSwitchAccount()
                    } else {
                        // Optional: show a snackbar or toast
                        println("Sign out failed")
                    }
                }
            }) {
                Text("Switch Account", color = Turquoise.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
            }
        }

        Text("Create Conference", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(Modifier.height(24.dp))

        // Basic Info Fields (Name, ID, Location)
        StandardTextField(name, { name = it }, "Conference Name", Turquoise, Silver)
        Spacer(Modifier.height(12.dp))

        // Conditional ID Label: If private, call it "Access Code", if public, call it "ID"
        StandardTextField(confId, { confId = it }, if (isPublic) "Conference ID" else "Access Code", Turquoise, Silver)
        Spacer(Modifier.height(12.dp))

        StandardTextField(address, { address = it }, "Location (Provide An Address)", Turquoise, Silver)

        Spacer(Modifier.height(24.dp))

        // --- Privacy Toggle Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Privacy", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    text = if (isPublic) "Public (Anyone can join)" else "Private (Code required)",
                    color = Silver,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = isPublic,
                onCheckedChange = { isPublic = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Turquoise)
            )
        }

        Spacer(Modifier.height(24.dp))

        // --- Date Picker Button ---
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Silver.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_daterange),
                contentDescription = "Select Date",
                tint = Silver
            )
            Spacer(Modifier.width(8.dp))
            Text(dateText, color = Color.White)
        }

        Spacer(Modifier.height(40.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Turquoise)
        } else {
            if (showError) {
                Text(
                    text = "Please fill in all fields and select a date.",
                    color = Color(0xFFFF6B6B),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Button(
                onClick = {
                    if (name.isNotBlank() && confId.isNotBlank() && selectedDateMillis != null && address.isNotBlank()) {
                        scope.launch {
                            isLoading = true

                            val isoDateString = selectedDateMillis?.let { millis ->
                                val instant = Instant.fromEpochMilliseconds(millis)
                                val localDateTime = instant.toLocalDateTime(TimeZone.UTC)

                                localDateTime.date.toString()
                            } ?: ""

                            // Pass the new fields to your model
                            val newConf = Conference(
                                joinCode = confId,
                                name = name,
                                isPublic = isPublic,
                                dateString = isoDateString,
                                address = address
                            )
                            val result = repository.createConference(newConf)
                            isLoading = false
                            if (result.isSuccess) onConferenceCreated(confId)
                        }
                    }
                    else{
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
            ) {
                Text("Save and Manage Events", color = NavyBlue, fontWeight = FontWeight.Bold)
            }
        }
        TextButton(onClick = onBack) { Text("Return to Home Page", color = Color.White) }
    }
}

// Helper to keep the main code clean
@Composable
fun StandardTextField(value: String, onValueChange: (String) -> Unit, label: String, accent: Color, silver: Color) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = silver,
            focusedBorderColor = accent,
            unfocusedBorderColor = silver.copy(alpha = 0.7f),
            focusedLabelColor = accent,
            unfocusedLabelColor = silver.copy(alpha = 0.8f),
        ),
        maxLines = 2
    )
}