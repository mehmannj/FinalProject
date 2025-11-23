package week11.st185898.finalproject.ui.sensors

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import week11.st185898.finalproject.ui.SmartCampusColors
import week11.st185898.finalproject.ui.sensors.SensorViewModelFactory

@Composable
fun SensorDashboardScreen(
    vm: SensorViewModel = viewModel(
        factory = SensorViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val state by vm.state.collectAsState()

    // Smooth compass rotation animation
    val animatedAzimuth by animateFloatAsState(
        targetValue = state.azimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "azimuthAnim"
    )

    // Start sensor listeners
    DisposableEffect(Unit) {
        vm.startListening()
        onDispose { vm.stopListening() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // 🔹 STEPS CARD ----------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            colors = CardDefaults.cardColors(
                containerColor = SmartCampusColors.CardSoft
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Steps Today",
                    color = SmartCampusColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = String.format("%,d", state.stepCount),
                        color = SmartCampusColors.AccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = SmartCampusColors.AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Keep moving around campus",
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 12.sp
                )

                // progress bar (10,000 steps)
                val progress = (state.stepCount.coerceAtMost(10000)) / 10000f
                val progressPercent = (progress * 100).toInt()

                Column {
                    Text(
                        text = "$progressPercent% of daily goal (10,000 steps)",
                        color = SmartCampusColors.TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFF0F172A))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(SmartCampusColors.AccentGreen)
                        )
                    }
                }
            }
        }

        // 🔹 COMPASS CARD ----------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            colors = CardDefaults.cardColors(
                containerColor = SmartCampusColors.Card
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Compass",
                    color = SmartCampusColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer circle with compass labels
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(SmartCampusColors.CardSoft)
                    )
                    
                    // Compass labels (N, E, S, W)
                    Text(
                        text = "N",
                        color = SmartCampusColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                    Text(
                        text = "E",
                        color = SmartCampusColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "S",
                        color = SmartCampusColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "W",
                        color = SmartCampusColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                    )

                    // Compass arrow (rotating)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .graphicsLayer {
                                rotationZ = -animatedAzimuth
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = "▲",
                            fontSize = 42.sp,
                            color = SmartCampusColors.AccentCyan,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = "${state.azimuth.toInt()}°",
                    color = SmartCampusColors.TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp
                )

                Text(
                    text = "Rotate your phone to update direction.",
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
