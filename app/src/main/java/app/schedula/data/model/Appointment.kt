package app.schedula.data.model

import app.schedula.ui.booking.PatientDetails
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Appointment(
    val id: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorGender: String = "",
    val doctorSpecialty: String = "",
    val userId: String = "",
    val day: String = "",
    val time: String = "",
    val slotId: String = "",
    val date: String = "",

    // 🔥 Updated Status (default UPCOMING)
    val status: String = AppointmentStatus.UPCOMING.name,

    val consultingType: String = "",
    
    // 🔥 TELEMEDICINE FIELD
    val meetingLink: String? = null,

    @ServerTimestamp
    val createdAt: Date? = null,

    // 🔥 Main appointment time (used for logic)
    val appointmentDate: Date? = null,

    // 🔥 NEW FIELDS FOR WORKFLOW
    val checkedInAt: Date? = null,
    val startedAt: Date? = null,
    val completedAt: Date? = null,

    val patientDetails: PatientDetails? = null,
    val paymentDetails: PaymentDetails? = null
)

data class PaymentDetails(
    val consultationFee: Int = 0,
    val serviceCharge: Int = 0,
    val totalAmount: Int = 0,
    val status: String = ""
)


// 🔥 STATUS ENUM (ADD THIS)
enum class AppointmentStatus {
    UPCOMING,
    CHECKED_IN,
    ONGOING,
    COMPLETED,
    NO_SHOW,
    CANCELLED,
    RESCHEDULE_REQUESTED // New status for doctor-initiated reschedule
}
