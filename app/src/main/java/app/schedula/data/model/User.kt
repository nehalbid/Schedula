package app.schedula.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(

// Firebase Auth UID
    val uid: String = "",

// Basic identity
    val phone: String = "",

// Role system
    val role: String = "PATIENT",   // PATIENT / DOCTOR / ADMIN

// Optional profile fields (Nullable)
    val name: String? = null,
    val email: String? = null,
    val location: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val bloodGroup: String? = null,
    val avatarIcon: String? = null,

// Creation timestamp
    val createdAt: Long = System.currentTimeMillis() ,
    val updatedAt: Long = System.currentTimeMillis()

) {
    /**
     * Converts to a map and filters out null values so they don't appear in Firestore.
     */
    fun toSafeMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "phone" to phone,
            "role" to role,
            "name" to name,
            "email" to email,
            "location" to location,
            "dob" to dob,
            "gender" to gender,
            "bloodGroup" to bloodGroup,
            "avatarIcon" to avatarIcon,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        ).filterValues { it != null }
    }
}
