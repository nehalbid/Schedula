package app.schedula.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Prescription(
    val id: String = "",
    val appointmentId: String = "",
    val doctorId: String = "",
    val patientId: String = "",
    val doctorName: String = "",
    val diagnosis: String = "",
    val medicines: List<Medicine> = emptyList(),
    val instructions: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)

data class Medicine(
    val name: String = "",
    val dosage: String = "", // e.g., "500mg"
    val frequency: String = "", // e.g., "1-0-1" (Morning-Afternoon-Night)
    val duration: String = "" // e.g., "5 days"
)
