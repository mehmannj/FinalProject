package week11.st185898.finalproject.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import week11.st185898.finalproject.ui.MainViewModel
import week11.st185898.finalproject.ui.SmartCampusColors
import week11.st185898.finalproject.ui.rememberMapViewWithLifecycle
import week11.st185898.finalproject.ui.map.searchLocation
import week11.st185898.finalproject.ui.map.showDirections
import week11.st185898.finalproject.ui.map.moveCameraToCurrentLocation

@Composable
fun MapScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        val fine = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            hasLocationPermission = true
            // Get current location
            try {
                locationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            } catch (_: SecurityException) {}
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = {
                if (searchQuery.isNotBlank()) {
                    searchLocation(context, searchQuery, googleMap) { latLng ->
                        selectedLocation = latLng
                        googleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(latLng, 16f)
                        )
                    }
                }
            }
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = SmartCampusColors.Card
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        MapsInitializer.initialize(ctx)
                        mapView.apply {
                            getMapAsync { map ->
                                googleMap = map
                                map.uiSettings.isZoomControlsEnabled = false
                                map.uiSettings.isMyLocationButtonEnabled = true
                                if (hasLocationPermission) {
                                    try {
                                        map.isMyLocationEnabled = true
                                    } catch (_: SecurityException) {}
                                    moveCameraToCurrentLocation(locationClient, map)
                                }

                                // Sheridan College Trafalgar Campus (Oakville, Ontario)
                                val campusCenter = LatLng(43.4567, -79.6800)
                                
                                // Campus building locations
                                val library = LatLng(43.4570, -79.6805)
                                val cafeteria = LatLng(43.4565, -79.6795)
                                val scienceBuilding = LatLng(43.4572, -79.6802)
                                val sportsComplex = LatLng(43.4560, -79.6798)
                                val studentCenter = LatLng(43.4568, -79.6803)

                                // Add markers for campus buildings
                                map.addMarker(
                                    MarkerOptions()
                                        .position(library)
                                        .title("Sheridan Library")
                                        .snippet("Study & Research - Trafalgar Campus")
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_RED
                                            )
                                        )
                                )
                                map.addMarker(
                                    MarkerOptions()
                                        .position(cafeteria)
                                        .title("Student Cafeteria")
                                        .snippet("Dining Hall - Food Services")
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_AZURE
                                            )
                                        )
                                )
                                map.addMarker(
                                    MarkerOptions()
                                        .position(scienceBuilding)
                                        .title("Science Building")
                                        .snippet("Chemistry and Biology Labs")
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_GREEN
                                            )
                                        )
                                )
                                map.addMarker(
                                    MarkerOptions()
                                        .position(sportsComplex)
                                        .title("Sports Complex")
                                        .snippet("Gym, Pool & Fitness Facilities")
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_ORANGE
                                            )
                                        )
                                )
                                map.addMarker(
                                    MarkerOptions()
                                        .position(studentCenter)
                                        .title("Student Center")
                                        .snippet("Student Services & Activities")
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_VIOLET
                                            )
                                        )
                                )

                                map.moveCamera(
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.Builder()
                                            .target(campusCenter)
                                            .zoom(16f)
                                            .tilt(0f)
                                            .bearing(0f)
                                            .build()
                                    )
                                )

                                // Store selected marker reference
                                var selectedMarker: com.google.android.gms.maps.model.Marker? = null
                                
                                // Handle map click to select location
                                map.setOnMapClickListener { latLng ->
                                    selectedLocation = latLng
                                    // Remove previous selected marker if exists
                                    selectedMarker?.remove()
                                    // Add new selected location marker
                                    selectedMarker = map.addMarker(
                                        MarkerOptions()
                                            .position(latLng)
                                            .title("Selected Location")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                    )
                                }

                                map.setOnInfoWindowClickListener { marker ->
                                    selectedLocation = marker.position
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (!hasLocationPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SmartCampusColors.BackgroundGradientBottom.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Location permission is required to show the map.",
                            color = SmartCampusColors.TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    selectedLocation?.let {
                        showSaveDialog = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                enabled = selectedLocation != null,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SmartCampusColors.AccentGreen
                )
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Location",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    selectedLocation?.let { destination ->
                        currentLocation?.let { origin ->
                            showDirections(context, origin, destination)
                        } ?: run {
                            // Use campus center as fallback
                            val campusCenter = LatLng(43.4567, -79.6800)
                            showDirections(context, campusCenter, destination)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                enabled = selectedLocation != null,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SmartCampusColors.AccentGreen
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Get Direction",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Save Location Dialog
    if (showSaveDialog && selectedLocation != null) {
        SaveLocationDialog(
            location = selectedLocation!!,
            onDismiss = { showSaveDialog = false },
            onSave = { name, description ->
                viewModel.addFavorite(
                    name = name,
                    desc = description,
                    lat = selectedLocation!!.latitude,
                    lon = selectedLocation!!.longitude
                )
                showSaveDialog = false
                selectedLocation = null
            }
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SmartCampusColors.CardSoft
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = SmartCampusColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search", color = SmartCampusColors.TextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = SmartCampusColors.TextPrimary,
                    unfocusedTextColor = SmartCampusColors.TextPrimary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
            )
            if (query.isNotBlank()) {
                TextButton(onClick = onSearch) {
                    Text("Go", color = SmartCampusColors.AccentCyan)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveLocationDialog(
    location: LatLng,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Location", color = SmartCampusColors.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Location Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = SmartCampusColors.AccentCyan,
                        focusedBorderColor = SmartCampusColors.AccentGreen,
                        unfocusedLabelColor = SmartCampusColors.AccentCyan,
                        focusedLabelColor = SmartCampusColors.AccentGreen
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = SmartCampusColors.AccentCyan,
                        focusedBorderColor = SmartCampusColors.AccentGreen,
                        unfocusedLabelColor = SmartCampusColors.AccentCyan,
                        focusedLabelColor = SmartCampusColors.AccentGreen
                    ),
                    maxLines = 2
                )
                Text(
                    text = "${"%.6f".format(location.latitude)}, ${"%.6f".format(location.longitude)}",
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, description)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SmartCampusColors.AccentGreen
                )
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SmartCampusColors.TextSecondary)
            }
        },
        containerColor = SmartCampusColors.Card,
        titleContentColor = SmartCampusColors.TextPrimary,
        textContentColor = SmartCampusColors.TextPrimary
    )
}

