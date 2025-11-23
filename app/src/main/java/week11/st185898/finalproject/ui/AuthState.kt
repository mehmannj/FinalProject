package week11.st185898.finalproject.ui

import com.google.firebase.auth.FirebaseUser

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object SignedOut : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}