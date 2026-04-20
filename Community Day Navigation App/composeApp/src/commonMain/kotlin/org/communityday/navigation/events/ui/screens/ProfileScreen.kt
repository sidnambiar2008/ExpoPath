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
import communitydaynavigationapp.composeapp.generated.resources.ic_edit
import communitydaynavigationapp.composeapp.generated.resources.ic_manageaccount
import org.communityday.navigation.events.data.Conference
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource


@Composable
fun ProfileScreen(
    repository: EventRepository,
    onNavigateToManageList: () -> Unit, // 👈 New navigation trigger
    Turquoise: Color
) {
    // We can still keep this check to show a count, or just show the button
    val myConferences by repository.getManagedConferencesStream().collectAsState(initial = emptyList<Conference>())
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Account", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // --- THE MANAGE OPTION ---
        Surface(
            onClick = onNavigateToManageList, // Takes them to the list screen
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${myConferences.size} conference(s) created",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // A small arrow to indicate it's clickable
                Text(">", fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Optional Logout at bottom
        TextButton(
            onClick = { /* Handle Logout */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Log Out", color = Color.Red)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage My Conferences") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_edit),
                            contentDescription = null,
                            tint = Turquoise,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
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
                        onClick = { onConferenceSelected(conf.id) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(conf.name, style = MaterialTheme.typography.titleLarge)
                                Text("ID: ${conf.id}", style = MaterialTheme.typography.bodySmall)
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
        }
    }
}