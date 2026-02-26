package app.schedula.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Slot(
    val id: String = "",
    val day: String = "",
    val time: String = "",
    @JvmField
    val isBooked: Boolean = false,
    val bookedBy: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)
