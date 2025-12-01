package week11.st185898.finalproject.ui.sensors

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st185898.finalproject.data.UserData
import week11.st185898.finalproject.ui.SmartCampusColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDashboardScreen(
    onNavigateHome: () -> Unit,
    sensorViewModel: SensorViewModel = viewModel()
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    val uiState by sensorViewModel.uiState.collectAsState()
    val history by sensorViewModel.history.collectAsState()

    // flags
    var hasStepCounter by remember { mutableStateOf(false) }
    var hasStepDetector by remember { mutableStateOf(false) }
    var hasCompassSensor by remember { mutableStateOf(true) }
    var hasActivityPermission by remember { mutableStateOf(false) }

    // permission launcher
    val activityPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasActivityPermission = granted
        }

    // ask for permission on first launch
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                activityPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                hasActivityPermission = true
            }
        } else {
            hasActivityPermission = true
        }
    }

    // register sensors after permission – update ViewModel instead of local state
    if (hasActivityPermission) {
        DisposableEffect(sensorManager) {
            val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

            hasStepCounter = stepCounter != null
            hasStepDetector = stepDetector != null
            hasCompassSensor = rotationSensor != null

            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_STEP_COUNTER -> {
                            sensorViewModel.onStepCounter(event.values[0])
                        }

                        Sensor.TYPE_STEP_DETECTOR -> {
                            sensorViewModel.onStepDetected()
                        }

                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                            SensorManager.getOrientation(rotationMatrix, orientationAngles)
                            val azimuthRad = orientationAngles[0]
                            val deg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                            sensorViewModel.onAzimuthChanged(deg)
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            stepCounter?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
            stepDetector?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
            rotationSensor?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
            }

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val displayedSteps = uiState.displayedSteps
    val hasAnyStepSource = hasStepCounter || hasStepDetector

    val scrollState = rememberScrollState()
    val stepGoal = 10_000
    val progress = (displayedSteps.toFloat() / stepGoal)
        .coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ===== header bar (Home + title + subtitle) =====
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateHome() }
                ) {
                    Icon(Icons.Default.Home, "Home", tint = SmartCampusColors.DangerRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Home", color = SmartCampusColors.DangerRed)
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Smart Campus Assistant",
                        color = SmartCampusColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Activity & Sensors",
                        color = SmartCampusColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))
            }
        }

        // ===== page title =====
        Text(
            text = "Sensor Dashboard",
            color = SmartCampusColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        // ===== steps card =====
        StepsCard(
            steps = displayedSteps,
            goal = stepGoal,
            hasPermission = hasActivityPermission,
            hasSensor = hasAnyStepSource,
            progress = progress
        )

        // ===== compass card =====
        CompassCard(
            azimuthDegrees = uiState.azimuthDegrees,
            hasCompassSensor = hasCompassSensor
        )

        // ===== Activity History card =====
        ActivityHistoryCard(history = history)
    }
}

@Composable
private fun StepsCard(
    steps: Int,
    goal: Int,
    hasPermission: Boolean,
    hasSensor: Boolean,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SmartCampusColors.Card),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Steps Today",
                        color = SmartCampusColors.TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (hasPermission && hasSensor) steps.toString() else "--",
                        color = SmartCampusColors.AccentGreen,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF022C22)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = "Activity",
                        tint = SmartCampusColors.AccentGreen,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(22.dp)
                    )
                }
            }

            Text(
                text = when {
                    !hasPermission ->
                        "Allow Physical activity permission to start counting steps."
                    !hasSensor ->
                        "No hardware step sensor found (emulator will stay at 0)."
                    else ->
                        "Keep moving around campus 🏃‍♂️"
                },
                color = SmartCampusColors.TextSecondary,
                fontSize = 12.sp
            )

            LinearProgressIndicator(
                progress = {
                    if (hasPermission && hasSensor) progress else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = SmartCampusColors.AccentGreen,
                trackColor = SmartCampusColors.CardSoft
            )

            val percent = (progress * 100).roundToInt()
            Text(
                text = if (hasPermission && hasSensor)
                    "$percent% of daily goal (${goal} steps)"
                else
                    "Goal progress will appear once step data is available.",
                color = SmartCampusColors.TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun CompassCard(
    azimuthDegrees: Float,
    hasCompassSensor: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SmartCampusColors.Card),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Compass",
                    color = SmartCampusColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "Compass",
                    tint = SmartCampusColors.AccentCyan
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (hasCompassSensor) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(SmartCampusColors.CardSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "S",
                        color = SmartCampusColors.TextSecondary,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "W",
                        color = SmartCampusColors.TextSecondary,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "E",
                        color = SmartCampusColors.TextSecondary,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp),
                        fontSize = 12.sp
                    )

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF020617)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompassCalibration,
                            contentDescription = "Direction",
                            tint = SmartCampusColors.AccentCyan,
                            modifier = Modifier
                                .size(46.dp)
                                .graphicsLayer {
                                    rotationZ = -azimuthDegrees
                                }
                        )
                    }
                }

                Text(
                    text = "${azimuthDegrees.roundToInt()}°",
                    color = SmartCampusColors.AccentCyan,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Rotate your phone to update direction.",
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Compass sensor not available on this device.",
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ActivityHistoryCard(
    history: List<UserData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SmartCampusColors.CardSoft),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Activity History",
                color = SmartCampusColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (history.isEmpty()) {
                Text(
                    text = "No step history yet. Start walking and your daily activity will appear here.",
                    color = SmartCampusColors.TextSecondary,
                    fontSize = 12.sp
                )
            } else {
                history.forEach { day ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = day.date,
                            color = SmartCampusColors.TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${day.stepCount} steps",
                            color = SmartCampusColors.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
