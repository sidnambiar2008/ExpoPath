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

actual fun openMap(
    lat: Double,
    lon: Double,
    label: String,
    conferenceAddress: String,
    context: Any?
) {
    val encodedLabel = label.trim().replace(" ", "+")
    val encodedAddress = conferenceAddress.trim().replace("\n", "+").replace(" ", "+")

    val searchQuery = if (lat == 0.0) {
        if (encodedAddress.isNotBlank()) {
            "$encodedLabel,+$encodedAddress" // Result: "Ooga,+123+Main+St"
        } else {
            encodedLabel // Result: "Ooga" (At least no trailing comma!)
        }
    } else {
        encodedLabel
    }

    val googleUrlString: String
    val appleUrlString: String
    val webFallbackUrl: String // Added for users without the apps

    if (lat != 0.0 && lon != 0.0) {
        googleUrlString = "comgooglemaps://?q=$lat,$lon&center=$lat,$lon&zoom=15&views=traffic"
        appleUrlString = "maps://?ll=$lat,$lon&q=$encodedLabel"
        webFallbackUrl = "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
    } else {
        // SEARCH MODE: Fallback to address/label
        googleUrlString = "comgooglemaps://?q=$searchQuery"
        appleUrlString = "maps://?q=$searchQuery"
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

@OptIn(ExperimentalForeignApi::class)
actual class LocationProvider actual constructor() {
    private val locationManager = CLLocationManager()
    

    private class LocationDelegate(
        private val onLocationReceived: (Double, Double) -> Unit
    ) : NSObject(), CLLocationManagerDelegateProtocol {

        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation
            location?.coordinate?.useContents {
                onLocationReceived(latitude, longitude)
            }
            manager.stopUpdatingLocation()
            manager.delegate = null // Clean up connection
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: platform.Foundation.NSError) {
            println("GPS Error Code: ${didFailWithError.code}")
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
        // 1. Lower accuracy = Faster results (Nearest 10 Meters)
        locationManager.desiredAccuracy = 10.0 // Raw double for 10 meters
        locationManager.distanceFilter = -1.0 // Raw double for "None"

        // 2. Create and STORE the delegate strongly
        activeDelegate = LocationDelegate(onLocationReceived)
        locationManager.delegate = activeDelegate

        val status = locationManager.authorizationStatus

        when (status) {
            platform.CoreLocation.kCLAuthorizationStatusNotDetermined -> {
                locationManager.requestWhenInUseAuthorization()
            }
            platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse,
            platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways -> {
                // startUpdatingLocation is usually more reliable than requestLocation in KMP
                locationManager.startUpdatingLocation()
            }
            else -> {
                onLocationReceived(0.0, 0.0)
            }
        }
    }
}

