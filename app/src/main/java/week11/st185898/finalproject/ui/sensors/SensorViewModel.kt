package week11.st185898.finalproject.ui.sensors

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import week11.st185898.finalproject.data.UserDataRepository

class SensorViewModel(app: Application) : AndroidViewModel(app), SensorEventListener {
    
    private val userDataRepo = UserDataRepository(
        FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance()
    )

    private val sensorManager =
        app.getSystemService(Application.SENSOR_SERVICE) as SensorManager

    private val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _state = MutableStateFlow(SensorState())
    val state: StateFlow<SensorState> = _state
    
    // For step counter (cumulative)
    private var stepCountOffset = 0
    private var lastStepCount = 0
    
    // For compass using accelerometer + magnetometer (fallback)
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private var hasAccelerometerData = false
    private var hasMagnetometerData = false
    
    // For Firestore persistence
    private var lastSavedStepCount = 0
    private var autoSaveJob: Job? = null
    private val SAVE_INTERVAL_MS = 30_000L // Save every 30 seconds
    private val SAVE_STEP_THRESHOLD = 10 // Save every 10 steps
    
    init {
        checkAndResetDaily()
        loadStepCountFromFirestore()
        startAutoSave()
    }
    
    /**
     * Check if date changed and reset step count if needed
     */
    private fun checkAndResetDaily() {
        viewModelScope.launch {
            try {
                val today = userDataRepo.getTodayDateString()
                if (lastLoadedDate.isNotEmpty() && lastLoadedDate != today) {
                    // New day - reset step count
                    _state.emit(_state.value.copy(stepCount = 0))
                    lastSavedStepCount = 0
                    stepCountOffset = 0
                    lastLoadedDate = today
                } else if (lastLoadedDate.isEmpty()) {
                    lastLoadedDate = today
                }
            } catch (e: IllegalStateException) {
                // User not logged in
            }
        }
    }
    
    private var lastLoadedDate: String = ""
    
    /**
     * Load step count from Firestore for today
     * Resets if date changed (new day)
     */
    private fun loadStepCountFromFirestore() {
        viewModelScope.launch {
            try {
                val today = userDataRepo.getTodayDateString()
                
                // If date changed, reset step count
                if (lastLoadedDate.isNotEmpty() && lastLoadedDate != today) {
                    _state.emit(_state.value.copy(stepCount = 0))
                    lastSavedStepCount = 0
                    stepCountOffset = 0
                }
                
                lastLoadedDate = today
                
                userDataRepo.loadTodayStepCount()
                    .onSuccess { savedCount ->
                        if (savedCount > 0) {
                            _state.emit(_state.value.copy(stepCount = savedCount))
                            lastSavedStepCount = savedCount
                        }
                    }
                    .onFailure {
                        // Silently fail - user can still use the app
                    }
            } catch (e: IllegalStateException) {
                // User not logged in - that's okay, steps will be saved when they log in
            }
        }
    }
    
    /**
     * Save step count to Firestore
     */
    private fun saveStepCountToFirestore(stepCount: Int) {
        viewModelScope.launch {
            try {
                userDataRepo.saveStepCount(stepCount)
                    .onSuccess {
                        lastSavedStepCount = stepCount
                    }
                    .onFailure {
                        // Silently fail - will retry on next save
                    }
            } catch (e: IllegalStateException) {
                // User not logged in - skip saving
            }
        }
    }
    
    /**
     * Start auto-save job that saves steps periodically
     */
    private fun startAutoSave() {
        autoSaveJob = viewModelScope.launch {
            while (true) {
                delay(SAVE_INTERVAL_MS)
                val currentSteps = _state.value.stepCount
                if (currentSteps != lastSavedStepCount) {
                    saveStepCountToFirestore(currentSteps)
                }
            }
        }
    }

    fun startListening() {
        // Step detection - prefer TYPE_STEP_DETECTOR (fires per step)
        stepDetector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: stepCounter?.let {
            // Fallback to TYPE_STEP_COUNTER if detector not available
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            lastStepCount = 0
        }
        
        // Compass - prefer rotation vector
        rotationVector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: run {
            // Fallback to accelerometer + magnetometer
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            magnetometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
    
    fun resetStepCount() {
        viewModelScope.launch {
            _state.emit(_state.value.copy(stepCount = 0))
            stepCountOffset = 0
            lastStepCount = 0
            lastSavedStepCount = 0
            // Save reset to Firestore
            saveStepCountToFirestore(0)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                // Fires once per step detected
                if (event.values.isNotEmpty() && event.values[0] == 1.0f) {
                    viewModelScope.launch {
                        val newCount = _state.value.stepCount + 1
                        _state.emit(_state.value.copy(stepCount = newCount))
                        
                        // Save to Firestore if threshold reached
                        if (newCount - lastSavedStepCount >= SAVE_STEP_THRESHOLD) {
                            saveStepCountToFirestore(newCount)
                        }
                    }
                }
            }
            
            Sensor.TYPE_STEP_COUNTER -> {
                // Cumulative step count since last reboot
                if (event.values.isNotEmpty()) {
                    val totalSteps = event.values[0].toInt()
                    if (stepCountOffset == 0) {
                        // First reading - set offset
                        stepCountOffset = totalSteps
                        lastStepCount = totalSteps
                    } else {
                        // Calculate steps since app started
                        val stepsSinceStart = totalSteps - stepCountOffset
                        if (stepsSinceStart > _state.value.stepCount) {
                            viewModelScope.launch {
                                _state.emit(_state.value.copy(stepCount = stepsSinceStart))
                                
                                // Save to Firestore if threshold reached
                                if (stepsSinceStart - lastSavedStepCount >= SAVE_STEP_THRESHOLD) {
                                    saveStepCountToFirestore(stepsSinceStart)
                                }
                            }
                        }
                    }
                }
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                // Primary method for compass - most accurate
                val rotationMatrix = FloatArray(9)
                val orientationAngles = FloatArray(3)

                try {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    var azimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    if (azimuthDeg < 0) azimuthDeg += 360f

                    viewModelScope.launch {
                        _state.emit(_state.value.copy(azimuth = azimuthDeg))
                    }
                } catch (e: Exception) {
                    // Handle invalid rotation vector data
                }
            }
            
            Sensor.TYPE_ACCELEROMETER -> {
                // Fallback compass method - part 1
                System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
                hasAccelerometerData = true
                calculateCompassFromAccelMag()
            }
            
            Sensor.TYPE_MAGNETIC_FIELD -> {
                // Fallback compass method - part 2
                System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
                hasMagnetometerData = true
                calculateCompassFromAccelMag()
            }
        }
    }
    
    private fun calculateCompassFromAccelMag() {
        if (hasAccelerometerData && hasMagnetometerData) {
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)
            
            val success = SensorManager.getRotationMatrix(
                rotationMatrix, null, accelerometerReading, magnetometerReading
            )
            
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                var azimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                if (azimuthDeg < 0) azimuthDeg += 360f
                
                viewModelScope.launch {
                    _state.emit(_state.value.copy(azimuth = azimuthDeg))
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    
    override fun onCleared() {
        super.onCleared()
        stopListening()
        autoSaveJob?.cancel()
        // Final save before clearing
        val finalStepCount = _state.value.stepCount
        if (finalStepCount != lastSavedStepCount) {
            viewModelScope.launch {
                saveStepCountToFirestore(finalStepCount)
            }
        }
    }
}
