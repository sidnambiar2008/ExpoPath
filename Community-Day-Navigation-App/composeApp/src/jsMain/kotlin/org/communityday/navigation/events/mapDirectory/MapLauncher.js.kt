package org.communityday.navigation.events.mapDirectory

import kotlinx.browser.window


actual fun openMap(
    lat: Double,
    lon: Double,
    label: String,
    conferenceAddress: String,
    context: Any?
) {
    // Instead of encodeURIComponent, use the JS built-in via the 'js' function
    // or call it directly if your setup allows.

    val fullQuery = if (lat == 0.0) "$label, $conferenceAddress" else label

    // This calls the native JS function directly
    val encodedQuery = js("encodeURIComponent")(fullQuery) as String

    val url = if (lat != 0.0 && lon != 0.0) {
        "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
    } else {
        "https://www.google.com/maps/search/?api=1&query=$encodedQuery"
    }

    window.open(url, "_blank")
}

actual class LocationProvider { // Remove 'actual val'
    actual fun getCurrentLocation(onLocationReceived: (Double, Double) -> Unit) {
        // Use asDynamic() to bypass the missing type definitions
        val navigator = window.navigator.asDynamic()

        if (navigator.geolocation != null) {
            navigator.geolocation.getCurrentPosition(
                { pos: dynamic ->
                    val lat = pos.coords.latitude as Double
                    val lon = pos.coords.longitude as Double
                    onLocationReceived(lat, lon)
                },
                { error: dynamic ->
                    println("Browser Geolocation Error: ${error.message}")
                }
            )
        } else {
            println("Geolocation is not supported by this browser.")
        }
    }
}
