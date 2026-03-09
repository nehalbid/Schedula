package app.schedula.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class FirebaseService {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        enableOfflinePersistence()
    }

    private fun enableOfflinePersistence() {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

            firestore.firestoreSettings = settings
        } catch (_: Exception) {
            // Firestore settings already initialized
        }
    }

    // DOCTORS

    fun getDoctors(): CollectionReference =
        firestore.collection("doctors")

    fun getActiveDoctors(): Query =
        firestore.collection("doctors")
            .whereEqualTo("status", "ACTIVE")
            .whereEqualTo("profileCompleted", true)

    fun getDoctorById(doctorId: String): DocumentReference =
        firestore.collection("doctors")
            .document(doctorId)

    // -----------------------------
    // SLOTS
    // -----------------------------

    fun getSlots(doctorId: String): CollectionReference =
        firestore.collection("doctors")
            .document(doctorId)
            .collection("slots")

    fun createSlot(doctorId: String): DocumentReference =
        firestore.collection("doctors")
            .document(doctorId)
            .collection("slots")
            .document()

    // -----------------------------
    // APPOINTMENTS
    // -----------------------------

    fun getAppointments(): CollectionReference =
        firestore.collection("appointments")

    fun getAppointmentById(appointmentId: String): DocumentReference =
        firestore.collection("appointments")
            .document(appointmentId)

    fun createAppointment(): DocumentReference =
        firestore.collection("appointments")
            .document()
}