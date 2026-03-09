package app.schedula.ui.booking

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.schedula.data.model.*
import app.schedula.data.repository.DoctorRepository
import app.schedula.utils.ReminderScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class PatientDetails(
    var fullName: String = "",
    var dob: String = "",
    var contactNumber: String = "",
    var gender: String = "Male",
    var bloodType: String = "A+",
    var weight: String = "",
    var knownAllergies: String = "",
    var currentMedications: String = "",
    var complaint: String = ""
)

class BookingViewModel(
    private val doctorRepository: DoctorRepository = DoctorRepository()
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _doctor = MutableStateFlow<Doctor?>(null)
    val doctor = _doctor.asStateFlow()

    private val _slots = MutableStateFlow<List<Slot>>(emptyList())
    val slots = _slots.asStateFlow()

    private val _isLoadingSlots = MutableStateFlow(false)
    val isLoadingSlots = _isLoadingSlots.asStateFlow()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate = _selectedDate.asStateFlow()

    private val _selectedSlot = MutableStateFlow<Slot?>(null)
    val selectedSlot = _selectedSlot.asStateFlow()

    private val _selectedConsultingType = MutableStateFlow("Select Type")
    val selectedConsultingType = _selectedConsultingType.asStateFlow()

    private val _bookingState = MutableStateFlow<Pair<String, String?>>("idle" to null)
    val bookingState = _bookingState.asStateFlow()

    private val _patientDetails = MutableStateFlow(PatientDetails())
    val patientDetails = _patientDetails.asStateFlow()

    fun resetBookingState() {
        _bookingState.value = "idle" to null
    }

    fun loadDoctorAndSlots(doctorId: String) {
        viewModelScope.launch {
            try {

                val doctorDoc = firestore.collection("doctors")
                    .document(doctorId)
                    .get()
                    .await()

                _doctor.value =
                    doctorDoc.toObject<Doctor>()?.copy(id = doctorDoc.id)

                loadSlotsForDate(doctorId, _selectedDate.value)

            } catch (_: Exception) {}
        }
    }

    private fun loadSlotsForDate(doctorId: String, date: Calendar) {

        viewModelScope.launch {

            _isLoadingSlots.value = true

            val dateString =
                SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date.time)

            try {

                // FETCH ALL SLOTS (including booked ones)
                val slotsSnapshot = firestore.collection("doctors")
                    .document(doctorId)
                    .collection("slots")
                    .whereEqualTo("date", dateString)
                    .get()
                    .await()

                val allSlots = slotsSnapshot.documents.mapNotNull {
                    it.toObject<Slot>()?.copy(id = it.id)
                }

                val isToday = isSameDay(date, Calendar.getInstance())

                val filteredSlots = if (isToday) {

                    val currentTime = Calendar.getInstance()

                    allSlots.filter { slot ->
                        try {
                            val slotTime = parseTime(slot.time)
                            slotTime.after(currentTime)
                        } catch (_: Exception) {
                            true
                        }
                    }

                } else {
                    allSlots
                }

                // simplified sorting
                _slots.value = filteredSlots.sortedBy { it.time }

            } catch (_: Exception) {

                _slots.value = emptyList()

            } finally {

                _isLoadingSlots.value = false

            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun parseTime(timeString: String): Calendar {

        val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val date = sdf.parse(timeString)

        val calendar = Calendar.getInstance()
        val timeCalendar = Calendar.getInstance()

        timeCalendar.time = date ?: Date()

        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar
    }

    fun selectDate(date: Calendar) {
        _selectedDate.value = date
        _selectedSlot.value = null
        _doctor.value?.id?.let { loadSlotsForDate(it, date) }
    }

    fun selectSlot(slot: Slot) {
        _selectedSlot.value = slot
    }

    fun selectConsultingType(type: String) {
        _selectedConsultingType.value = type
    }

    fun onPatientSelected(user: User) {

        _patientDetails.value = PatientDetails(
            fullName = user.name ?: "",
            contactNumber = user.phone ?: "",
            gender = user.gender ?: "Male",
            bloodType = user.bloodGroup ?: "A+"
        )
    }

    fun onPatientSelected(familyMember: FamilyMember) {

        _patientDetails.value = PatientDetails(
            fullName = familyMember.name ?: "",
            gender = familyMember.gender ?: "Male"
        )
    }

    fun onOtherSelected() {
        _patientDetails.value = PatientDetails()
    }

    fun updateDetails(details: PatientDetails) {
        _patientDetails.value = details
    }

    fun bookSlot(context: Context) {

        viewModelScope.launch {

            val currentUser = auth.currentUser
            val doctor = _doctor.value
            val slot = _selectedSlot.value

            if (currentUser == null) {
                _bookingState.value = "auth_error" to null
                return@launch
            }

            if (doctor == null || slot == null) {
                _bookingState.value = "error" to "Doctor or slot not selected"
                return@launch
            }

            _bookingState.value = "loading" to null

            try {

                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val userName = userDoc.getString("name") ?: "User"

                val dateString =
                    SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                        .format(_selectedDate.value.time)

                val appointmentCalendar = _selectedDate.value.clone() as Calendar
                val slotTimeCalendar = parseTime(slot.time)

                appointmentCalendar.set(
                    Calendar.HOUR_OF_DAY,
                    slotTimeCalendar.get(Calendar.HOUR_OF_DAY)
                )

                appointmentCalendar.set(
                    Calendar.MINUTE,
                    slotTimeCalendar.get(Calendar.MINUTE)
                )

                val appointmentDate = appointmentCalendar.time

                val cleanUserName = userName.replace(" ", "").take(8)
                val bookingTime =
                    SimpleDateFormat("HHmm", Locale.ENGLISH).format(Date())
                val bookingDate =
                    SimpleDateFormat("ddMM", Locale.ENGLISH).format(Date())

                val appointmentId =
                    "${cleanUserName}_${bookingDate}_${bookingTime}"

                val appointment = Appointment(
                    id = appointmentId,
                    doctorId = doctor.id,
                    doctorName = doctor.name,
                    doctorSpecialty = doctor.specialty,
                    doctorGender = doctor.gender,
                    userId = currentUser.uid,
                    date = dateString,
                    slotId = slot.id,
                    time = slot.time,
                    status = AppointmentStatus.UPCOMING.name,
                    consultingType = _selectedConsultingType.value,
                    appointmentDate = appointmentDate,
                    patientDetails = _patientDetails.value,
                    paymentDetails = PaymentDetails(
                        consultationFee = doctor.fee,
                        serviceCharge = 50,
                        totalAmount = doctor.fee + 50,
                        status = "PAID"
                    )
                )

                val result =
                    doctorRepository.bookAppointment(appointment, slot)

                if (result.isSuccess) {

                    try {
                        ReminderScheduler.scheduleReminders(
                            context,
                            appointmentId,
                            appointmentDate
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "BookingViewModel",
                            "Failed to schedule reminders: ${e.message}"
                        )
                    }

                    _bookingState.value = "success" to appointmentId

                } else {

                    _bookingState.value =
                        "error" to (result.exceptionOrNull()?.message
                            ?: "Booking failed")

                }

            } catch (e: Exception) {

                _bookingState.value =
                    "error" to (e.message ?: "Booking failed")

            }
        }
    }
}
