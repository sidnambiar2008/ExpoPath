package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import communitydaynavigationapp.composeapp.generated.resources.Res
import communitydaynavigationapp.composeapp.generated.resources.ic_back_arrow
import kotlinx.coroutines.launch
import org.communityday.navigation.events.data.EventRepository
import org.jetbrains.compose.resources.vectorResource
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinConferenceScreen(
    initialCode: String = "", // <--- Add this parameter
    onConferenceJoined: (String) -> Unit,
    NavyBlue: Color,
    Turquoise: Color,
    Silver: Color,
    ActionOrange: Color,
    onBackClick: () -> Unit
) {
    var confCode by remember { mutableStateOf("") }
    val repository = remember { EventRepository() }
    var isVerifying by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar( // The title to be centered
                title = {},
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
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyBlue)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Enter Access Code",
                style = MaterialTheme.typography.headlineSmall,
                color = Turquoise
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter the Event Code Provided by the Organizer",
                color = Silver,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confCode,
                onValueChange = { confCode = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Code") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Turquoise,
                    unfocusedLabelColor = Silver,
                    focusedBorderColor = Turquoise,
                    unfocusedBorderColor = Silver,
                    cursorColor = Turquoise,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (confCode.isNotBlank()) {
                        isVerifying = true
                        showError = false

                        scope.launch {
                            // We attempt to fetch the conference details first
                            repository.getConferenceById(confCode.trim()).collect { conference ->
                                if (conference != null) {
                                    // SUCCESS: The code exists!
                                    isVerifying = false
                                    onConferenceJoined(confCode.trim())
                                } else {
                                    // FAIL: Code doesn't exist in Firebase
                                    isVerifying = false
                                    showError = true
                                }
                            }
                        }
                    }
                },
                enabled = !isVerifying,
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NavyBlue)
                } else {
                    Text("Join Conference", color = NavyBlue, fontWeight = FontWeight.Bold)
                }
            }

        // Add an error message text below the button
            if (showError) {
                Text(
                    text = "Invalid code. Please check and try again.",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}