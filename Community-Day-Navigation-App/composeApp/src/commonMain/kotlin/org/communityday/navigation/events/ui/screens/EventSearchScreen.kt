package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import org.communityday.navigation.events.data.Conference
import org.communityday.navigation.events.data.SearchViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_back_arrow
import communitydaynavigationapp.composeapp.generated.resources.ic_close
import communitydaynavigationapp.composeapp.generated.resources.ic_lock
import communitydaynavigationapp.composeapp.generated.resources.ic_rightarrow
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSearchScreen(
    viewModel: SearchViewModel,
    onNavigateToJoinCode: (String) -> Unit,
    onDirectJoin: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val NavyBlue = Color(0xFF000033)
    val Turquoise = Color(0xFF40E0D0)
    val Silver = Color(0xFFC0C0C0)
    val ActionOrange = Color(0xFFFF8C00)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar( // The title to be centered
                title = {
                    Text(
                        text = "Find your Event",
                        style = MaterialTheme.typography.titleMedium, // Adjusted style for AppBar fit
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier.offset(y = 18.dp) // Positive y moves it DOWN
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_back_arrow),
                            contentDescription = "Back",
                            tint = ActionOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NavyBlue
                )
            )
        },
        containerColor = NavyBlue //
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().background(NavyBlue).padding(innerPadding).padding(16.dp)) {
            OutlinedTextField(
                value = viewModel.query,
                onValueChange = { viewModel.onQueryChange(it) },
                label = { Text("Search by event name...") },
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
                singleLine = true, // Prevents Enter from adding new rows
                trailingIcon = {
                    if (viewModel.isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else if (viewModel.query.isNotEmpty()) {
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
                            "No upcoming events found for \"${viewModel.query}\".\n\nTry retyping the event name, closing the app, retrying your internet, or turning on your cellular data ",
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
                            onNavigateToJoinCode(conference.joinCode)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConferenceResultRow(conference: Conference, onClick: () -> Unit) {
    val Silver = Color(0xFFC0C0C0)
    val Turquoise = Color(0xFF40E0D0)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Material 3 syntax
        colors = CardDefaults.cardColors(
            containerColor = Silver.copy(alpha = 0.2f), // Only 10% brightness
            contentColor = Color.White), // Keeps the text readable
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
                    tint = Color.Yellow.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            Icon(
                imageVector = vectorResource(Res.drawable.ic_rightarrow),
                contentDescription = "Join",
                tint = Turquoise
            )
        }
    }
}
