package org.communityday.navigation.events.mapDirectory

expect fun openMap(lat: Double, lon: Double, label: String, context: Any?)

// A helper function in commonMain that uses the expect
fun launchGoogleMap(lat: Double, lon: Double, label: String, context: Any?) {
    openMap(lat, lon, label, context)
}