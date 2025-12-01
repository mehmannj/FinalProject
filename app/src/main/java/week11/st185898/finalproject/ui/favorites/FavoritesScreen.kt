package week11.st185898.finalproject.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import week11.st185898.finalproject.data.FavoritePlace
import week11.st185898.finalproject.ui.SmartCampusColors

@Composable
fun FavoritesScreen(
    favorites: List<FavoritePlace>,
    onDelete: (FavoritePlace) -> Unit,
    onNavigateHome: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {


        // Header Card
        FavoritesHeaderCard(onHomeClick = onNavigateHome)

        // Saved Places heading
        Text(
            text = "Saved Places",
            color = SmartCampusColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You haven't saved any locations yet.",
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { place ->
                    FavoriteCard(place = place, onDelete = { onDelete(place) })
                }
            }
        }
    }
}

@Composable
private fun FavoritesHeaderCard(onHomeClick: () -> Unit = {}) {
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Home icon and text on left - clickable
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onHomeClick)
            ) {
                Icon(Icons.Default.Home, "Home", tint = SmartCampusColors.DangerRed)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Home", color = SmartCampusColors.DangerRed)
                Spacer(modifier = Modifier.width(12.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            // Centered title and subtitle
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Smart Campus Assistant",
                    color = SmartCampusColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Your Saved Locations",
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Empty space on right for balance
            Spacer(modifier = Modifier.width(60.dp))
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title
                Text(
                    text = place.name.ifBlank { "Unnamed Location" },
                    color = SmartCampusColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                Text(
                    text = place.description.ifBlank { "No description" },
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Location with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = SmartCampusColors.AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${"%.4f".format(place.latitude)}° ${if (place.latitude >= 0) "N" else "S"}, ${"%.4f".format(place.longitude)}° ${if (place.longitude >= 0) "E" else "W"}",
                        color = SmartCampusColors.AccentCyan,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable {
                            val destination = LatLng(place.latitude, place.longitude)
                            val origin = currentLocation ?: LatLng(43.4567, -79.6800) // Use campus center as fallback
                            week11.st185898.finalproject.ui.map.showDirections(context, origin, destination)
                        }
                    )
                }
            }

            // Delete icon on right
            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = SmartCampusColors.DangerRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

