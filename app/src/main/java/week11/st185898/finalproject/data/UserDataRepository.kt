package week11.st185898.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume


class UserDataRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    private fun uid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")

    private fun userDataPath() = "users/${uid()}/userData"

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Save step count for today
     */
    suspend fun saveStepCount(stepCount: Int): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            val today = getTodayDateString()
            val userData = UserData(
                id = today, // Use date as document ID for daily tracking
                stepCount = stepCount,
                lastUpdated = System.currentTimeMillis(),
                date = today
            )

            db.collection(userDataPath())
                .document(today)
                .set(userData)
                .addOnSuccessListener { cont.resume(Result.success(Unit)) }
                .addOnFailureListener { cont.resume(Result.failure(it)) }
        }

    /**
     * Load step count for today
     */
    suspend fun loadTodayStepCount(): Result<Int> =
        suspendCancellableCoroutine { cont ->
            val today = getTodayDateString()
            db.collection(userDataPath())
                .document(today)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userData = document.toObject(UserData::class.java)
                        cont.resume(Result.success(userData?.stepCount ?: 0))
                    } else {
                        cont.resume(Result.success(0))
                    }
                }
                .addOnFailureListener { cont.resume(Result.failure(it)) }
        }

    /**
     * Get all step data (for history/statistics)
     */
    suspend fun getAllStepData(): Result<List<UserData>> =
        suspendCancellableCoroutine { cont ->
            db.collection(userDataPath())
                .orderBy("lastUpdated", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(30) // Last 30 days
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val dataList = querySnapshot.documents.mapNotNull { doc ->
                        doc.toObject(UserData::class.java)
                    }
                    cont.resume(Result.success(dataList))
                }
                .addOnFailureListener { cont.resume(Result.failure(it)) }
        }

}
