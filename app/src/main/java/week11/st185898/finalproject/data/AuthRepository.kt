package week11.st185898.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AuthRepository(
    private val auth: FirebaseAuth
) {

    // Currently signed-in user (or null)
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // SIGN UP: create a new user and return FirebaseUser in Result
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        cont.resume(Result.success(user))
                    } else {
                        cont.resume(
                            Result.failure(
                                IllegalStateException("User is null after sign up")
                            )
                        )
                    }
                }
                .addOnFailureListener { e ->
                    cont.resume(Result.failure(e))
                }
        }

    // SIGN IN: existing user login and return FirebaseUser in Result
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        cont.resume(Result.success(user))
                    } else {
                        cont.resume(
                            Result.failure(
                                IllegalStateException("User is null after sign in")
                            )
                        )
                    }
                }
                .addOnFailureListener { e ->
                    cont.resume(Result.failure(e))
                }
        }

    // SIGN OUT
    fun signOut() {
        auth.signOut()
    }
}
