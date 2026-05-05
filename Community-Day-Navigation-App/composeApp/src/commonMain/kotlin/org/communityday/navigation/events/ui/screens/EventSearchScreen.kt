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
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign

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
    var conferenceToHide by remember { mutableStateOf<Conference?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar( // The title to be centered
                title = {
                  //  Text(
                   //     text = "Find your Event",
                    //    style = MaterialTheme.typography.titleMedium, // Adjusted style for AppBar fit
                     //   color = Color.White,
                      //  fontSize = 24.sp,
                       // modifier = Modifier.offset(y = 12.dp) // Positive y moves it DOWN
                    //)
                },
                navigationIcon = {
                    // We use a Row inside a clickable Box or TextButton to make the whole area touchable
                    TextButton(
                        onClick = onBackClick,
                        contentPadding = PaddingValues(start = 8.dp) // Align it closer to the edge
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.ic_back_arrow),
                                    contentDescription = "Back",
                                    tint = Turquoise
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Return to Home",
                                color = Silver,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
            Text(
                text = "Search for your Event",
                color = Turquoise, // Use Silver or White so it's visible on Navy
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp).fillMaxWidth(), // Small start padding to align with the box
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
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
                    ConferenceResultRow(
                        conference = conference,
                        onClick = {
                            if (conference.isPublic) {
                                onDirectJoin(conference.joinCode)
                            } else {
                                onNavigateToJoinCode(conference.joinCode)
                            }
                        },
                        onLongClick = {
                            // Set the conference to show the dialog
                            conferenceToHide = conference
                        }
                    )
                }
            }
        }
        if (conferenceToHide != null) {
            AlertDialog(
                onDismissRequest = { conferenceToHide = null },
                containerColor = Color(0xFF1A1A4D),
                title = { Text("Hide Conference?", color = Color.White) },
                text = {
                    Text(
                        "Would you like to hide '${conferenceToHide?.name}'? It will no longer appear in your search results.",
                        color = Silver
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        conferenceToHide?.let { viewModel.hideConference(it.joinCode) }
                        conferenceToHide = null
                    }) {
                        Text("Hide", color = ActionOrange)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { conferenceToHide = null }) {
                        Text("Cancel", color = Silver)
                    }
                }
            )
        }
    }
}

@Composable
fun ConferenceResultRow(conference: Conference, onClick: () -> Unit, onLongClick: () -> Unit) {
    val Silver = Color(0xFFC0C0C0)
    val Turquoise = Color(0xFF40E0D0)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
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
