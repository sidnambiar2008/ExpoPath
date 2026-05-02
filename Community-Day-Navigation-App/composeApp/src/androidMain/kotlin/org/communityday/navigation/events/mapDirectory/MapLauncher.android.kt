package org.communityday.navigation.events.mapDirectory
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.content.Context
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.lang.ref.WeakReference


object AndroidMapConfig {
    private var _context: WeakReference<Context>? = null
    var context: Context?
        get() = _context?.get()
        set(value) { _context = value?.let { WeakReference(it) } }
}
@Suppress("WrongConstant")
actual fun openMap(
    lat: Double,
    lon: Double,
    label: String,
    conferenceAddress: String, // Add this parameter
    context: Any?
) {
    val actualContext = (context as? android.content.Context) ?: AndroidMapConfig.context

    if (actualContext != null) {
        val uriString = if (lat != 0.0 && lon != 0.0) {
            // SCENARIO A: Exact GPS
            "geo:$lat,$lon?q=$lat,$lon(${Uri.encode(label)})"
        } else {
            // SCENARIO B: Anchored Search (e.g., "Room 204, USC Village")
            // This ensures Google Maps searches within the right building
            val fullQuery = "$label, $conferenceAddress"
            "geo:0,0?q=${Uri.encode(fullQuery)}"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            actualContext.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: If no Map app is installed, try opening in a browser
            val webUri = if (lat != 0.0 && lon != 0.0) {
                "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
            } else {
                "https://www.google.com/maps/search/?api=1&query=${Uri.encode("$label, $conferenceAddress")}"
            }
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri))
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            actualContext.startActivity(webIntent)
        }
    } else {
        Log.e("MAP_ERROR", "No context found to launch map!")
    }
}
actual class LocationProvider actual constructor() {
    @SuppressLint("MissingPermission")
    actual fun getCurrentLocation(onLocationReceived: (Double, Double) -> Unit) {
        try {
            // Use your singleton backup!
            val actualContext = AndroidMapConfig.context

            if (actualContext == null) {
                Log.e("GPS_DEBUG", "ERROR: Context is null. Did you set AndroidMapConfig.context in MainActivity?")
                onLocationReceived(0.0, 0.0)
                return
            }

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(actualContext)

            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (lastLoc != null) {
                    onLocationReceived(lastLoc.latitude, lastLoc.longitude)
                } else {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { freshLoc ->
                            onLocationReceived(freshLoc?.latitude ?: 0.0, freshLoc?.longitude ?: 0.0)
                        }
                }
            }
        } catch (e: Exception) {
            Log.e("GPS_DEBUG", "CRITICAL CRASH: ${e.message}")
            onLocationReceived(0.0, 0.0)
        }
    }
}



