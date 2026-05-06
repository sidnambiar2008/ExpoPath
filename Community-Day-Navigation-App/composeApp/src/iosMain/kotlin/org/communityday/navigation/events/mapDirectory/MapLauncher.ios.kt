package org.communityday.navigation.events.mapDirectory
import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import kotlinx.cinterop.useContents // Add this import
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import kotlinx.cinterop.ExperimentalForeignApi
import org.communityday.navigation.events.data.Conference

actual fun openMap(
    lat: Double,
    lon: Double,
    label: String,
    conferenceAddress: String, // Back to being a simple String
    context: Any?
) {
    val cleanLabel = label.trim().replace("&", "and").replace(" ", "+")
    val cleanConfAddr = conferenceAddress.trim().replace(" ", "+")

    val hasGps = lat != 0.0 && lon != 0.0
    val googleUrlString: String
    val appleUrlString: String
    val webFallbackUrl: String

    if (hasGps) {
        // --- GPS MODE ---
        // We use the coordinates for precision and the label for the UI
        googleUrlString = "comgooglemaps://?q=$lat,$lon($cleanLabel)&center=$lat,$lon&zoom=15"
        appleUrlString = "maps://?ll=$lat,$lon&q=$cleanLabel"
        webFallbackUrl = "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
    } else {
        // We put the address in 'q' so the pin drops correctly.
        // We put the booth name in 'label' to try and force the UI text.
        val query = cleanConfAddr.ifBlank { cleanLabel }

        // label parameter is specifically for the display text
        googleUrlString = "comgooglemaps://?q=$query&label=$cleanLabel"

        // Apple Maps usually handles the 'q' better than Google
        appleUrlString = "maps://?q=$cleanLabel,+$cleanConfAddr"

        webFallbackUrl = "https://www.google.com/maps/search/?api=1&query=$cleanLabel%20$cleanConfAddr"
    }

    val application = UIApplication.sharedApplication
    val googleMapsAppUrl = NSURL.URLWithString(googleUrlString)
    val appleMapsUrl = NSURL.URLWithString(appleUrlString)
    val webUrl = NSURL.URLWithString(webFallbackUrl)

    // Selection Logic: Google -> Apple -> Web Browser
    when {
        googleMapsAppUrl != null && application.canOpenURL(googleMapsAppUrl) -> {
            application.openURL(googleMapsAppUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
        }
        appleMapsUrl != null && application.canOpenURL(appleMapsUrl) -> {
            application.openURL(appleMapsUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
        }
        else -> {
            webUrl?.let { application.openURL(it, options = emptyMap<Any?, Any?>(), completionHandler = null) }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual class LocationProvider actual constructor() {
    private val locationManager = CLLocationManager()


    private class LocationDelegate(
        private val onLocationReceived: (Double, Double) -> Unit
    ) : NSObject(), CLLocationManagerDelegateProtocol {

        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return

            // horizontalAccuracy represents the margin of error in meters
            // A lower number is BETTER.
            val accuracy = location.horizontalAccuracy


            // Only accept the location if it's accurate within 10 meters
            // If it's 50 or 100, we wait for the next update.
            if (accuracy > 0 && accuracy <= 10.0) {
                location.coordinate.useContents {
                    onLocationReceived(latitude, longitude)
                }
                manager.stopUpdatingLocation()
            } else {
                // We don't stopUpdatingLocation here; we let it keep trying.
            }
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: platform.Foundation.NSError) {
            onLocationReceived(0.0, 0.0)
            manager.stopUpdatingLocation()
            manager.delegate = null
        }

        // Use the old version if the new one won't compile,
        // but this one is standard for modern KMP:
        override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: platform.CoreLocation.CLAuthorizationStatus) {
            if (didChangeAuthorizationStatus == platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse ||
                didChangeAuthorizationStatus == platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways) {
                manager.startUpdatingLocation()
            }
        }
    }

    // We keep this reference outside the function to prevent Garbage Collection
    private var activeDelegate: LocationDelegate? = null

    actual fun getCurrentLocation(onLocationReceived: (Double, Double) -> Unit) {
        println("📍 GPS DEBUG: getCurrentLocation called")

        // 1. Setup hardware settings
        locationManager.desiredAccuracy = platform.CoreLocation.kCLLocationAccuracyBestForNavigation
        locationManager.distanceFilter = -1.0

        // 2. Setup Delegate
        println("📍 GPS DEBUG: Setting up delegate")
        activeDelegate = LocationDelegate(onLocationReceived)
        locationManager.delegate = activeDelegate

        // 3. Check Status
        val status = locationManager.authorizationStatus
        println("📍 GPS DEBUG: Current Auth Status: $status")

        when (status) {
            platform.CoreLocation.kCLAuthorizationStatusNotDetermined -> {
                println("📍 GPS DEBUG: Status is NotDetermined - Requesting Popup...")
                locationManager.requestWhenInUseAuthorization()
            }
            platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse,
            platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways -> {
                println("📍 GPS DEBUG: Status is Authorized - Calling startUpdatingLocation()")
                locationManager.startUpdatingLocation()
            }
            platform.CoreLocation.kCLAuthorizationStatusDenied,
            platform.CoreLocation.kCLAuthorizationStatusRestricted -> {
                println("📍 GPS DEBUG: Status is Denied/Restricted - Returning 0,0")
                onLocationReceived(0.0, 0.0)
            }
            else -> {
                println("📍 GPS DEBUG: Unknown Status ($status) - Starting anyway")
                locationManager.startUpdatingLocation()
            }
        }
    }
}

