package week11.st185898.finalproject.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import week11.st185898.finalproject.data.FavoritePlace

data class MapState(
    val currentLocation: LatLng? = null,
    val selectedLocation: LatLng? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MapViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state
    
    private val _selectedLocationForSave = MutableStateFlow<LatLng?>(null)
    val selectedLocationForSave: StateFlow<LatLng?> = _selectedLocationForSave
    
    fun updateCurrentLocation(location: LatLng) {
        _state.value = _state.value.copy(currentLocation = location)
    }
    
    fun selectLocationForSave(location: LatLng) {
        _selectedLocationForSave.value = location
    }
    
    fun clearSelectedLocation() {
        _selectedLocationForSave.value = null
    }
    
    fun setError(message: String?) {
        _state.value = _state.value.copy(errorMessage = message)
    }
    
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
    
    fun setLoading(loading: Boolean) {
        _state.value = _state.value.copy(isLoading = loading)
    }
    
    data class CampusLocation(
        val name: String,
        val position: LatLng,
        val description: String
    )
    
    // Sheridan College Trafalgar Campus (Oakville, Ontario) - Main Campus
    val SHERIDAN_TRAFALGAR_CENTER = LatLng(43.4567, -79.6800)
    
    fun getCampusLocations(): List<CampusLocation> {
        return listOf(
            CampusLocation(
                "Sheridan Library", 
                LatLng(43.4570, -79.6805), 
                "Study & Research - Trafalgar Campus"
            ),
            CampusLocation(
                "Student Cafeteria", 
                LatLng(43.4565, -79.6795), 
                "Dining Hall - Food Services"
            ),
            CampusLocation(
                "Science Building", 
                LatLng(43.4572, -79.6802), 
                "Chemistry and Biology Labs"
            ),
            CampusLocation(
                "Sports Complex", 
                LatLng(43.4560, -79.6798), 
                "Gym, Pool & Fitness Facilities"
            ),
            CampusLocation(
                "Student Center", 
                LatLng(43.4568, -79.6803), 
                "Student Services & Activities"
            )
        )
    }
    
    fun getCampusCenter(): LatLng = SHERIDAN_TRAFALGAR_CENTER
}

