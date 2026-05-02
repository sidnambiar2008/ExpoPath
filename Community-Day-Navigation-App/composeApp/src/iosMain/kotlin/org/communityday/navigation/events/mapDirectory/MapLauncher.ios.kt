package org.communityday.navigation.events.mapDirectory
import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import kotlinx.cinterop.useContents // Add this import

actual fun openMap(
    lat: Double,
    lon: Double,
    label: String,
    conferenceAddress: String,
    context: Any?
) {
    val encodedLabel = label.replace(" ", "+")
    val encodedAddress = conferenceAddress.trim().replace("\n", "+")

    // If no GPS, we "Anchor" the search to the building
    val searchQuery = if (lat == 0.0) "$encodedLabel,+$encodedAddress" else encodedLabel

    val googleUrlString: String
    val appleUrlString: String
    val webFallbackUrl: String // Added for users without the apps

    if (lat != 0.0 && lon != 0.0) {
        // EXACT MODE
        googleUrlString = "comgooglemaps://?q=$lat,$lon($encodedLabel)&center=$lat,$lon&zoom=17"
        appleUrlString = "http://maps.apple.com/?ll=$lat,$lon&q=$encodedLabel"
        webFallbackUrl = "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
    } else {
        // ANCHORED SEARCH MODE
        googleUrlString = "comgooglemaps://?q=$searchQuery"
        appleUrlString = "http://maps.apple.com/?q=$searchQuery"
        webFallbackUrl = "https://www.google.com/maps/search/?api=1&query=$searchQuery"
    }

    val googleMapsAppUrl = NSURL.URLWithString(googleUrlString)
    val appleMapsUrl = NSURL.URLWithString(appleUrlString)
    val webUrl = NSURL.URLWithString(webFallbackUrl)
    val application = UIApplication.sharedApplication

    // 1. Try Google Maps App
    if (googleMapsAppUrl != null && application.canOpenURL(googleMapsAppUrl)) {
        application.openURL(googleMapsAppUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
    }
    // 2. Try Apple Maps App
    else if (appleMapsUrl != null && application.canOpenURL(appleMapsUrl)) {
        application.openURL(appleMapsUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
    }
    // 3. Fallback to Safari (Google Maps Web) - This ensures it NEVER fails
    else if (webUrl != null) {
        application.openURL(webUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
    }
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class LocationProvider actual constructor() {
    private val locationManager = CLLocationManager()

    actual fun getCurrentLocation(onLocationReceived: (Double, Double) -> Unit) {
        locationManager.requestWhenInUseAuthorization()

        val location = locationManager.location
        if (location != null) {
            location.coordinate.useContents {
                onLocationReceived(latitude, longitude)
            }
        }
        else {
            // Optional: You could log something here or
            // trigger a one-time update request if you wanted to get fancy.
            println("iOS Location is currently null - GPS warming up or permission pending")
            onLocationReceived(0.0, 0.0)
        }
    }
}


