package week11.st185898.finalproject.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import week11.st185898.finalproject.data.FavoritePlace
import week11.st185898.finalproject.ui.SmartCampusColors
import week11.st185898.finalproject.ui.map.showDirections

@Composable
fun FavoritesScreen(
    favorites: List<FavoritePlace>,
    onDelete: (FavoritePlace) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Saved Places",
            color = SmartCampusColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (favorites.isEmpty()) {
            Text(
                text = "You haven't saved any locations yet.",
                color = SmartCampusColors.TextSecondary,
                fontSize = 13.sp
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favorites) { place ->
                    FavoriteCard(place = place, onDelete = { onDelete(place) })
                }
            }
        }
    }
}

@Composable
fun FavoriteCard(
    place: FavoritePlace,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            locationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                }
            }
        } catch (_: SecurityException) {}
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SmartCampusColors.Card
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = place.name.ifBlank { "Unnamed Location" },
                    color = SmartCampusColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (place.description.isNotBlank()) {
                    Text(
                        text = place.description,
                        color = SmartCampusColors.TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "${"%.4f".format(place.latitude)}° ${if (place.latitude >= 0) "N" else "S"}, ${"%.4f".format(place.longitude)}° ${if (place.longitude >= 0) "E" else "W"}",
                        color = SmartCampusColors.AccentCyan,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable {
                            val destination = LatLng(place.latitude, place.longitude)
                            currentLocation?.let { origin ->
                                showDirections(context, origin, destination)
                            } ?: run {
                                // Use campus center as fallback
                                val campusCenter = LatLng(43.4567, -79.6800)
                                showDirections(context, campusCenter, destination)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Get Directions",
                        tint = SmartCampusColors.AccentCyan,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                val destination = LatLng(place.latitude, place.longitude)
                                currentLocation?.let { origin ->
                                    showDirections(context, origin, destination)
                                } ?: run {
                                    val campusCenter = LatLng(43.4567, -79.6800)
                                    showDirections(context, campusCenter, destination)
                                }
                            }
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = SmartCampusColors.DangerRed
                )
            }
        }
    }
}

