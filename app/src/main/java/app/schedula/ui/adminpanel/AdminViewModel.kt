package app.schedula.ui.adminpanel

import androidx.lifecycle.ViewModel
import app.schedula.data.model.Doctor
import app.schedula.data.repository.AdminAppointment
import app.schedula.data.repository.AdminRepository
import app.schedula.data.repository.AdminStats
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AdminViewModel : ViewModel() {

    private val repo = AdminRepository()
    private val listeners = mutableListOf<ListenerRegistration>()

    private val _stats = MutableStateFlow(AdminStats())
    val stats: StateFlow<AdminStats> = _stats

    private val _doctors = MutableStateFlow<List<Doctor>>(emptyList())
    val doctors: StateFlow<List<Doctor>> = _doctors

    private val _appointments = MutableStateFlow<List<AdminAppointment>>(emptyList())
    val appointments: StateFlow<List<AdminAppointment>> = _appointments

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        listeners.add(repo.listenDoctorCount { count -> _stats.update { it.copy(doctors = count) } })
        listeners.add(repo.listenPatientCount { count -> _stats.update { it.copy(patients = count) } })
        listeners.add(repo.listenAppointmentCount { count -> _stats.update { it.copy(appointments = count) } })
        listeners.add(repo.listenTopSpecialties { top -> _stats.update { it.copy(topSpecialties = top) } })
        listeners.add(repo.listenDoctors { _doctors.value = it } )
        listeners.add(repo.listenAppointments { _appointments.value = it })
    }

    private fun formatPhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }.takeLast(10)
        return if (digits.length == 10) "+91$digits" else ""
    }

    // ---------------- CREATE DOCTOR ----------------

    fun createDoctor(
        name: String,
        specialty: String,
        experience: Int,
        fee: Int,
        phone: String,
        gender: String,
        onComplete: (Boolean) -> Unit
    ) {
        val fullPhone = formatPhone(phone)
        if (fullPhone.isEmpty()) {
            _error.value = "Phone number must be 10 digits"
            onComplete(false)
            return
        }

        val doctor = Doctor(
            id = "",
            name = name,
            specialty = specialty,
            phone = fullPhone,
            experience = experience,
            fee = fee,
            gender = gender,
            status = "ACTIVE",
            profileCompleted = false,
            createdByAdmin = "ADMIN",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        repo.createDoctor(doctor) { success, msg ->
            if (!success) _error.value = msg
            onComplete(success)
        }
    }

    // ---------------- DELETE DOCTOR ----------------

    fun deleteDoctor(doctorId: String) {
        repo.deleteDoctor(doctorId)
    }

    // ---------------- UPDATE DOCTOR ----------------

    fun updateDoctor(
        doctorId: String,
        name: String,
        specialty: String,
        experience: Int,
        fee: Int,
        phone: String,
        status: String,
        gender: String,
        onComplete: (Boolean) -> Unit
    ) {
        val fullPhone = formatPhone(phone)
        if (fullPhone.isEmpty()) {
            _error.value = "Phone number must be 10 digits"
            onComplete(false)
            return
        }

        val updatedDoctor = Doctor(
            id = doctorId,
            name = name,
            specialty = specialty,
            phone = fullPhone,
            experience = experience,
            fee = fee,
            gender = gender,
            status = status,
            updatedAt = System.currentTimeMillis()
        )

        repo.updateDoctor(doctorId, updatedDoctor) { success, msg ->
            if (!success) _error.value = msg
            onComplete(success)
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}
