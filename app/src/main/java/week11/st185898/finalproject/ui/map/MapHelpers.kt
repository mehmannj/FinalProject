package week11.st185898.finalproject.ui.map

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

fun moveCameraToCurrentLocation(
    locationClient: FusedLocationProviderClient,
    googleMap: GoogleMap
) {
    try {
        locationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(latLng)
                                .zoom(17f)
                                .build()
                        )
                    )
                }
            }
    } catch (_: SecurityException) {
    }
}

fun searchLocation(
    context: Context,
    query: String,
    googleMap: GoogleMap?,
    onResult: (LatLng) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val addresses = geocoder.getFromLocationName(query, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val latLng = LatLng(address.latitude, address.longitude)
            onResult(latLng)
            googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(query)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }
    } catch (e: Exception) {
        // Handle geocoding error
    }
}

fun showDirections(
    context: Context,
    origin: LatLng,
    destination: LatLng
) {
    val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // If Google Maps not installed, open in browser
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(browserIntent)
    }
}

