package app.schedula.ui.appointment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.schedula.data.model.Appointment
import app.schedula.data.model.Prescription
import app.schedula.data.model.Slot
import app.schedula.data.remote.FirebaseService
import app.schedula.ui.booking.PatientDetails
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AppointmentDetailViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private var appointmentListener: ListenerRegistration? = null

    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment

    private val _prescription = MutableStateFlow<Prescription?>(null)
    val prescription: StateFlow<Prescription?> = _prescription.asStateFlow()

    private val _availableSlots = MutableStateFlow<List<Slot>>(emptyList())
    val availableSlots: StateFlow<List<Slot>> = _availableSlots.asStateFlow()

    private val _isRescheduling = MutableStateFlow(false)
    val isRescheduling: StateFlow<Boolean> = _isRescheduling.asStateFlow()

    private val _isUpdatingPatient = MutableStateFlow(false)
    val isUpdatingPatient: StateFlow<Boolean> = _isUpdatingPatient.asStateFlow()

    fun loadAppointment(appointmentId: String) {
        if (appointmentId.isEmpty()) return
        appointmentListener?.remove()

        appointmentListener = firebaseService.firestore.collection("appointments").document(appointmentId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("AppointmentDetail", "Error loading appointment: ${error.message}")
                    return@addSnapshotListener
                }
                _appointment.value = document?.toObject<Appointment>()?.copy(id = document.id)
                loadPrescription(appointmentId)
            }
    }

    private fun loadPrescription(appointmentId: String) {
        firebaseService.firestore.collection("prescriptions")
            .whereEqualTo("appointmentId", appointmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                _prescription.value = snapshot?.documents?.firstOrNull()?.toObject<Prescription>()
            }
    }

    fun loadSlotsForReschedule(date: Calendar) {
        val appt = _appointment.value ?: return
        val doctorId = appt.doctorId
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date.time)

        viewModelScope.launch {
            try {
                val slotsSnapshot = firebaseService.firestore.collection("doctors").document(doctorId)
                    .collection("slots")
                    .whereEqualTo("date", dateString)
                    .get().await()
                
                val allSlots = slotsSnapshot.documents.mapNotNull { 
                    it.toObject<Slot>()?.copy(id = it.id) 
                }

                val now = Calendar.getInstance()
                val isCurrentApptDate = dateString == appt.date

                val filteredSlots = allSlots.filter { slot ->
                    // A slot is unavailable if it's booked by someone else
                    val isOthersSlot = slot.isBooked && slot.bookedBy != appt.userId
                    // Hide the slot the user currently has if it's the same day
                    val isMyCurrentSlot = slot.id == appt.slotId && isCurrentApptDate
                    
                    if (isOthersSlot || isMyCurrentSlot) return@filter false

                    val isToday = date.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                 date.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                    
                    if (isToday) {
                        try {
                            val slotTime = parseTime(slot.time)
                            val diffMinutes = (slotTime.timeInMillis - now.timeInMillis) / (60 * 1000)
                            // Allow booking if slot is at least 30 mins in the future
                            diffMinutes >= 30
                        } catch (e: Exception) { true }
                    } else true
                }.sortedBy { 
                    try { parseTime(it.time).timeInMillis } catch (e: Exception) { 0L } 
                }

                _availableSlots.value = filteredSlots
            } catch (e: Exception) {
                Log.e("AppointmentDetail", "Error loading slots: ${e.message}")
                _availableSlots.value = emptyList()
            }
        }
    }

    private fun parseTime(timeString: String): Calendar {
        val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val date = sdf.parse(timeString)
        val calendar = Calendar.getInstance()
        val timeCalendar = Calendar.getInstance()
        timeCalendar.time = date ?: java.util.Date()
        
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    fun rescheduleAppointment(newDate: Calendar, newSlot: Slot, onSuccess: (String) -> Unit) {
        val appt = _appointment.value ?: return
        _isRescheduling.value = true

        viewModelScope.launch {
            try {
                val doctorId = appt.doctorId
                val oldSlotId = appt.slotId
                val newSlotId = newSlot.id
                val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(newDate.time)
                val dayName = SimpleDateFormat("EEEE", Locale.ENGLISH).format(newDate.time)

                // 🔥 Calculate the new appointmentDate for check-in logic
                val newAppointmentDate = parseTime(newSlot.time).apply {
                    set(Calendar.YEAR, newDate.get(Calendar.YEAR))
                    set(Calendar.MONTH, newDate.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, newDate.get(Calendar.DAY_OF_MONTH))
                }.time

                firebaseService.firestore.runTransaction { transaction ->
                    // 1. READS FIRST: Get refs
                    val apptRef = firebaseService.firestore.collection("appointments").document(appt.id)
                    val newSlotRef = firebaseService.firestore.collection("doctors").document(doctorId)
                        .collection("slots").document(newSlotId)
                    
                    // Get new slot status
                    val newSlotDoc = transaction.get(newSlotRef)
                    val isBookedBySomeoneElse = newSlotDoc.getBoolean("isBooked") == true && 
                                               newSlotDoc.getString("bookedBy") != appt.userId
                    
                    if (isBookedBySomeoneElse) {
                        throw Exception("Selected slot is already booked")
                    }

                    // 2. WRITES SECOND
                    // Unbook old slot (only if it's different from the new one)
                    if (oldSlotId.isNotEmpty() && oldSlotId != newSlotId) {
                        val oldSlotRef = firebaseService.firestore.collection("doctors").document(doctorId)
                            .collection("slots").document(oldSlotId)
                        transaction.update(oldSlotRef, "isBooked", false)
                        transaction.update(oldSlotRef, "bookedBy", null)
                    }

                    // Book new slot (if it's different or wasn't booked by current user)
                    transaction.update(newSlotRef, "isBooked", true)
                    transaction.update(newSlotRef, "bookedBy", appt.userId)

                    // Update appointment
                    val updatedData = mapOf(
                        "date" to dateString,
                        "day" to dayName,
                        "slotId" to newSlotId,
                        "time" to newSlot.time,
                        "status" to "UPCOMING",
                        "appointmentDate" to newAppointmentDate
                    )
                    transaction.update(apptRef, updatedData)
                    
                    null
                }.await()

                _isRescheduling.value = false
                onSuccess(appt.id)
            } catch (e: Exception) {
                _isRescheduling.value = false
                Log.e("Reschedule", "Error: ${e.message}")
            }
        }
    }

    fun updatePatientDetails(details: PatientDetails, onSuccess: () -> Unit) {
        val appt = _appointment.value ?: return
        _isUpdatingPatient.value = true

        viewModelScope.launch {
            try {
                firebaseService.firestore.collection("appointments").document(appt.id)
                    .update("patientDetails", details)
                    .await()
                _isUpdatingPatient.value = false
                onSuccess()
            } catch (e: Exception) {
                _isUpdatingPatient.value = false
                Log.e("UpdatePatient", "Error: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        appointmentListener?.remove()
    }
}
