package app.schedula.ui.appointment

import android.util.Log
import androidx.lifecycle.ViewModel
import app.schedula.data.model.Appointment
import app.schedula.data.remote.FirebaseService
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

class AppointmentViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private var appointmentsListener: ListenerRegistration? = null

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    init {
        loadAppointments()
    }

    private fun loadAppointments() {
        val userId = firebaseService.auth.currentUser?.phoneNumber // Using phone number as ID
        if (userId == null) return

        // Fetching all upcoming appointments
        appointmentsListener = firebaseService.firestore
            .collection("appointments")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "UPCOMING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AppointmentViewModel", "Error loading appointments", error)
                    return@addSnapshotListener
                }

                val appointmentList = snapshot?.documents?.mapNotNull {
                    try {
                        it.toObject(Appointment::class.java)?.copy(id = it.id)
                    } catch (e: Exception) {
                        Log.e("AppointmentViewModel", "Failed to deserialize appointment ${it.id}", e)
                        null // Skip records that fail to deserialize
                    }
                } ?: emptyList()

                val now = Date()
                val upcoming = mutableListOf<Appointment>()
                val past = mutableListOf<Appointment>()

                for (appointment in appointmentList) {
                    if (appointment.appointmentDate != null && appointment.appointmentDate.after(now)) {
                        upcoming.add(appointment)
                    } else {
                        past.add(appointment)
                    }
                }

                _appointments.value = upcoming

                if (past.isNotEmpty()) {
                    updateStatusesToCompleted(past)
                }
            }
    }

    private fun updateStatusesToCompleted(appointments: List<Appointment>) {
        for (appointment in appointments) {
            firebaseService.firestore.collection("appointments").document(appointment.id)
                .update("status", "Completed")
                .addOnFailureListener { e ->
                    Log.e("AppointmentViewModel", "Failed to update status for ${appointment.id}", e)
                }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        firebaseService.cancelAppointment(appointmentId)
    }

    override fun onCleared() {
        super.onCleared()
        appointmentsListener?.remove()
    }
}
