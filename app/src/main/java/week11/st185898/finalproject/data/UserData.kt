package week11.st185898.finalproject.data

import com.google.firebase.firestore.DocumentId

data class UserData(
 
   @DocumentId
    val id: String = "",
    val stepCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val date: String = ""
)

