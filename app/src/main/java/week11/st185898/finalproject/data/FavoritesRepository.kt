package week11.st185898.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class FavoritesRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    private fun uid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")

    private fun path() = "users/${uid()}/favorites"

    fun observeFavorites(
        onSuccess: (List<FavoritePlace>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return db.collection(path())
            .orderBy("createdAt")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    onError(e); return@addSnapshotListener
                }
                val list = snap?.toObjects(FavoritePlace::class.java).orEmpty()
                onSuccess(list)
            }
    }

    suspend fun addFavorite(place: FavoritePlace): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            db.collection(path())
                .add(place.copy(createdAt = System.currentTimeMillis()))
                .addOnSuccessListener { cont.resume(Result.success(Unit)) }
                .addOnFailureListener { cont.resume(Result.failure(it)) }
        }

    suspend fun deleteFavorite(id: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            db.collection(path())
                .document(id)
                .delete()
                .addOnSuccessListener { cont.resume(Result.success(Unit)) }
                .addOnFailureListener { cont.resume(Result.failure(it)) }
        }
}
