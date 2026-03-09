package app.schedula.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Doctor(

// Firestore document ID
    val id: String = "",

// Basic profile
    val name: String = "",
    val specialty: String = "",
    val phone: String = "",
    val gender: String = "",

// Professional info
    val experience: Int = 0,
    val fee: Int = 0,
    val rating: Double = 0.0,

// Doctor status
    val status: String = "ACTIVE",      // ACTIVE / DISABLED / ON_LEAVE
    val profileCompleted: Boolean = false,
    val loginEnabled: Boolean = true,

// Admin metadata
    val createdByAdmin: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,

// Slot configuration
    val slotDuration: Int = 30,

// Weekly schedule - Session 1
    val mondayStart: String = "",
    val mondayEnd: String = "",
    val tuesdayStart: String = "",
    val tuesdayEnd: String = "",
    val wednesdayStart: String = "",
    val wednesdayEnd: String = "",
    val thursdayStart: String = "",
    val thursdayEnd: String = "",
    val fridayStart: String = "",
    val fridayEnd: String = "",
    val saturdayStart: String = "",
    val saturdayEnd: String = "",
    val sundayStart: String = "",
    val sundayEnd: String = "",

// Weekly schedule - Session 2 (Optional for split shifts)
    val mondayStart2: String = "",
    val mondayEnd2: String = "",
    val tuesdayStart2: String = "",
    val tuesdayEnd2: String = "",
    val wednesdayStart2: String = "",
    val wednesdayEnd2: String = "",
    val thursdayStart2: String = "",
    val thursdayEnd2: String = "",
    val fridayStart2: String = "",
    val fridayEnd2: String = "",
    val saturdayStart2: String = "",
    val saturdayEnd2: String = "",
    val sundayStart2: String = "",
    val sundayEnd2: String = "",

// Off days (List of dates in yyyy-MM-dd format)
    val offDates: List<String> = emptyList()

) {
    fun toSafeMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "specialty" to specialty,
            "phone" to phone,
            "gender" to gender,
            "experience" to experience,
            "fee" to fee,
            "rating" to rating,
            "status" to status,
            "profileCompleted" to profileCompleted,
            "loginEnabled" to loginEnabled,
            "createdByAdmin" to createdByAdmin,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "slotDuration" to slotDuration,
            "mondayStart" to mondayStart,
            "mondayEnd" to mondayEnd,
            "tuesdayStart" to tuesdayStart,
            "tuesdayEnd" to tuesdayEnd,
            "wednesdayStart" to wednesdayStart,
            "wednesdayEnd" to wednesdayEnd,
            "thursdayStart" to thursdayStart,
            "thursdayEnd" to thursdayEnd,
            "fridayStart" to fridayStart,
            "fridayEnd" to fridayEnd,
            "saturdayStart" to saturdayStart,
            "saturdayEnd" to saturdayEnd,
            "sundayStart" to sundayStart,
            "sundayEnd" to sundayEnd,
            "mondayStart2" to mondayStart2,
            "mondayEnd2" to mondayEnd2,
            "tuesdayStart2" to tuesdayStart2,
            "tuesdayEnd2" to tuesdayEnd2,
            "wednesdayStart2" to wednesdayStart2,
            "wednesdayEnd2" to wednesdayEnd2,
            "thursdayStart2" to thursdayStart2,
            "thursdayEnd2" to thursdayEnd2,
            "fridayStart2" to fridayStart2,
            "fridayEnd2" to fridayEnd2,
            "saturdayStart2" to saturdayStart2,
            "saturdayEnd2" to saturdayEnd2,
            "sundayStart2" to sundayStart2,
            "sundayEnd2" to sundayEnd2,
            "offDates" to offDates
        ).filterValues { it != null }
    }
}
