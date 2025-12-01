package week11.st185898.finalproject.ui.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import week11.st185898.finalproject.data.UserData
import week11.st185898.finalproject.data.UserDataRepository
import kotlin.math.max
import kotlin.math.roundToInt

data class SensorUiState(
    val stepsFromCounter: Float = 0f,
    val stepsFromDetector: Float = 0f,
    val initialCounterValue: Float = -1f,
    val azimuthDegrees: Float = 0f
) {
    val displayedSteps: Int
        get() = max(stepsFromCounter, stepsFromDetector).roundToInt()
}

class SensorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SensorUiState())
    val uiState: StateFlow<SensorUiState> = _uiState

    // --- NEW: history + repo ---
    private val _history = MutableStateFlow<List<UserData>>(emptyList())
    val history: StateFlow<List<UserData>> = _history

    private val userDataRepo = UserDataRepository(
        FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance()
    )

    // Track which date we are counting steps for
    private var currentDate: String = userDataRepo.getTodayDateString()

    init {
        // Load today's saved steps + history when ViewModel is created
        viewModelScope.launch {
            // Load today's steps
            userDataRepo.loadTodayStepCount()
                .onSuccess { savedSteps ->
                    _uiState.update { state ->
                        state.copy(
                            // we treat saved steps as coming from detector so UI shows them
                            stepsFromDetector = savedSteps.toFloat()
                        )
                    }
                }
                .onFailure {
                    // ignore; start from 0 if error
                }

            // Load last 30 days history
            refreshHistory()
        }
    }

    private suspend fun refreshHistory() {
        userDataRepo.getAllStepData()
            .onSuccess { list ->
                _history.value = list
            }
            .onFailure {
                // ignore for now
            }
    }

    /** Helper: ensure we are still on same date; if not, reset daily steps. */
    private fun checkDateAndResetIfNeeded() {
        val today = userDataRepo.getTodayDateString()
        if (today != currentDate) {
            currentDate = today
            // reset daily steps in UI
            _uiState.value = SensorUiState()
        }
    }

    /** Save current steps for "today" in Firestore. */
    private fun persistSteps() {
        val steps = _uiState.value.displayedSteps
        viewModelScope.launch {
            userDataRepo.saveStepCount(steps)
            // also refresh history so Activity card updates
            refreshHistory()
        }
    }

    /** Called whenever we get a TYPE_STEP_COUNTER value */
    fun onStepCounter(rawTotal: Float) {
        checkDateAndResetIfNeeded()

        _uiState.update { state ->
            val baseline =
                if (state.initialCounterValue < 0f) rawTotal else state.initialCounterValue
            val steps = (rawTotal - baseline).coerceAtLeast(0f)

            state.copy(
                initialCounterValue = baseline,
                stepsFromCounter = steps
            )
        }

        // Save to Firestore for today
        persistSteps()
    }

    /** Called whenever we get a TYPE_STEP_DETECTOR event */
    fun onStepDetected() {
        checkDateAndResetIfNeeded()

        _uiState.update { state ->
            state.copy(stepsFromDetector = state.stepsFromDetector + 1f)
        }

        // Save to Firestore for today
        persistSteps()
    }

    /** Called with azimuth (degrees) from the compass sensor */
    fun onAzimuthChanged(degrees: Float) {
        val normalized = ((degrees + 360f) % 360f)
        _uiState.update { state ->
            state.copy(azimuthDegrees = normalized)
        }
    }
}
