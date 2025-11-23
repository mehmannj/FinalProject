package week11.st185898.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AuthRepository(
    private val auth: FirebaseAuth
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    result.user?.let { cont.resume(Result.success(it)) }
                        ?: cont.resume(Result.failure(IllegalStateException("User null")))
                }
                .addOnFailureListener { cont.resume(Result.failure(it)) }
        }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    result.user?.let { cont.resume(Result.success(it)) }
                        ?: cont.resume(Result.failure(IllegalStateException("User null")))
                }
                .addOnFailureListener { cont.resume(Result.failure(it)) }
        }

    fun signOut() = auth.signOut()
}