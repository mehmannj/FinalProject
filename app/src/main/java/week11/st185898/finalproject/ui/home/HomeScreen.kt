package week11.st185898.finalproject.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val favorites by viewModel.favorites.collectAsState()
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top cards only when on HOME tab
        if (selectedTab == HomeTab.HOME) {
            HeaderCard(
                onLogout = { viewModel.signOut() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExploreCard(
                onClick = { selectedTab = HomeTab.MAP }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniStatCard(
                    title = "Saved Spots",
                    value = favorites.size.toString(),
                    iconColor = SmartCampusColors.AccentCyan,
                    modifier = Modifier.weight(1f)
                )
                MiniStatCard(
                    title = "Visited",
                    value = favorites.size.toString(),
                    iconColor = SmartCampusColors.AccentGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Middle content area (tabs)
        Box(
            modifier = Modifier.weight(1f)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith
                            fadeOut(animationSpec = tween(250))
                },
                label = "homeTabTransition"
            ) { tab ->
                when (tab) {
                    HomeTab.HOME -> {
                        // Empty centre area – matches Figma
                        Box(modifier = Modifier.fillMaxSize())
                    }

                    HomeTab.MAP -> {
                        MapScreen(
                            mainViewModel = viewModel,
                            onNavigateHome = { selectedTab = HomeTab.HOME },
                        )

                    }

                    HomeTab.FAVORITES -> {
                        FavoritesScreen(
                            favorites = favorites,
                            onDelete = { viewModel.deleteFavorite(it.id) },
                            onNavigateHome = { selectedTab = HomeTab.HOME }
                        )
                    }

                    HomeTab.SENSORS -> {
                        SensorDashboardScreen(
                            onNavigateHome = { selectedTab = HomeTab.HOME }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}

// ---------- Top header card ----------

@Composable
private fun HeaderCard(
    onLogout: () -> Unit
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Smart Campus Assistant",
                    color = SmartCampusColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Welcome User",
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 13.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onLogout)
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = "Logout",
                    tint = SmartCampusColors.DangerRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Logout",
                    color = SmartCampusColors.DangerRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------- Explore card ----------

@Composable
private fun ExploreCard(
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SmartCampusColors.Card
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Explore Campus",
                    color = SmartCampusColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "View all campus locations on the interactive map",
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = "Map",
                tint = SmartCampusColors.AccentCyan,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// ---------- Mini stats cards ----------

@Composable
private fun MiniStatCard(
    title: String,
    value: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val cardBackground = if (title == "Visited") {
        Color(0xFF15803D).copy(alpha = 0.45f) // soft green
    } else {
        SmartCampusColors.Card                      // deep blue
    }

    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Icon(
                imageVector = if (title == "Saved Spots")
                    Icons.Filled.LocationOn
                else
                    Icons.AutoMirrored.Filled.ShowChart,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }
        }
    }
}

// ---------- Bottom navigation ----------

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
                icon = { Icon(Icons.Filled.LocationOn, contentDescription = "Maps") },
                label = { Text("Maps") },
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
                icon = { Icon(Icons.Filled.Star, contentDescription = "Favorites") },
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
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = "Sensors"
                    )
                },
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
