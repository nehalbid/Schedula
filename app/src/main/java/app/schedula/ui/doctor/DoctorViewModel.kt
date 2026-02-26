package app.schedula.ui.doctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.schedula.data.model.Doctor
import app.schedula.data.model.Slot
import app.schedula.data.remote.FirebaseService
import app.schedula.data.repository.DoctorRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class DoctorViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private val doctorRepository = DoctorRepository()

    private var slotsListener: ListenerRegistration? = null

    private val _doctor = MutableStateFlow<Doctor?>(null)
    val doctor: StateFlow<Doctor?> = _doctor.asStateFlow()

    private val _slots = MutableStateFlow<List<Slot>>(emptyList())
    val slots: StateFlow<List<Slot>> = _slots.asStateFlow()

    private val _selectedDay = MutableStateFlow("Monday")
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    private val _selectedSlot = MutableStateFlow<Slot?>(null)
    val selectedSlot: StateFlow<Slot?> = _selectedSlot.asStateFlow()

    private val _bookingState = MutableStateFlow<String?>(null)
    val bookingState: StateFlow<String?> = _bookingState.asStateFlow()

    fun loadDoctor(doctorId: String) {
        viewModelScope.launch {
            firebaseService.firestore.collection("doctors").document(doctorId).get()
                .addOnSuccessListener { document ->
                    _doctor.value = document.toObject(Doctor::class.java)?.copy(id = document.id)
                }
        }
    }

    fun loadSlots(doctorId: String) {
        slotsListener?.remove()
        slotsListener = firebaseService.getSlots(doctorId)
            .whereEqualTo("day", _selectedDay.value)
            .addSnapshotListener { snapshot, _ ->
                _slots.value = snapshot?.toObjects(Slot::class.java) ?: emptyList()
            }
    }

    fun selectDay(day: String) {
        _selectedDay.value = day
        _selectedSlot.value = null
        _doctor.value?.id?.let { loadSlots(it) }
    }

    fun selectSlot(slot: Slot) {
        _selectedSlot.value = slot
    }

    fun bookSlot(doctorId: String, appointmentDate: Date) {
        val slot = _selectedSlot.value
        val user = firebaseService.auth.currentUser
        val doctor = _doctor.value

        if (slot != null && user != null && doctor != null) {
            _bookingState.value = "loading"
            doctorRepository.bookSlot(
                doctorId = doctorId,
                doctorName = doctor.name,
                doctorGender = doctor.gender,
                slot = slot,
                userId = user.uid,
                appointmentDate = appointmentDate,
                consultingType = "Video",
                onSuccess = { _bookingState.value = "success" },
                onFailure = { _bookingState.value = it }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        slotsListener?.remove()
    }
}
