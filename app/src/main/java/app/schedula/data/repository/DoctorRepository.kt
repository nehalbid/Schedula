package app.schedula.data.repository

import app.schedula.data.model.Appointment
import app.schedula.data.model.AppointmentStatus
import app.schedula.data.model.Doctor
import app.schedula.data.model.Slot
import app.schedula.data.remote.FirebaseService
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class DoctorRepository(
    private val firebaseService: FirebaseService = FirebaseService()
) {

    private val firestore = firebaseService.firestore

    // --------------------------------
    // GET DOCTORS (REALTIME)
    // Only ACTIVE + profileCompleted
    // --------------------------------
    fun getDoctorsRealtime(
        onResult: (List<Doctor>, String?) -> Unit
    ): ListenerRegistration {

        return firestore.collection("doctors")
            .whereEqualTo("status", "ACTIVE")
            .whereEqualTo("profileCompleted", true)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onResult(emptyList(), error.message)
                    return@addSnapshotListener
                }

                val doctors = snapshot?.documents?.mapNotNull { doc ->
                    val doctor = doc.toObject(Doctor::class.java)
                    doctor?.copy(id = doc.id)
                } ?: emptyList()

                onResult(doctors, null)
            }
    }

    // --------------------------------
    // GET SLOTS REALTIME
    // --------------------------------
    fun getSlotsRealtime(
        doctorId: String,
        onResult: (List<Slot>, String?) -> Unit
    ): ListenerRegistration {

        return firestore.collection("doctors")
            .document(doctorId)
            .collection("slots")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onResult(emptyList(), error.message)
                    return@addSnapshotListener
                }

                val slots = snapshot?.documents?.mapNotNull { doc ->
                    val slot = doc.toObject(Slot::class.java)
                    slot?.copy(id = doc.id)
                } ?: emptyList()

                onResult(slots, null)
            }
    }

    // --------------------------------
    // BOOK APPOINTMENT
    // --------------------------------
    suspend fun bookAppointment(
        appointment: Appointment,
        slot: Slot
    ): Result<String> {

        return try {

            val slotRef = firestore.collection("doctors")
                .document(appointment.doctorId)
                .collection("slots")
                .document(slot.id)

            val appointmentRef = firestore.collection("appointments")
                .document(appointment.id)

            firestore.runTransaction { transaction ->

                val slotDoc = transaction.get(slotRef)

                // Check if already booked using the correct field name
                val alreadyBooked = slotDoc.getBoolean("isBooked") ?: false
                if (alreadyBooked) {
                    throw Exception("This slot is already booked. Please select another one.")
                }

                // Update slot to booked
                transaction.update(slotRef, mapOf(
                    "isBooked" to true,
                    "bookedBy" to appointment.userId,
                    "status" to "BOOKED"
                ))

                // Create the appointment document
                transaction.set(appointmentRef, appointment)

            }.await()

            Result.success(appointment.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------
    // RESCHEDULE APPOINTMENT
    // --------------------------------
    suspend fun rescheduleAppointment(
        appointmentId: String,
        doctorId: String,
        oldSlotId: String,
        newSlot: Slot,
        newDate: Date
    ): Result<Unit> {

        return try {

            val appointmentRef =
                firestore.collection("appointments").document(appointmentId)

            val oldSlotRef =
                firestore.collection("doctors")
                    .document(doctorId)
                    .collection("slots")
                    .document(oldSlotId)

            val newSlotRef =
                firestore.collection("doctors")
                    .document(doctorId)
                    .collection("slots")
                    .document(newSlot.id)

            val dateString =
                SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    .format(newDate)

            firestore.runTransaction { transaction ->

                val newSlotSnapshot = transaction.get(newSlotRef)

                if (newSlotSnapshot.getBoolean("isBooked") == true) {
                    throw Exception("Selected slot already booked")
                }

                // Release old slot
                transaction.update(oldSlotRef, mapOf(
                    "isBooked" to false,
                    "bookedBy" to null,
                    "status" to "AVAILABLE"
                ))

                // Book new slot
                transaction.update(newSlotRef, mapOf(
                    "isBooked" to true,
                    "bookedBy" to (transaction.get(appointmentRef).getString("userId")),
                    "status" to "BOOKED"
                ))

                transaction.update(
                    appointmentRef,
                    mapOf(
                        "slotId" to newSlot.id,
                        "time" to newSlot.time,
                        "date" to dateString,
                        "appointmentDate" to newDate,
                        "status" to AppointmentStatus.UPCOMING.name
                    )
                )
            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------
    // CANCEL APPOINTMENT
    // --------------------------------
    fun cancelAppointmentAndReleaseSlot(
        appointmentId: String,
        doctorId: String,
        slotId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {

        val appointmentRef =
            firestore.collection("appointments").document(appointmentId)

        val slotRef =
            firestore.collection("doctors")
                .document(doctorId)
                .collection("slots")
                .document(slotId)

        firestore.runTransaction { transaction ->

            transaction.update(
                appointmentRef,
                "status",
                AppointmentStatus.CANCELLED.name
            )

            transaction.update(slotRef, mapOf(
                "isBooked" to false,
                "bookedBy" to null,
                "status" to "AVAILABLE"
            ))

        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onFailure(it.message ?: "Cancel failed")
            }
    }

    // --------------------------------
    // GENERATE SLOTS
    // --------------------------------
    suspend fun generateSlots(
        doctorId: String,
        date: String,
        startTime: String,
        endTime: String,
        durationMinutes: Int
    ): Result<Unit> {

        return try {

            val startFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val displayFormatter = DateTimeFormatter.ofPattern("hh:mm a")

            var current = LocalTime.parse(startTime, startFormatter)
            val end = LocalTime.parse(endTime, startFormatter)

            val slotsCollection =
                firestore.collection("doctors")
                    .document(doctorId)
                    .collection("slots")

            // DELETE OLD SLOTS FOR THIS DATE
            val existingSlots = slotsCollection
                .whereEqualTo("date", date)
                .get()
                .await()

            for (doc in existingSlots.documents) {
                doc.reference.delete()
            }

            // GENERATE NEW SLOTS
            while (current.isBefore(end)) {

                val formattedTime =
                    current.format(displayFormatter)

                val slot = Slot(
                    doctorId = doctorId,
                    date = date,
                    time = formattedTime,
                    isBooked = false
                )

                slotsCollection.document().set(slot).await()

                current = current.plusMinutes(durationMinutes.toLong())
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
