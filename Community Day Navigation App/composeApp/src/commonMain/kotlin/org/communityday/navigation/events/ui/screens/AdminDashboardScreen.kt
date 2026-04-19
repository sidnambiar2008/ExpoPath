package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.communityday.navigation.events.data.EventRepository

@Composable
fun AdminDashboardScreen(
    confId: String,
    repository: EventRepository,
    onBack: () -> Unit
) {
    var showEventDialog by remember { mutableStateOf(false) }
    var showBoothDialog by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Managing: $confId",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text("Conference Dashboard", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { showEventDialog = true }, // Opens our upcoming dialog
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add New Event")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { showBoothDialog = true }, // Opens our upcoming dialog
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Add New Booth")
            }

            Spacer(Modifier.weight(1f))

            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Exit Dashboard")
            }
        }
    }
    // This is where we will "call" the dialogs once we build them
    if (showEventDialog) {
        // AddEventDialog(onDismiss = { showEventDialog = false }, confId = confId)
    }
}

@Composable
fun AddEventDialog(
    confId: String,
    repository: EventRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") } // Updated
    var endTime by remember { mutableStateOf("") }   // Updated
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var latText by remember { mutableStateOf("") }
    var lonText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start (e.g. 9:00)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End (e.g. 10:30)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Lat") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = lonText,
                        onValueChange = { lonText = it },
                        label = { Text("Long") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && latText.isNotBlank() && lonText.isNotBlank() && !isSaving,
                onClick = {
                    scope.launch {
                        isSaving = true
                        // Ensure these names match your Event data class exactly!
                        val newEvent = org.communityday.navigation.events.data.Event(
                            title = title,
                            description = description,
                            startTime = startTime,
                            endTime = endTime,
                            latitude = latText.toDoubleOrNull() ?: 0.0,
                            longitude = lonText.toDoubleOrNull() ?: 0.0
                        )
                        val result = repository.addEvent(confId, newEvent)
                        isSaving = false
                        if (result.isSuccess) {
                            onSuccess()
                        }
                        else
                        {
                            isSaving = false
                        }
                    }
                }
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddBoothDialog(
    confId: String,
    repository: EventRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf("") }
    var lonText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Booth") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Company / Organization Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Lat") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = lonText,
                        onValueChange = { lonText = it },
                        label = { Text("Long") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Brief Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && latText.isNotBlank() && lonText.isNotBlank() && !isSaving,
                onClick = {
                    scope.launch {
                        isSaving = true
                        // Ensure this matches your Booth data class exactly!
                        val newBooth = org.communityday.navigation.events.data.Booth(
                            name = name,
                            latitude = latText.toDoubleOrNull()?:0.0,
                            longitude = lonText.toDoubleOrNull()?:0.0,
                            description = description
                        )
                        // Make sure your repository has an addBooth function!
                        val result = repository.addBooth(confId, newBooth)
                        isSaving = true
                        if (result.isSuccess) {
                            onSuccess()
                        }
                        else
                        {
                            isSaving = false
                        }
                    }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save Booth")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}