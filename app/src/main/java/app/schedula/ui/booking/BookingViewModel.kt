package app.schedula.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.schedula.data.model.Appointment
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
import java.util.Calendar
import java.util.Locale

class BookingViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private val doctorRepository = DoctorRepository()
    private var slotsListener: ListenerRegistration? = null
    private var appointmentsListener: ListenerRegistration? = null

    private val _doctor = MutableStateFlow<Doctor?>(null)
    val doctor: StateFlow<Doctor?> = _doctor.asStateFlow()

    private val _slots = MutableStateFlow<List<Slot>>(emptyList())
    val slots: StateFlow<List<Slot>> = _slots.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _selectedSlot = MutableStateFlow<Slot?>(null)
    val selectedSlot: StateFlow<Slot?> = _selectedSlot.asStateFlow()

    private val _selectedConsultingType = MutableStateFlow("Select Type")
    val selectedConsultingType: StateFlow<String> = _selectedConsultingType.asStateFlow()

    private val _bookingState = MutableStateFlow<Pair<String, String?>>(Pair("idle", null))
    val bookingState: StateFlow<Pair<String, String?>> = _bookingState.asStateFlow()

    fun loadDoctorAndSlots(doctorId: String) {
        loadDoctor(doctorId)
        loadSlots(doctorId)
    }

    private fun loadDoctor(doctorId: String) {
        viewModelScope.launch {
            firebaseService.firestore.collection("doctors").document(doctorId).get()
                .addOnSuccessListener { document ->
                    _doctor.value = document.toObject(Doctor::class.java)?.copy(id = document.id)
                }
        }
    }

    private fun loadSlots(doctorId: String) {
        slotsListener?.remove()

        val dayOfWeek = _selectedDate.value.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
        if (dayOfWeek != null) {
            slotsListener = firebaseService.getSlots(doctorId)
                .whereEqualTo("day", dayOfWeek)
                .addSnapshotListener { snapshot, _ ->
                    val slots = snapshot?.documents?.mapNotNull {
                        it.toObject(Slot::class.java)?.copy(id = it.id)
                    } ?: emptyList()

                    val now = Calendar.getInstance()
                    val isToday = _selectedDate.value.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            _selectedDate.value.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

                    if (isToday) {
                        val timeFormat24h = SimpleDateFormat("HH:mm", Locale.US)
                        val currentTime24h = timeFormat24h.format(now.time)

                        val timeFormat12h = SimpleDateFormat("hh:mm a", Locale.US)

                        _slots.value = slots.filter { slot ->
                            try {
                                val slotDate = timeFormat12h.parse(slot.time)
                                slotDate?.let {
                                    val slotTime24h = timeFormat24h.format(it)
                                    slotTime24h > currentTime24h
                                } ?: false
                            } catch (e: Exception) {
                                false
                            }
                        }
                    } else {
                        _slots.value = slots
                    }
                }
        }
    }

    fun loadAppointments(userId: String) {
        appointmentsListener?.remove()
        appointmentsListener = firebaseService.getAppointments(userId)
            .addSnapshotListener { snapshot, _ ->
                _appointments.value = snapshot?.documents?.mapNotNull {
                    it.toObject(Appointment::class.java)
                } ?: emptyList()
            }
    }

    fun cancelAppointment(appointmentId: String) {
        firebaseService.cancelAppointment(appointmentId)
    }

    fun selectDate(calendar: Calendar) {
        _selectedDate.value = calendar
        _selectedSlot.value = null
        _doctor.value?.id?.let { loadSlots(it) }
    }

    fun selectSlot(slot: Slot) {
        _selectedSlot.value = slot
    }

    fun selectConsultingType(type: String) {
        _selectedConsultingType.value = type
    }

    fun bookSlot() {
        val user = firebaseService.auth.currentUser
        if (user == null || user.phoneNumber == null) {
            _bookingState.value = Pair("auth_error", null)
            return
        }

        val slot = _selectedSlot.value
        val doctorValue = _doctor.value
        val consultingType = _selectedConsultingType.value

        if (consultingType == "Select Type") {
            _bookingState.value = Pair("Please select consulting type", null)
            return
        }

        if (slot != null && doctorValue != null) {
            _bookingState.value = Pair("loading", null)
            val appointmentDateCal = _selectedDate.value.clone() as Calendar
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            try {
                val parsedTime = sdf.parse(slot.time)
                parsedTime?.let {
                    val slotCal = Calendar.getInstance()
                    slotCal.time = it
                    appointmentDateCal.set(Calendar.HOUR_OF_DAY, slotCal.get(Calendar.HOUR_OF_DAY))
                    appointmentDateCal.set(Calendar.MINUTE, slotCal.get(Calendar.MINUTE))
                    appointmentDateCal.set(Calendar.SECOND, 0)
                    appointmentDateCal.set(Calendar.MILLISECOND, 0)

                    doctorRepository.bookSlot(
                        doctorId = doctorValue.id,
                        doctorName = doctorValue.name,
                        doctorGender = doctorValue.gender,
                        slot = slot,
                        userId = user.phoneNumber!!, // Using phone number as UID
                        appointmentDate = appointmentDateCal.time,
                        consultingType = consultingType,
                        onSuccess = { appointmentId -> _bookingState.value = Pair("success", appointmentId) },
                        onFailure = { error -> _bookingState.value = Pair(error, null) }
                    )
                }
            } catch (e: Exception) {
                _bookingState.value = Pair("error", "Invalid slot time format")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        slotsListener?.remove()
        appointmentsListener?.remove()
    }
}
