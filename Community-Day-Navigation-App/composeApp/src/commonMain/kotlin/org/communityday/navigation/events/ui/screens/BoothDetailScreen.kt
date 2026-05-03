package org.communityday.navigation.events.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.communityday.navigation.events.data.Booth
import org.communityday.navigation.events.mapDirectory.openMap

@Composable
fun BoothDetailScreen(
    booth: Booth,
    conferenceAddress: String, // Add this parameter
    onBackClick: () -> Unit
) {
    val context: Any? = null
    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    val Turquoise = Color(0xFF40E0D0)
    val CardNavy = Color(0xFF1A1A4D)
    val focusManager = LocalFocusManager.current // 1. Add this


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
        TextButton(
            onClick = onBackClick,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("< Back to Exhibitors", color = Turquoise, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Header Section ---
        Text(
            text = booth.name,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 38.sp
        )

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
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}