package org.communityday.navigation.events.mapDirectory

expect fun openMap(lat: Double, lon: Double, label: String,conferenceAddress: String, context: Any?)

// A helper function in commonMain that uses the expect
fun launchGoogleMap(
    lat: Double,
    lon: Double,
    label: String,
    conferenceAddress: String, // 1. Add this parameter
    context: Any?
) {
    // 2. Pass it into the expect function
    openMap(lat, lon, label, conferenceAddress, context)
}


expect class LocationProvider() { // No 'val' here
    fun getCurrentLocation(onLocationReceived: (Double, Double) -> Unit)
}