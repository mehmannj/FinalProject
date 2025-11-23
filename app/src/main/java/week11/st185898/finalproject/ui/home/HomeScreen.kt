package week11.st185898.finalproject.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st185898.finalproject.ui.MainViewModel
import week11.st185898.finalproject.ui.SmartCampusColors
import week11.st185898.finalproject.ui.favorites.FavoritesScreen
import week11.st185898.finalproject.ui.map.MapScreen
import week11.st185898.finalproject.ui.sensors.SensorDashboardScreen
import week11.st185898.finalproject.ui.home.HomeTab

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val favorites by viewModel.favorites.collectAsState()
    var selectedTab by remember { mutableStateOf(HomeTab.MAP) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        HeaderCard(
            selectedTab = selectedTab,
            onLogout = { viewModel.signOut() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExploreCard()

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStatCard(
                title = "Saved Spots",
                value = favorites.size.toString(),
                iconColor = SmartCampusColors.AccentCyan
            )
            MiniStatCard(
                title = "Visited",
                value = "8",
                iconColor = SmartCampusColors.AccentGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            when (selectedTab) {
                HomeTab.MAP -> MapScreen(viewModel)
                HomeTab.FAVORITES -> FavoritesScreen(
                    favorites = favorites,
                    onDelete = { viewModel.deleteFavorite(it.id) }
                )
                HomeTab.SENSORS -> SensorDashboardScreen()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}

@Composable
private fun HeaderCard(
    selectedTab: HomeTab,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SmartCampusColors.CardSoft
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = SmartCampusColors.DangerRed,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "Smart Campus Assistant",
                            color = SmartCampusColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = when (selectedTab) {
                                HomeTab.MAP -> "Campus Map & Spots"
                                HomeTab.FAVORITES -> "Your Saved Locations"
                                HomeTab.SENSORS -> "Activity & Sensors"
                            },
                            color = SmartCampusColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                TextButton(onClick = onLogout) {
                    Text(
                        text = "Logout",
                        color = SmartCampusColors.DangerRed,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Welcome User",
                color = SmartCampusColors.TextPrimary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ExploreCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SmartCampusColors.Card
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Explore Campus",
                    color = SmartCampusColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "View all campus locations on the interactive map",
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = "Map",
                tint = SmartCampusColors.AccentCyan,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun MiniStatCard(
    title: String,
    value: String,
    iconColor: Color
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(90.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SmartCampusColors.Card
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (title == "Saved Spots") Icons.Default.Star else Icons.Default.FitnessCenter,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                color = SmartCampusColors.TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = iconColor,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
    }
}

@Composable
private fun BottomNavBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SmartCampusColors.Card
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        NavigationBar(
            containerColor = Color.Transparent
        ) {
            NavigationBarItem(
                selected = selectedTab == HomeTab.MAP,
                onClick = { onTabSelected(HomeTab.MAP) },
                icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                label = { Text("Map") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SmartCampusColors.AccentCyan,
                    selectedTextColor = SmartCampusColors.AccentCyan,
                    unselectedIconColor = SmartCampusColors.TextSecondary,
                    unselectedTextColor = SmartCampusColors.TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
            NavigationBarItem(
                selected = selectedTab == HomeTab.FAVORITES,
                onClick = { onTabSelected(HomeTab.FAVORITES) },
                icon = { Icon(Icons.Default.Star, contentDescription = "Favorites") },
                label = { Text("Favorites") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SmartCampusColors.AccentCyan,
                    selectedTextColor = SmartCampusColors.AccentCyan,
                    unselectedIconColor = SmartCampusColors.TextSecondary,
                    unselectedTextColor = SmartCampusColors.TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
            NavigationBarItem(
                selected = selectedTab == HomeTab.SENSORS,
                onClick = { onTabSelected(HomeTab.SENSORS) },
                icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Sensors") },
                label = { Text("Sensors") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SmartCampusColors.AccentCyan,
                    selectedTextColor = SmartCampusColors.AccentCyan,
                    unselectedIconColor = SmartCampusColors.TextSecondary,
                    unselectedTextColor = SmartCampusColors.TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

