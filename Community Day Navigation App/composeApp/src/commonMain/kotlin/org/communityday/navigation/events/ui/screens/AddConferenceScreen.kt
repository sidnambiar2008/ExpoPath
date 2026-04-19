package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Conference", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Conference Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confId,
            onValueChange = { confId = it },
            label = { Text("Access Code (e.g. TECH2026)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
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
                            val newConf = Conference(id = confId, name = name)
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
                Text("Save and Manage Events")
            }
        }

        TextButton(onClick = onBack) { Text("Cancel") }
    }
}