package org.communityday.navigation.events.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.communityday.navigation.events.data.Booth
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import org.communityday.navigation.events.data.EventRepository
import org.communityday.navigation.events.mapDirectory.openMap


@Composable
fun BoothListScreen(
    confCode: String,
    onBoothClick: (Booth) -> Unit,
    modifier: Modifier = Modifier
    )
{
    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    val ActionOrange = Color(0xFFFF8C00)
    val Turquoise = Color(0xFF40E0D0)

    var booths by remember {mutableStateOf<List<Booth>>(emptyList())}
    var isLoading by remember {mutableStateOf(true)}
    val repository = remember { EventRepository() }

    LaunchedEffect(confCode)
    {
        repository.getBoothsStream(confCode).collect{
            booths = it
            isLoading = false}
    }

    Column(modifier = modifier.fillMaxSize().background(NavyBlue).padding(16.dp).padding(top = 16.dp))
    {
        Text("Exhibitors", color = Silver, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading)
        {
            CircularProgressIndicator(color = Turquoise)
        }
        else{
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp))
            {
                items(booths) { booth ->
                    BoothCard(
                        booth = booth,
                        onClick = { onBoothClick(booth) } // Pass the click up
                    )
                }
            }
        }
    }
}

@Composable
fun BoothCard(
    booth: Booth,
    onClick: () -> Unit // Don't forget the click listener!
) {
    // Stick to your brand colors
    val NavyBlue = Color(0xFF000033)
    val Silver = Color(0xFFC0C0C0)
    val ActionOrange = Color(0xFFFF8C00)
    val Turquoise = Color(0xFF40E0D0)
    val CardBackground = Color(0xFF1A1A4D)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Navigation trigger
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Circle Avatar (ActionOrange adds a great pop of color)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(ActionOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = booth.organization.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = booth.name,
                    color = Silver,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = booth.organization,
                    color = Turquoise,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = booth.description,
                    color = Silver.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3. Category Badge (Clean and subtle)
            Surface(
                color = NavyBlue,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Silver.copy(alpha = 0.3f))
            ) {
                Text(
                    text = booth.category,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Silver,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
