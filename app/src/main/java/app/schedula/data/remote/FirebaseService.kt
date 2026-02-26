package app.schedula.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import java.util.Calendar
import java.util.Date

class FirebaseService {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // --------------------------------------------------
    // 🔹 GET DOCTORS
    // --------------------------------------------------
    fun getDoctors() =
        firestore.collection("doctors")

    // --------------------------------------------------
    // 🔹 GET SLOTS
    // --------------------------------------------------
    fun getSlots(doctorId: String) =
        firestore.collection("doctors")
            .document(doctorId)
            .collection("slots")

    // --------------------------------------------------
    // 🔥 BOOK SLOT (SAFE TRANSACTION + CREATE APPOINTMENT)
    // --------------------------------------------------
    fun bookSlot(
        doctorId: String,
        doctorName: String,
        doctorGender: String,
        slotId: String,
        day: String,
        time: String,
        userId: String, // This will now be the phone number
        appointmentDate: Date,
        consultingType: String,
        onSuccess: (String) -> Unit, // Pass appointment ID on success
        onFailure: (String) -> Unit
    ) {

        val slotRef = firestore
            .collection("doctors")
            .document(doctorId)
            .collection("slots")
            .document(slotId)

        val appointmentRef =
            firestore.collection("appointments").document()

        firestore.runTransaction { transaction: Transaction ->

            val snapshot = transaction.get(slotRef)
            val isBooked = snapshot.getBoolean("isBooked") ?: false

            if (isBooked) {
                throw Exception("Slot already booked")
            }

            // Update slot
            transaction.update(
                slotRef,
                mapOf(
                    "isBooked" to true,
                    "bookedBy" to userId
                )
            )

            // Create appointment
            transaction.set(
                appointmentRef,
                mapOf(
                    "id" to appointmentRef.id,
                    "doctorId" to doctorId,
                    "doctorName" to doctorName,
                    "doctorGender" to doctorGender,
                    "userId" to userId,
                    "day" to day,
                    "time" to time,
                    "status" to "UPCOMING",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "appointmentDate" to appointmentDate,
                    "consultingType" to consultingType
                )
            )
        }
            .addOnSuccessListener { onSuccess(appointmentRef.id) } // Pass the ID
            .addOnFailureListener {
                onFailure(it.message ?: "Booking failed")
            }
    }

    // --------------------------------------------------
    // 🔹 GET APPOINTMENTS
    // --------------------------------------------------
    fun getAppointments(userId: String) =
        firestore.collection("appointments")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

    // --------------------------------------------------
    // 🔹 CANCEL APPOINTMENT
    // --------------------------------------------------
    fun cancelAppointment(appointmentId: String) {
        firestore.collection("appointments")
            .document(appointmentId)
            .update("status", "Cancelled")
    }

    // --------------------------------------------------
    // 🔹 UPDATE PAST APPOINTMENTS
    // --------------------------------------------------
    fun updatePastAppointments() {
        val now = Calendar.getInstance().time
        firestore.collection("appointments")
            .whereEqualTo("status", "UPCOMING")
            .whereLessThan("appointmentDate", now)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("status", "Completed")
                }
            }
    }
}