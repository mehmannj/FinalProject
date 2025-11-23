package week11.st185898.finalproject.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import week11.st185898.finalproject.data.AuthRepository
import week11.st185898.finalproject.data.FavoritePlace
import week11.st185898.finalproject.data.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val authRepo = AuthRepository(FirebaseAuth.getInstance())
    private val favRepo = FavoritesRepository(
        FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance()
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _favorites = MutableStateFlow<List<FavoritePlace>>(emptyList())
    val favorites: StateFlow<List<FavoritePlace>> = _favorites

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private var favListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        val user = authRepo.currentUser
        if (user != null) {
            _authState.value = AuthState.Authenticated(user)
            observeFavorites()
        } else {
            _authState.value = AuthState.SignedOut
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepo.signUp(email, password)
                .onSuccess {
                    _authState.value = AuthState.Authenticated(it)
                    observeFavorites()
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Sign up failed")
                }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepo.signIn(email, password)
                .onSuccess {
                    _authState.value = AuthState.Authenticated(it)
                    observeFavorites()
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Sign in failed")
                }
        }
    }

    fun signOut() {
        favListener?.remove()
        authRepo.signOut()
        _favorites.value = emptyList()
        _authState.value = AuthState.SignedOut
    }



}