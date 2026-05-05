package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.communityday.navigation.events.data.EventRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items //
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_schedule
import org.jetbrains.compose.resources.vectorResource
import communitydaynavigationapp.composeapp.generated.resources.ic_delete
import communitydaynavigationapp.composeapp.generated.resources.ic_location_on
import org.communityday.navigation.events.utils.convertTimeToMinutes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay
import org.communityday.navigation.events.mapDirectory.LocationProvider
import kotlin.toString

@Composable
fun AdminDashboardScreen(
    confId: String,
    repository: EventRepository,
    onBack: () -> Unit,
    Turquoise: Color,
    isAttendeeModeActive: Boolean // Add this parameter

) {
    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)

    // State for tracking which item we are editing
    var editingEvent by remember { mutableStateOf<org.communityday.navigation.events.data.Event?>(null) }
    var editingBooth by remember { mutableStateOf<org.communityday.navigation.events.data.Booth?>(null) }

    var showEventDialog by remember { mutableStateOf(false) }
    var showBoothDialog by remember { mutableStateOf(false) }

    // Fetch the data
    val events by repository.getEventsStream(confId).collectAsState(initial = emptyList())
    val booths by repository.getBoothsStream(confId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var itemToDelete by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(id, type) where type is "Event" or "Booth"


    Scaffold( containerColor = NavyBlue) { //
         padding ->
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. HEADER & BUTTONS
                item {
                    Text("Edit Conference: $confId", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    // The Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // BUTTON TO ADD EVENT
                        Button(
                            onClick = {
                                editingEvent = null // Ensure it's fresh, not an edit
                                showEventDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                        ) {
                            Text("Add Event", color = Color.White)
                        }

                        // BUTTON TO ADD BOOTH
                        Button(
                            onClick = {
                                editingBooth = null
                                showBoothDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                        ) {
                            Text("Add Booth", color = Color.White)
                        }
                    }
                }

                // 2. EVENTS SECTION
                item { Text("Edit Events", style = MaterialTheme.typography.titleLarge, color = Color.White) }

                if (events.isEmpty()) {
                    item { Text("No events added yet.", color = Color.Gray) }
                } else {
                    items(events) { event ->
                        AdminCard(
                            title = event.title,
                            onClick = { editingEvent = event; showEventDialog = true },
                            onDelete = { itemToDelete = event.id to "Event" }

                        )
                    }
                }

                // 3. BOOTHS SECTION
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Edit Booths", style = MaterialTheme.typography.titleLarge, color = Color.White)
                }

                if (booths.isEmpty()) {
                    item { Text("No booths added yet.", color = Color.Gray) }
                } else {
                    items(booths) { booth ->
                        AdminCard(
                            title = booth.name,
                            onClick = { editingBooth = booth; showBoothDialog = true },
                            onDelete = { itemToDelete = booth.id to "Booth" }
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(24.dp))
                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isAttendeeModeActive) "Exit to Profile Screen" else "Return to Home Screen",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

        // Dialogs
        if (showEventDialog) {
            AddEventDialog(
                confId = confId,
                repository = repository,
                initialEvent = editingEvent, // 👈 Pass the item we clicked
                onDismiss = { showEventDialog = false },
                onSuccess = { showEventDialog = false },
                Turquoise = Turquoise
            )
        }

        if (showBoothDialog) {
            AddBoothDialog(
                confId = confId,
                repository = repository,
                initialBooth = editingBooth, // 👈 Add similar logic to Booth Dialog
                onDismiss = { showBoothDialog = false },
                onSuccess = { showBoothDialog = false },
                Turquoise = Turquoise
            )
        }
        if (itemToDelete != null) {
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("Confirm Delete") },
                text = {
                    Text("Are you sure you want to delete this ${itemToDelete?.second?.lowercase()}? This action cannot be undone.")
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = {
                            val (id, type) = itemToDelete!!
                            scope.launch {
                                if (type == "Event") {
                                    repository.deleteEvent(confId, id)
                                } else {
                                    repository.deleteBooth(confId, id)
                                }
                                itemToDelete = null // Close dialog after delete
                            }
                        }
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddEventDialog(
    confId: String,
    repository: EventRepository,
    initialEvent: org.communityday.navigation.events.data.Event? = null, // A Null Event
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    Turquoise:Color
) {

    var title by remember(initialEvent) { mutableStateOf(initialEvent?.title ?: "") }
    var description by remember(initialEvent) { mutableStateOf(initialEvent?.description ?: "") }
    var startTime by remember(initialEvent) { mutableStateOf(initialEvent?.startTime ?: "") }
    var endTime by remember(initialEvent) { mutableStateOf(initialEvent?.endTime ?: "") }
    var latText by remember(initialEvent) { mutableStateOf(initialEvent?.latitude?.toString() ?: "") }
    var lonText by remember(initialEvent) { mutableStateOf(initialEvent?.longitude?.toString() ?: "") }
    var location by remember(initialEvent) {mutableStateOf(initialEvent?.location?: "")}

    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    // ... title and description states ...

    // States for controlling the pickers
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val startTimeState = rememberTimePickerState(is24Hour = false)
    val endTimeState = rememberTimePickerState(is24Hour = false)
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    val locationProvider = remember {
        LocationProvider() // We handle context inside the 'actual' logic
    }
    var locationError by remember { mutableStateOf<String?>(null) }
    var capacityText by remember(initialEvent) {
        // If we have an event, get its capacity; otherwise null
        val currentCap = initialEvent?.capacity

        // Logic: If it's -1 or null, show an empty string so the placeholder/RequiredLabel can work.
        // Otherwise, show the actual number.
        val displayValue = if (currentCap == null || currentCap == -1) "" else currentCap.toString()

        mutableStateOf(displayValue)
    }

    LaunchedEffect(locationError) {
        if (locationError != null) {
            delay(5000)      // Wait 5 seconds
            locationError = null // Clear the error (it disappears from UI)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialEvent == null) "Add New Event" else "Edit Event") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp), ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { RequiredLabel("Event Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // START TIME FIELD
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = {}, // Read-only
                        label = { RequiredLabel("Start Time") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showStartPicker = true }) {
                                Icon(vectorResource(Res.drawable.ic_schedule), contentDescription = null)
                            }
                        }
                    )
                    // END TIME FIELD
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = {}, // Read-only
                        label = { RequiredLabel("End Time") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showEndPicker = true }) {
                                Icon(vectorResource(Res.drawable.ic_schedule), contentDescription = null)
                            }
                        }
                    )
                }

                // --- PICKER DIALOGS ---
                if (showStartPicker) {
                    TimeSelectionDialog(
                        state = startTimeState,
                        onDismiss = { showStartPicker = false },
                        onConfirm = {
                            startTime = formatTime(startTimeState.hour, startTimeState.minute)
                            showStartPicker = false
                        }
                    )
                }

                if (showEndPicker) {
                    TimeSelectionDialog(
                        state = endTimeState,
                        onDismiss = { showEndPicker = false },
                        onConfirm = {
                            endTime = formatTime(endTimeState.hour, endTimeState.minute)
                            showEndPicker = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp) // 2dp is a bit tight for fingers!
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = {
                            Text(
                                text = "Latitude (Optional)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OutlinedTextField(
                        value = lonText,
                        onValueChange = { lonText = it },
                        label = {
                            Text(
                                text = "Longitude (Optional)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // The GPS Button
                Button(
                    onClick = {
                        isLocating = true
                        locationProvider.getCurrentLocation { lat, lon ->
                            // This code runs ONLY when the Delegate finally gets a result
                            if (lat != 0.0 && lon != 0.0) {
                                latText = lat.toString()
                                lonText = lon.toString()
                                locationError = null
                            } else {
                                // If it returns 0.0 after a long wait, it's likely a timeout or deny
                                locationError = "GPS timeout. Try moving closer to a window."
                            }
                            // STOP the spinner
                            isLocating = false
                        }
                    },
                    enabled = !isLocating,
                    colors = ButtonDefaults.buttonColors(
                        // If already pinned, maybe slightly fade it or keep it vibrant
                        containerColor = Turquoise,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_location_on),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            // Visual cue: "Update" vs "Pin"
                            Text(
                                text = if (latText.isNotBlank()) "Update Exact GPS" else "Pin Exact GPS",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                Text(
                    text = "Tip: Pin exact GPS for better map accuracy, or leave blank to use the address. This will automatically fill in your latitude and longitude.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                )
                locationError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error, // Standard Red
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                    )
                }
                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { capacityText = it },
                    label = { RequiredLabel("Capacity") }, // Shortened label so it doesn't wrap weirdly
                    placeholder = { Text("e.g. 1000000 for unlimited") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {
                        Text(
                            text = "Description (Details, Links, etc)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1,
                    maxLines = 6
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { RequiredLabel("Location/Address") },
                    placeholder = { Text("e.g. Room 204 or 123 Main St") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && location.isNotBlank() && capacityText.isNotBlank() && !isSaving,
                onClick = {
                    val startMin = convertTimeToMinutes(startTime)
                    val endMin = convertTimeToMinutes(endTime)
                    val capacityInt = capacityText.toIntOrNull()
                    when {
                        title.isBlank() -> {
                            errorMessage = "Event Title is required."
                        }

                        location.isBlank() -> errorMessage = "Location Address (or Room #) is required."

                        startTime.isBlank() || endTime.isBlank() -> {
                            errorMessage = "Please select both Start and End times."
                        }

                        capacityText.isBlank() || capacityInt == null -> {
                            errorMessage = "Capacity is required (Enter a large number for unlimited)."
                        }

                        endMin <= startMin -> {
                            errorMessage = "End time must be after start time."
                        }

                        (latText.isNotBlank() && latText.toDoubleOrNull() == null) ||
                                (lonText.isNotBlank() && lonText.toDoubleOrNull() == null) -> {
                            errorMessage = "If providing GPS, please enter valid coordinates."
                        }

                        else -> {
                            errorMessage = null
                            scope.launch {
                                isSaving = true
                                val eventData = org.communityday.navigation.events.data.Event(
                                    id = initialEvent?.id ?: "",
                                    title = title,
                                    description = description,
                                    startTime = startTime,
                                    endTime = endTime,
                                    latitude = latText.toDoubleOrNull(),
                                    longitude = lonText.toDoubleOrNull(),
                                    location = location, // Make sure location is here too
                                    sortOrder = 1,
                                    capacity = capacityInt, // ADD THIS
                                    registeredCount = initialEvent?.registeredCount ?: 0
                                )

                                // Switch between add and update
                                val result = if (initialEvent == null) {
                                    repository.addEvent(confId, eventData)
                                } else {
                                    repository.updateEvent(
                                        confId,
                                        eventData
                                    )
                                }

                                isSaving = false
                                if (result.isSuccess) onSuccess()
                            }
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
    initialBooth: org.communityday.navigation.events.data.Booth? = null,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    Turquoise: Color
) {
    var name by remember(initialBooth) { mutableStateOf(initialBooth?.name ?: "") }
    var latText by remember(initialBooth) { mutableStateOf(initialBooth?.latitude?.toString() ?: "") }
    var lonText by remember(initialBooth) { mutableStateOf(initialBooth?.longitude?.toString() ?: "") }
    var description by remember(initialBooth) { mutableStateOf(initialBooth?.description?: "")}
    var isSaving by remember { mutableStateOf(false) }
    var location by remember(initialBooth) {mutableStateOf(initialBooth?.location?: "")}
    var isLocating by remember { mutableStateOf(false) }
    val locationProvider = remember {
        LocationProvider()
    }
    var locationError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(locationError) {
        if (locationError != null) {
            delay(5000)      // Wait 5 seconds
            locationError = null // Clear the error (it disappears from UI)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialBooth == null) "Add New Booth" else "Edit Booth") },
        text = {
            Column( modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { RequiredLabel("Company/Organization Name") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = {
                            Text(
                                text = "Latitude (Optional)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = lonText,
                        onValueChange = { lonText = it },
                        label = {  Text(
                            text = "Longitude (Optional)",
                            style = MaterialTheme.typography.labelSmall
                        ) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                // The GPS Button
                Button(
                    onClick = {
                        isLocating = true
                        locationProvider.getCurrentLocation { lat, lon ->
                            // This code runs ONLY when the Delegate finally gets a result
                            if (lat != 0.0 && lon != 0.0) {
                                latText = lat.toString()
                                lonText = lon.toString()
                                locationError = null
                            } else {
                                // If it returns 0.0 after a long wait, it's likely a timeout or deny
                                locationError = "GPS timeout. Try moving closer to a window."
                            }
                            // STOP the spinner
                            isLocating = false
                        }
                    },
                    enabled = !isLocating,
                    colors = ButtonDefaults.buttonColors(
                        // If already pinned, maybe slightly fade it or keep it vibrant
                        containerColor = Turquoise,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_location_on),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            // Visual cue: "Update" vs "Pin"
                            Text(
                                text = if (latText.isNotBlank()) "Update Exact GPS" else "Pin Exact GPS",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                locationError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error, // Standard Red
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                    )
                }
                Text(
                    text = "Tip: Pin exact GPS for better map accuracy, or leave blank to use the address. This will automatically fill in your latitude and longitude.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {  Text(
                        text = "Description (Links, Details, etc)",
                        style = MaterialTheme.typography.labelSmall
                    ) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { RequiredLabel("Location/Address") },
                    placeholder = { Text("e.g. Booth 1A or 123 Main St") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && location.isNotBlank() && !isSaving,
                onClick = {
                    if (name.isBlank() || location.isBlank()) {
                        println("Please add the Exhibitor's Name and the address of the booth.\nFor a precise booth location, click Pin Exact GPS")
                    }
                    else {
                        scope.launch {
                            isSaving = true
                            // Ensure this matches your Booth data class exactly!
                            val newBooth = org.communityday.navigation.events.data.Booth(
                                name = name,
                                id = initialBooth?.id ?: "",
                                latitude = latText.toDoubleOrNull(),
                                longitude = lonText.toDoubleOrNull(),
                                description = description,
                                location = location
                            )
                            // Make sure your repository has an addBooth function!
                            val result = if (initialBooth == null) {
                                repository.addBooth(confId, newBooth)
                            } else {
                                repository.updateBooth(confId, newBooth)
                            }

                            isSaving = false
                            if (result.isSuccess) {
                                onSuccess()
                            } else {
                                println("Error: ${result.exceptionOrNull()?.message}")
                            }
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

@Composable
fun AdminCard(
    title: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val Silver = Color(0xFFC0C0C0) // Define locally if not passed in
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick, // Tapping the card = EDIT
                colors = CardDefaults.cardColors(
                // 10% Silver makes it look deep navy-grey
                containerColor = Silver.copy(alpha = 0.1f),
        contentColor = Color.White
         ),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Silver.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, modifier = Modifier.weight(1f))

            IconButton(onClick = onDelete) { // Tapping the trash = DELETE
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_delete),
                    contentDescription = "Time",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = if (hour % 12 == 0) 12 else hour % 12
    val m = minute.toString().padStart(2, '0')
    return "$h:$m $amPm"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionDialog(
    state: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            TimePicker(state = state)
        }
    )
}

@Composable
fun RequiredLabel(text: String) {
    Text(buildAnnotatedString {
        append(text)
        withStyle(style = SpanStyle(color = Color.Red)) {
            append(" *")
        }
    }, style = MaterialTheme.typography.labelSmall)
}
