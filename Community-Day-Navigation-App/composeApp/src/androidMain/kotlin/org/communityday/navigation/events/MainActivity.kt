package org.communityday.navigation.events

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.communityday.navigation.events.mapDirectory.AndroidMapConfig
import org.communityday.navigation.events.mapDirectory.LocationProvider
import androidx.compose.ui.platform.LocalContext
import org.communityday.navigation.events.notifications.NotificationScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 1. Set the static config so LocationProvider can find it internally
        AndroidMapConfig.context = this
        NotificationScheduler.context = applicationContext

        // 2. Initialize without passing a parameter
        val locationProvider = LocationProvider()

        setContent {
            // 3. Pass the clean provider to your App
            App(locationProvider = locationProvider)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // We provide a "dummy" provider just so the preview can render
    val dummyProvider = LocationProvider()
    App(locationProvider = dummyProvider)
}