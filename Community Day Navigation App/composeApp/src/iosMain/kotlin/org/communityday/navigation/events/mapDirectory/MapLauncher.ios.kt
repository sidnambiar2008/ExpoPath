package org.communityday.navigation.events.mapDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openMap(lat: Double, lon: Double, label: String, context: Any?) {
    val urlString = "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
    val url = NSURL.URLWithString(urlString)

    if (url != null) {
        UIApplication.sharedApplication.openURL(
            url = url!!,
            options = emptyMap<Any?, Any?>(),
            completionHandler = null
        )
    }
}
