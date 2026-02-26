package app.schedula.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Appointment(
    val id: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorGender: String = "", // Add this field
    val userId: String = "",
    val day: String = "",
    val time: String = "",
    val status: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    val appointmentDate: Date? = null
)
