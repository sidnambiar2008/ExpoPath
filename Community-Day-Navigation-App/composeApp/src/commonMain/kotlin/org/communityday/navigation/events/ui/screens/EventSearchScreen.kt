package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import org.communityday.navigation.events.data.Conference
import org.communityday.navigation.events.data.SearchViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_close
import communitydaynavigationapp.composeapp.generated.resources.ic_lock
import communitydaynavigationapp.composeapp.generated.resources.ic_map
import communitydaynavigationapp.composeapp.generated.resources.ic_rightarrow
import org.jetbrains.compose.resources.vectorResource


@Composable
fun EventSearchScreen(
    viewModel: SearchViewModel,
    onNavigateToJoinCode: (String) -> Unit,
    onDirectJoin: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Find Your Event", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = viewModel.query,
            onValueChange = { viewModel.onQueryChange(it) },
            label = { Text("Search by event name...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true, // Prevents Enter from adding new rows
            trailingIcon = {
                if (viewModel.isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                else if (viewModel.query.isNotEmpty()) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_close), // Add a close icon to Res
                        contentDescription = "Clear",
                        modifier = Modifier.clickable { viewModel.onQueryChange("") }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            if (viewModel.results.isEmpty() && !viewModel.isSearching) {
                item {
                    Text(
                        "No upcoming events found for \"${viewModel.query}\"",
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            items(viewModel.results) { conference ->
                ConferenceResultRow(conference) {
                    if (conference.isPublic) {
                        onDirectJoin(conference.joinCode)
                    } else {
                        onNavigateToJoinCode(conference.joinCode)                    }
                }
            }
        }
    }
} // <--- End of EventSearchScreen

@Composable
fun ConferenceResultRow(conference: Conference, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Material 3 syntax
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(conference.name, fontWeight = FontWeight.Bold)
                    //Text(conference.location, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Code: ${conference.joinCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (!conference.isPublic) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_lock),
                    contentDescription = "Private",
                    tint = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            Icon(
                imageVector = vectorResource(Res.drawable.ic_rightarrow),
                contentDescription = "Join"
            )
        }
    }
}