package week11.st185898.finalproject.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import week11.st185898.finalproject.ui.MainViewModel
import week11.st185898.finalproject.ui.SmartCampusColors
import week11.st185898.finalproject.ui.rememberMapViewWithLifecycle

@Composable
fun MapScreen(
    mainViewModel: MainViewModel,
    onNavigateHome: () -> Unit,
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()

    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var mapReady by remember { mutableStateOf(false) }

    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var selectedCampus by remember { mutableStateOf("Traf") }  // matches tab label

    var showSaveDialog by remember { mutableStateOf(false) }

    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }
    var placesClient by remember { mutableStateOf<PlacesClient?>(null) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    // Request location + initialize Places & client safely
    LaunchedEffect(Unit) {
        // ---- Location permission & last known location ----
        val granted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        fused.lastLocation.addOnSuccessListener {
            if (it != null) currentLocation = LatLng(it.latitude, it.longitude)
        }


        // ---- Places initialization + client (read from AndroidManifest) ----
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

            if (apiKey.isNullOrBlank()) {
                Log.w("MapScreen", "Google Maps API key missing in manifest meta-data.")
            } else {
                if (!Places.isInitialized()) {
                    Places.initialize(context.applicationContext, apiKey)
                }
                placesClient = Places.createClient(context)
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "Failed to load Maps API key / initialize Places", e)
        }
    }

        // When we get currentLocation AND map is ready, center on user
    LaunchedEffect(currentLocation, mapReady) {
        if (!mapReady || currentLocation == null) return@LaunchedEffect
        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(currentLocation!!, 16f)
        )
    }

    // Autocomplete search (only when PlacesClient is ready)
    LaunchedEffect(searchQuery, placesClient) {
        val client = placesClient
        if (client == null || searchQuery.length < 2) {
            predictions = emptyList()
            return@LaunchedEffect
        }

        val req = FindAutocompletePredictionsRequest.builder()
            .setQuery(searchQuery)
            .build()

        client.findAutocompletePredictions(req)
            .addOnSuccessListener { predictions = it.autocompletePredictions }
            .addOnFailureListener {
                Log.e("MapScreen", "findAutocompletePredictions failed", it)
                predictions = emptyList()
            }
    }

    // ----------------- UI START -----------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ---------- HEADER ----------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SmartCampusColors.CardSoft)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateHome() }
                ) {
                    Icon(Icons.Default.Home, "Home", tint = SmartCampusColors.DangerRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Home", color = SmartCampusColors.DangerRed)
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Smart Campus Assistant",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Campus Map & Spots",
                        color = SmartCampusColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
            }
        }

        // ---------- SEARCH BAR ----------
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp))
                .background(SmartCampusColors.Card)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = "", tint = Color.White)
                Spacer(Modifier.width(10.dp))

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search", color = SmartCampusColors.TextSecondary) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Optional helper text if Places didn’t init
        if (placesClient == null) {
            Text(
                text = "Search suggestions may be disabled (Places not initialized).",
                color = SmartCampusColors.TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        // ---------- AUTOCOMPLETE LIST ----------
        if (predictions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(SmartCampusColors.Card)
            ) {
                LazyColumn {
                    items(predictions) { prediction ->
                        Column(
                            modifier = Modifier
                                .clickable {
                                    val client = placesClient ?: return@clickable
                                    val req = FetchPlaceRequest.newInstance(
                                        prediction.placeId,
                                        listOf(Place.Field.LAT_LNG, Place.Field.NAME)
                                    )
                                    client.fetchPlace(req)
                                        .addOnSuccessListener { res ->
                                            val latLng = res.place.latLng
                                            if (latLng != null) {
                                                selectedLocation = latLng
                                                googleMap?.animateCamera(
                                                    CameraUpdateFactory.newLatLngZoom(
                                                        latLng,
                                                        16f
                                                    )
                                                )

                                                googleMap?.clear()
                                                addCampusPins(
                                                    googleMap,
                                                    mapViewModel,
                                                    selectedCampus
                                                )
                                                googleMap?.addMarker(
                                                    MarkerOptions()
                                                        .position(latLng)
                                                        .title(res.place.name ?: "Selected")
                                                        .icon(
                                                            BitmapDescriptorFactory.defaultMarker(
                                                                BitmapDescriptorFactory.HUE_YELLOW
                                                            )
                                                        )
                                                )
                                            }
                                        }
                                    searchQuery = prediction.getFullText(null).toString()
                                    predictions = emptyList()
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                prediction.getPrimaryText(null).toString(),
                                color = Color.White
                            )
                            Text(
                                prediction.getSecondaryText(null).toString(),
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- CAMPUS TABS ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Traf", "Davis", "HMC").forEach { campus ->
                Button(
                    onClick = {
                        selectedCampus = campus
                        selectedLocation = null
                        googleMap?.clear()
                        addCampusPins(googleMap, mapViewModel, campus)
                        googleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                mapViewModel.getCampusCenter(campus),
                                16f
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(SmartCampusColors.AccentCyan)
                ) {
                    Text(
                        campus,
                        color = Color.Black,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- GOOGLE MAP CARD ----------
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(SmartCampusColors.Card)
        ) {
            Box(Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        MapsInitializer.initialize(ctx)
                        mapView.apply {
                            getMapAsync { map ->
                                googleMap = map
                                mapReady = true

                                map.uiSettings.isZoomControlsEnabled = false
                                map.uiSettings.isMyLocationButtonEnabled = true

                                // Enable blue dot if permission granted
                                if (ActivityCompat.checkSelfPermission(
                                        ctx,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED ||
                                    ActivityCompat.checkSelfPermission(
                                        ctx,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    map.isMyLocationEnabled = true
                                }

                                val initialTarget =
                                    currentLocation ?: mapViewModel.getCampusCenter(selectedCampus)
                                map.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(initialTarget, 16f)
                                )

                                addCampusPins(map, mapViewModel, selectedCampus)

                                map.setOnMapClickListener {
                                    selectedLocation = it
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(it)
                                            .title("Selected")
                                            .icon(
                                                BitmapDescriptorFactory.defaultMarker(
                                                    BitmapDescriptorFactory.HUE_YELLOW
                                                )
                                            )
                                    )
                                }

                                map.setOnMarkerClickListener { marker ->
                                    selectedLocation = marker.position
                                    map.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            marker.position, 17f
                                        )
                                    )
                                    false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // FLOATING NAVIGATION BUTTON
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            val loc = selectedLocation ?: return@clickable
                            val origin =
                                currentLocation ?: mapViewModel.getCampusCenter(selectedCampus)
                            val url =
                                "https://www.google.com/maps/dir/?api=1&origin=${origin.latitude}," +
                                        "${origin.longitude}&destination=${loc.latitude},${loc.longitude}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Navigation,
                        contentDescription = "Navigate",
                        tint = SmartCampusColors.AccentCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // RECENTER BUTTON
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SmartCampusColors.CardSoft)
                        .clickable {
                            val target =
                                currentLocation ?: mapViewModel.getCampusCenter(selectedCampus)
                            googleMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(target, 16f)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Recenter",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---------- SAVE + GET DIRECTIONS ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { if (selectedLocation != null) showSaveDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(SmartCampusColors.AccentGreen),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedLocation != null
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Save Location")
            }

            Button(
                onClick = {
                    val dest = selectedLocation ?: return@Button
                    val origin =
                        currentLocation ?: mapViewModel.getCampusCenter(selectedCampus)
                    val url =
                        "https://www.google.com/maps/dir/?api=1&origin=${origin.latitude}," +
                                "${origin.longitude}&destination=${dest.latitude},${dest.longitude}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(SmartCampusColors.AccentGreen),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedLocation != null
            ) {
                Icon(Icons.Outlined.Navigation, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Get Direction")
            }
        }
    }

    // ---------- SAVE POPUP ----------
    if (showSaveDialog && selectedLocation != null) {
        SaveLocationDialog(
            location = selectedLocation!!,
            onDismiss = { showSaveDialog = false },
            onSave = { name, desc ->
                mainViewModel.addFavorite(
                    name,
                    desc,
                    selectedLocation!!.latitude,
                    selectedLocation!!.longitude
                )
                showSaveDialog = false
            }
        )
    }
}

// Add all pins for a campus
fun addCampusPins(map: GoogleMap?, vm: MapViewModel, campus: String) {
    if (map == null) return
    vm.getLocationsForCampus(campus).forEach { loc ->
        map.addMarker(
            MarkerOptions()
                .position(loc.position)
                .title(loc.name)
                .snippet(loc.description)
        )
    }
}
