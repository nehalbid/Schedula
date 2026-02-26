package app.schedula.data.repository

import app.schedula.data.model.Doctor
import app.schedula.data.model.Slot
import app.schedula.data.remote.FirebaseService
import com.google.firebase.firestore.ListenerRegistration
import java.util.Date

class DoctorRepository(
    private val firebaseService: FirebaseService = FirebaseService()
) {

    fun getDoctorsRealtime(
        onResult: (List<Doctor>, String?) -> Unit
    ): ListenerRegistration {

        return firebaseService.getDoctors()
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

    fun getSlotsRealtime(
        doctorId: String,
        onResult: (List<Slot>, String?) -> Unit
    ): ListenerRegistration {

        return firebaseService.getSlots(doctorId)
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

    fun bookSlot(
        doctorId: String,
        doctorName: String,
        doctorGender: String,
        slot: Slot,
        userId: String,
        appointmentDate: Date,
        consultingType: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firebaseService.bookSlot(
            doctorId = doctorId,
            doctorName = doctorName,
            doctorGender = doctorGender,
            slotId = slot.id,
            day = slot.day,
            time = slot.time,
            userId = userId,
            appointmentDate = appointmentDate,
            consultingType = consultingType,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}
