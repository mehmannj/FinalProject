package week11.st185898.finalproject.ui.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.maps.model.LatLng

/**
 * Show directions using Google Maps
 */
fun showDirections(context: Context, origin: LatLng, destination: LatLng) {
    try {
        // Use Google Maps navigation
        val gmmIntentUri = Uri.parse(
            "google.navigation:q=${destination.latitude},${destination.longitude}" +
                    "&waypoints=${origin.latitude},${origin.longitude}"
        )
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback to web browser with Google Maps
            val webUri = Uri.parse(
                "https://www.google.com/maps/dir/${origin.latitude},${origin.longitude}/" +
                        "${destination.latitude},${destination.longitude}"
            )
            val browserIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        // Last resort: open in browser
        val webUri = Uri.parse(
            "https://www.google.com/maps/dir/${origin.latitude},${origin.longitude}/" +
                    "${destination.latitude},${destination.longitude}"
        )
        val browserIntent = Intent(Intent.ACTION_VIEW, webUri)
        context.startActivity(browserIntent)
    }
}