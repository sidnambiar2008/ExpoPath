package org.communityday.navigation.events.mapDirectory
import kotlinx.browser.window

actual fun openMap(lat: Double, lon: Double, label: String, context: Any?) {
    // 1. Wrap the URL in quotes
    val url = "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
    window.open(url, "_blank")
}