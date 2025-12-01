package week11.st185898.finalproject.ui.map

import androidx.lifecycle.ViewModel
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

    fun updateCurrentLocation(location: LatLng?) {
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

    // Data model
    data class CampusLocation(
        val name: String,
        val position: LatLng,
        val description: String
    )

    // -----------------------------
    // CAMPUS CENTERS (for map zoom)
    // -----------------------------
    private val campusCenters = mapOf(
        "davis" to LatLng(43.6569071, -79.7410563),
        "hmc" to LatLng(43.591229, -79.6479929),
        "trafalgar" to LatLng(43.4690663, -79.7000411)
    )

    fun getCampusCenter(campus: String): LatLng {
        return campusCenters[campus.lowercase()] ?: campusCenters["trafalgar"]!!
    }

    // -----------------------------
    // DAVIS CAMPUS LOCATIONS
    // -----------------------------
    private val davisLocations = listOf(
        CampusLocation("A-Wing", LatLng(43.6569071, -79.7410563), "General Academic Building"),
        CampusLocation("B-Wing", LatLng(43.6557895, -79.7392195), "Includes Tim Hortons"),
        CampusLocation("C-Wing", LatLng(43.6569026, -79.7372519), "Campus Safety & Parking Office"),
        CampusLocation("J-Wing", LatLng(43.6573258, -79.7417416), "Includes Tim Hortons"),
        CampusLocation("Gymnasium", LatLng(43.6565913, -79.7375825), "Athletics and Recreation"),
        CampusLocation("Student Centre", LatLng(43.6559789, -79.7387351), "Services, Lounge, Events"),
        CampusLocation("Residence", LatLng(43.6571918, -79.7363940), "On-campus housing"),
        CampusLocation("Marketplace", LatLng(43.6559789, -79.7387351), "Cafeteria (Food Court)"),
        CampusLocation("The Den", LatLng(43.6561305, -79.739679), "Student food and event space"),
        CampusLocation("Davis Library", LatLng(43.6573258, -79.7417416), "Campus Library"),
        CampusLocation("Subway", LatLng(43.6578711, -79.7414495), "Subway Restaurant")
    )

    // -----------------------------
    // HMC CAMPUS LOCATIONS
    // -----------------------------
    private val hmcLocations = listOf(
        CampusLocation("A Building", LatLng(43.591229, -79.6479929), "Main Academic Building (Phase 1)"),
        CampusLocation("B Building", LatLng(43.591229, -79.6479929), "Main Academic Building (Phase 2)"),
        CampusLocation("C Building (Athletic Centre)", LatLng(43.4675737, -79.7029973), "Gym & Athletics"),
        CampusLocation("Cafeteria", LatLng(43.5919198, -79.6480181), "Main food area"),
        CampusLocation("Tim Hortons", LatLng(43.5919198, -79.6480181), "Inside cafeteria"),
        CampusLocation("HMC Library", LatLng(43.5911027, -79.6466423), "Campus Library"),
        CampusLocation("Residence", LatLng(43.6571692, -79.7363508), "On-campus housing"),
        CampusLocation("Subway", LatLng(43.591229, -79.6479929), "Subway Restaurant")
    )

    // -----------------------------
    // TRAFALGAR CAMPUS LOCATIONS
    // -----------------------------
    private val trafalgarLocations = listOf(
        CampusLocation("A-Wing", LatLng(43.4690663, -79.7000411), "Main Academic/Administrative Building"),
        CampusLocation("B-Wing", LatLng(43.4690663, -79.7000411), "Includes Tim Hortons"),
        CampusLocation("C-Wing", LatLng(43.4690663, -79.7000411), "Includes Tim Hortons"),
        CampusLocation("D-Wing", LatLng(43.4690663, -79.7000411), "Academic Building"),
        CampusLocation("Athletic Complex", LatLng(43.4675737, -79.7029973), "Gym/Athletic Facility"),
        CampusLocation("Student Centre", LatLng(43.4690663, -79.7000411), "Student Union Building"),
        CampusLocation("The Marquee", LatLng(43.46928, -79.6996154), "Restaurant/Pub"),
        CampusLocation("Residence", LatLng(43.4684362, -79.6994725), "On-campus housing"),
        CampusLocation("Trafalgar Library", LatLng(43.4683639, -79.6990591), "Campus Library"),
        CampusLocation("Subway", LatLng(43.4863942, -79.7141372), "Subway Restaurant")
    )

    // -----------------------------
    // GET LOCATIONS BASED ON CAMPUS
    // -----------------------------
    fun getLocationsForCampus(campus: String): List<CampusLocation> {
        return when (campus.lowercase()) {
            "davis" -> davisLocations
            "hmc" -> hmcLocations
            else -> trafalgarLocations
        }
    }

    // -----------------------------
    // NEARBY LOCATIONS FOR FAVORITES
    // -----------------------------
    fun getNearbyLocations(favorite: FavoritePlace, radiusKm: Double = 1.0): List<CampusLocation> {
        val all = davisLocations + hmcLocations + trafalgarLocations
        return all.filter {
            calculateDistance(
                favorite.latitude, favorite.longitude,
                it.position.latitude, it.position.longitude
            ) <= radiusKm
        }
    }

    // Distance formula
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) *
                Math.sin(dLon / 2)

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }
}