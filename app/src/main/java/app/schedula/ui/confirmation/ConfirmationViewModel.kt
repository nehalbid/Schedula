package app.schedula.ui.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.schedula.data.model.Appointment
import app.schedula.data.model.Doctor
import app.schedula.data.remote.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfirmationViewModel : ViewModel() {

    private val firebaseService = FirebaseService()

    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment.asStateFlow()

    private val _doctor = MutableStateFlow<Doctor?>(null)
    val doctor: StateFlow<Doctor?> = _doctor.asStateFlow()

    fun loadAppointmentDetails(appointmentId: String) {
        viewModelScope.launch {
            firebaseService.firestore.collection("appointments").document(appointmentId).get()
                .addOnSuccessListener { document ->
                    val appt = document.toObject(Appointment::class.java)?.copy(id = document.id)
                    _appointment.value = appt
                    appt?.doctorId?.let { loadDoctorDetails(it) }
                }
        }
    }

    private fun loadDoctorDetails(doctorId: String) {
        firebaseService.firestore.collection("doctors").document(doctorId).get()
            .addOnSuccessListener { document ->
                _doctor.value = document.toObject(Doctor::class.java)?.copy(id = document.id)
            }
    }
}
