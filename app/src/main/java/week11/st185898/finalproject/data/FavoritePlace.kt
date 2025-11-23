package week11.st185898.finalproject.data

import com.google.firebase.firestore.DocumentId

data class FavoritePlace(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = 0L
)