package org.communityday.navigation.events.mapDirectory

import java.awt.Desktop
import java.net.URI

actual fun openMap(lat: Double, lon: Double, label: String, context:Any?) {
    // We use the same Universal Google Maps URL
    val encodedLabel = label.replace(" ", "+")
    val urlString = "https://www.google.com/maps/search/?api=1&query=$lat,$lon"

    // Desktop.getDesktop() is the standard Java way to open a browser
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(URI(urlString))
        }
    } else {
        // Fallback for systems where Desktop isn't supported (like some Linux distros)
        val runtime = Runtime.getRuntime()
        val os = System.getProperty("os.name").lowercase()

        when {
            os.contains("win") -> runtime.exec("round /c start $urlString")
            os.contains("mac") -> runtime.exec("open $urlString")
            os.contains("nix") || os.contains("nux") -> runtime.exec("xdg-open $urlString")
        }
    }
}