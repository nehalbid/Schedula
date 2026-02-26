package app.schedula.ui.appointment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.schedula.data.model.Appointment
import app.schedula.data.remote.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppointmentDetailViewModel : ViewModel() {

    private val firebaseService = FirebaseService()

    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment

    fun loadAppointment(appointmentId: String) {
        if (appointmentId.isEmpty()) return

        viewModelScope.launch {
            firebaseService.firestore.collection("appointments").document(appointmentId).get()
                .addOnSuccessListener { document ->
                    _appointment.value = document.toObject(Appointment::class.java)?.copy(id = document.id)
                }
        }
    }
}