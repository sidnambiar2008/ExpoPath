package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.communityday.navigation.events.data.Conference // Import your Conference model
import org.communityday.navigation.events.data.EventRepository // Import your Repository

@Composable
fun AddConferenceScreen(
    repository: EventRepository,
    onConferenceCreated: (String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var confId by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val NavyBlue = Color(0xFF000033)
    val Turquoise = Color(0xFF40E0D0)
    val Silver = Color(0xFFC0C0C0)
    val ActionOrange = Color(0xFFFF8C00)
    val focusManager = LocalFocusManager.current


    Column(
        modifier = Modifier.fillMaxSize().background(NavyBlue).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Conference", style = MaterialTheme.typography.headlineMedium, color = Color.White)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Conference Name", color = Color.White) },
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

        )

        OutlinedTextField(
            value = confId,
            onValueChange = { confId = it },
            label = { Text("Access Code (e.g. TECH2026)", color = Color.White) },
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
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location", color = Color.White) },
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
        )

        Spacer(Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (name.isNotBlank() && confId.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            val newConf = Conference(joinCode = confId, name = name)
                            val result = repository.createConference(newConf)
                            isLoading = false

                            if (result.isSuccess) {
                                onConferenceCreated(confId)
                            }
                            else{
                                println("Firebase Error: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Save and Manage Events", color = Color.White)
            }
        }

        TextButton(onClick = onBack) { Text("Cancel", color = Color.White) }
    }
}