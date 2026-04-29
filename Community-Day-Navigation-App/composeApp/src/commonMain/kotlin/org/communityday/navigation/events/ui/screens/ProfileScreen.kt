package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.communityday.navigation.events.data.EventRepository
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_back_arrow
import communitydaynavigationapp.composeapp.generated.resources.ic_edit
import communitydaynavigationapp.composeapp.generated.resources.ic_manageaccount
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import org.communityday.navigation.events.data.Conference
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import kotlinx.coroutines.launch



@Composable
fun ProfileScreen(
    repository: EventRepository,
    onNavigateToManageList: () -> Unit, // 👈 New navigation trigger
    Turquoise: Color,
    onBackClick: () -> Unit,
) {
    // We can still keep this check to show a count, or just show the button
    val myConferences by repository.getManagedConferencesStream().collectAsState(initial = emptyList<Conference>())
    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(NavyBlue).padding(16.dp)) {
        Text("My Account", style = MaterialTheme.typography.headlineMedium, color = Color.White)

        Spacer(modifier = Modifier.height(32.dp))

        // --- THE MANAGE OPTION ---
        Surface(
            onClick = onNavigateToManageList, // Takes them to the list screen
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Using your Edit icon here
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_edit),
                    contentDescription = null,
                    tint = Turquoise,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Manage My Conferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        // CHANGED: Use Turquoise or White instead of Black
                        color = Turquoise
                    )
                    Text(
                        text = "${myConferences.size} conference(s) created",
                        style = MaterialTheme.typography.bodySmall,
                        // CHANGED: Use a bright Silver with 80% opacity so it's crisp but secondary
                        color = Silver.copy(alpha = 0.8f)
                    )
                }

                // A small arrow to indicate it's clickable
                Text(">", fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Optional Logout at bottom
        Button(
            onClick = { scope.launch {
                Firebase.auth.signOut()
                // Navigate the user back to the Welcome/Login screen
                onBackClick()
            } },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray.copy(alpha = 0.5f),

            )
        ) {
            Text("Log Out", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // 👈 Add this line
@Composable
fun ManageMyConferencesScreen(
    repository: EventRepository,
    onConferenceSelected: (String) -> Unit,
    Turquoise: Color,
    onBack: () -> Unit
) {
    val myConferences by repository.getManagedConferencesStream().collectAsState(emptyList())
    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    var conferenceToRename by remember { mutableStateOf<Conference?>(null) }
    var newNameText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()


    Scaffold(containerColor = NavyBlue,
        topBar = {
            TopAppBar(
                title = { Text("Manage My Conferences", color = Color.White) },

                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_back_arrow),
                            contentDescription = null,
                            tint = Turquoise,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyBlue // Match your background
                )
            )
        }
    ) { padding ->
        if (myConferences.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No conferences found. Create one to get started!", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                items(myConferences) { conf ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        onClick = { onConferenceSelected(conf.joinCode) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Silver.copy(alpha = 0.1f), // Only 10% brightness
                            contentColor = Color.White), // Keeps the text readable
                    )
                     {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(conf.name, style = MaterialTheme.typography.titleLarge)
                                Text("ID: ${conf.joinCode}", style = MaterialTheme.typography.bodySmall)
                            }

                            IconButton(onClick = {
                                conferenceToRename = conf
                                newNameText = conf.name
                            }) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.ic_edit),
                                    contentDescription = "Edit Name",
                                    tint = Turquoise
                                )
                            }

                            // The Google "Manage" Icon
                            Icon(
                                painter = painterResource(Res.drawable.ic_manageaccount),
                                contentDescription = "Manage",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp) // Optional: adjust size as needed
                            )
                        }
                    }
                }
            }

            if (conferenceToRename != null) {
                AlertDialog(
                    onDismissRequest = { conferenceToRename = null },
                    title = { Text("Rename Conference") },
                    text = {
                        OutlinedTextField(
                            value = newNameText,
                            onValueChange = { newNameText = it },
                            label = { Text("New Name") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val code = conferenceToRename?.joinCode ?: ""
                            scope.launch {
                                repository.updateConferenceName(code, newNameText)
                                conferenceToRename = null
                            }
                        }) { Text("Save", color = Turquoise) }
                    },
                    dismissButton = {
                        TextButton(onClick = { conferenceToRename = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}