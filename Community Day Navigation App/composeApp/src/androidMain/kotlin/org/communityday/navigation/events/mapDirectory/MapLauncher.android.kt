package org.communityday.navigation.events.mapDirectory
import android.content.Intent
import android.net.Uri
import android.content.Context
import java.lang.ref.WeakReference


object AndroidMapConfig {
    private var _context: WeakReference<Context>? = null
    var context: Context?
        get() = _context?.get()
        set(value) { _context = value?.let { WeakReference(it) } }
}
@Suppress("WrongConstant") // This specifically targets the flag error
actual fun openMap(lat: Double, lon: Double, label: String, context: Any?) {
    // Check the passed context first, then our global backup
    val actualContext = (context as? android.content.Context) ?: AndroidMapConfig.context

    if (actualContext != null) {
        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($label)")
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        actualContext.startActivity(intent)
    } else {
        android.util.Log.e("MAP_ERROR", "No context found to launch map!")
    }
}


