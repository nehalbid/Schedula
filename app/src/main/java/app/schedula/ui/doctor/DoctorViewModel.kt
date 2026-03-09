package app.schedula.ui.doctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.schedula.data.model.Appointment
import app.schedula.data.model.AppointmentStatus
import app.schedula.data.model.Doctor
import app.schedula.data.model.Slot
import app.schedula.data.remote.FirebaseService
import app.schedula.data.repository.DoctorRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DoctorViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private val doctorRepository = DoctorRepository()

    private val listeners = mutableListOf<ListenerRegistration>()
    private var slotsListener: ListenerRegistration? = null

    private val _doctor = MutableStateFlow<Doctor?>(null)
    val doctor: StateFlow<Doctor?> = _doctor.asStateFlow()

    private val _slots = MutableStateFlow<List<Slot>>(emptyList())
    val slots: StateFlow<List<Slot>> = _slots.asStateFlow()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _selectedSlot = MutableStateFlow<Slot?>(null)
    val selectedSlot: StateFlow<Slot?> = _selectedSlot.asStateFlow()

    private val _bookingState = MutableStateFlow<String?>(null)
    val bookingState: StateFlow<String?> = _bookingState.asStateFlow()

    fun loadDoctor(doctorId: String) {
        viewModelScope.launch {
            firebaseService.getDoctorById(doctorId).get()
                .addOnSuccessListener { document ->
                    _doctor.value = document.toObject(Doctor::class.java)?.copy(id = document.id)
                    loadSlots(doctorId)
                }
        }
    }

    fun loadSlots(doctorId: String) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(_selectedDate.value.time)
        slotsListener?.remove()
        slotsListener = firebaseService.getSlots(doctorId)
            .whereEqualTo("date", dateStr)
            .whereEqualTo("isBooked", false)
            .addSnapshotListener { snapshot, _ ->
                val slotList = snapshot?.toObjects(Slot::class.java) ?: emptyList()
                _slots.value = slotList.sortedBy { it.time }
            }
    }

    fun selectDate(calendar: Calendar) {
        _selectedDate.value = calendar
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
            viewModelScope.launch {
                val appointmentId = "${user.uid.take(5)}_${System.currentTimeMillis()}"
                val appointment = Appointment(
                    id = appointmentId,
                    doctorId = doctorId,
                    doctorName = doctor.name,
                    doctorSpecialty = doctor.specialty,
                    doctorGender = doctor.gender,
                    userId = user.uid,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(appointmentDate),
                    slotId = slot.id,
                    time = slot.time,
                    status = AppointmentStatus.UPCOMING.name,
                    consultingType = "Video",
                    appointmentDate = appointmentDate
                )

                val result = doctorRepository.bookAppointment(appointment, slot)
                if (result.isSuccess) {
                    _bookingState.value = "success"
                } else {
                    _bookingState.value = result.exceptionOrNull()?.message ?: "Booking failed"
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        slotsListener?.remove()
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}
