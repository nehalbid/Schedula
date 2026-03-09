package app.schedula.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Slot(

    val id: String = "",

    val doctorId: String = "",

    val date: String = "",              // yyyy-MM-dd

    val time: String = "",              // 09:30 AM

    @get:PropertyName("isBooked")
    var isBooked: Boolean = false,

    val bookedBy: String? = null,

    val status: String = "AVAILABLE",   // AVAILABLE / BOOKED / BLOCKED

    @ServerTimestamp
    val timestamp: Date? = null
)