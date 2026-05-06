package org.communityday.navigation.events.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_flag
import org.communityday.navigation.events.data.Booth
import org.communityday.navigation.events.mapDirectory.openMap
import org.jetbrains.compose.resources.vectorResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import communitydaynavigationapp.composeapp.generated.resources.ic_back_arrow
import communitydaynavigationapp.composeapp.generated.resources.ic_block
import kotlinx.coroutines.launch
import org.communityday.navigation.events.data.EventRepository

@Composable
fun BoothDetailScreen(
    booth: Booth,
    conferenceAddress: String, // Add this parameter
    onBackClick: () -> Unit,
    confId: String,
    repository: EventRepository
) {
    val context: Any? = null
    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    val Turquoise = Color(0xFF40E0D0)
    val CardNavy = Color(0xFF1A1A4D)
    val focusManager = LocalFocusManager.current // 1. Add this
    var showSafetyDialog by remember { mutableStateOf(false) }
    var showHideDialog by remember { mutableStateOf(false) }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val conference by repository.getConferenceById(confId).collectAsState(null)



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBlue)
            // 2. Add this modifier to the main container
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(16.dp)
    ) {
        // --- Navigation ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    tint = Turquoise
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Event Details",
                color = Silver,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Header Section ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = booth.name,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp
                )
            }
        }

        Text(
            text = booth.organization,
            color = Turquoise,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Category Badge ---
        Surface(
            color = CardNavy,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = booth.category.uppercase(),
                color = Silver,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- About Section ---
        Text(
            text = "About the Exhibitor",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = booth.description,
            color = Silver,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- New Smart Map Section ---
        // Show the button if we have coordinates OR a specific booth location string
        if ((booth.latitude != null && booth.longitude != null) || booth.location.isNotBlank()) {
            Button(
                onClick = {
                    openMap(
                        lat = booth.latitude ?: 0.0,
                        lon = booth.longitude ?: 0.0,
                        label = booth.location.ifBlank { booth.name },
                        conferenceAddress = conferenceAddress, // The "Neighborhood" Anchor
                        context = context
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Find Booth on Map", color = NavyBlue, fontWeight = FontWeight.Bold)
            }

            // Show the text below the button so they know which booth to look for
            if (booth.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Location: ${booth.location}",
                    color = Silver,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // Give it some space from the map info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BUTTON 1: REPORT
                IconButton(
                    onClick = {
                        showSafetyDialog = true
                    }
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_flag),
                        contentDescription = "Report Content",
                        tint = Color(0xFFCF6679).copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // A small vertical divider for a high-end look
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(Silver.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp)
                )

                // BUTTON 2: HIDE
                IconButton(
                    onClick = {
                        // You can show a simpler "Are you sure you want to hide?" dialog here
                        showHideDialog = true
                    }
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_block),
                        contentDescription = "Hide Conference",
                        tint = Color(0xFFCF6679).copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            if (showSafetyDialog) {
                AlertDialog(
                    onDismissRequest = { showSafetyDialog = false },
                    containerColor = Color(0xFF1A1A4D),
                    title = {
                        Text("Report Content", color = Color.White, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(
                            "You will be redirected to our community report form to provide details about this event.",
                            color = Silver,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showSafetyDialog = false
                                uriHandler.openUri("https://docs.google.com/forms/...")
                            }
                        ) {
                            // The main action button
                            Text("Report", color = Turquoise, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSafetyDialog = false }) {
                            Text("Cancel", color = Silver)
                        }
                    }
                )
            }
            if (showHideDialog) {
                AlertDialog(
                    onDismissRequest = { showHideDialog = false },
                    containerColor = Color(0xFF1A1A4D),
                    title = {
                        Text("Hide Conference?", color = Color.White, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column {
                            Text(
                                "You will no longer see '${conference?.name}' or any of its events in your search results.",
                                color = Silver,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "This action cannot be undone easily.",
                                color = Color(0xFFCF6679),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showHideDialog = false
                                scope.launch {
                                    repository.hideConference(confId)
                                    onBackClick() // Send them home immediately
                                }
                            }
                        ) {
                            // Keep the color red/pink to signal it's a permanent "removal"
                            Text("Hide", color = Color(0xFFCF6679), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showHideDialog = false }) {
                            Text("Cancel", color = Silver)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}