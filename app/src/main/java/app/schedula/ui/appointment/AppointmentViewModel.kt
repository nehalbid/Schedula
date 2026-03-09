package app.schedula.ui.appointment

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import app.schedula.data.model.Appointment
import app.schedula.data.model.AppointmentStatus
import app.schedula.data.remote.FirebaseService
import app.schedula.data.repository.DoctorRepository
import app.schedula.utils.NotificationHelper
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AppointmentViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private val doctorRepository = DoctorRepository()

    private val listeners = mutableListOf<ListenerRegistration>()

    // ✅ ONLY ACTIVE APPOINTMENTS
    private val _appointments =
        MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> =
        _appointments

    private val _averageMinutes = MutableStateFlow(30L)
    val averageMinutes: StateFlow<Long> = _averageMinutes

    private var lastNotifiedId: String? = null

    init {
        loadAppointments()
    }

    private fun loadAppointments() {

        val currentUser = firebaseService.auth.currentUser
        val userId = currentUser?.uid ?: return

        val appointmentListener = firebaseService.firestore
            .collection("appointments")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("AppointmentVM", "Firestore Error", error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Appointment::class.java)
                        ?.copy(id = it.id)
                } ?: emptyList()

                // ✅ Filter and Sort Locally for better reliability
                _appointments.value = list.filter {
                    it.status == AppointmentStatus.UPCOMING.name ||
                            it.status == AppointmentStatus.CHECKED_IN.name ||
                            it.status == AppointmentStatus.ONGOING.name ||
                            it.status == AppointmentStatus.RESCHEDULE_REQUESTED.name
                }.sortedByDescending { it.createdAt }

                list.firstOrNull()?.doctorId?.let { doctorId ->
                    listenDoctorAverage(doctorId)
                }
            }
        
        listeners.add(appointmentListener)
    }

    private fun listenDoctorAverage(doctorId: String) {

        val doctorListener = firebaseService.firestore
            .collection("doctors")
            .document(doctorId)
            .addSnapshotListener { snapshot, _ ->
                val avg =
                    snapshot?.getLong("averageConsultationMinutes") ?: 30L
                _averageMinutes.value = avg.coerceAtLeast(5L)
            }
        
        listeners.add(doctorListener)
    }

    fun checkForOngoingAndNotify(context: Context) {

        val ongoing = _appointments.value.firstOrNull {
            it.status == AppointmentStatus.ONGOING.name
        }
        
        if (ongoing != null) {
            if (lastNotifiedId != ongoing.id) {
                lastNotifiedId = ongoing.id
                NotificationHelper.showNotification(
                    context,
                    ongoing.id.hashCode(),
                    "It's Your Turn!",
                    "Dr. ${ongoing.doctorName} is ready for you."
                )
            }
            return
        }

        val resched = _appointments.value.firstOrNull {
            it.status == AppointmentStatus.RESCHEDULE_REQUESTED.name
        }
        
        if (resched != null && lastNotifiedId != resched.id + "_resched") {
            lastNotifiedId = resched.id + "_resched"
            NotificationHelper.showNotification(
                context,
                resched.id.hashCode(),
                "Doctor Unavailable",
                "Dr. ${resched.doctorName} had an emergency. Please reschedule your appointment."
            )
        }
    }

    fun canCheckIn(appointment: Appointment): Boolean {
        if (appointment.status != AppointmentStatus.UPCOMING.name)
            return false

        // FALLBACK: Reconstruct date if appointmentDate is missing
        val apptTimeMillis = appointment.appointmentDate?.time ?: try {
            val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH)
            sdf.parse("${appointment.date} ${appointment.time}")?.time
        } catch (e: Exception) { null } ?: return false

        val now = System.currentTimeMillis()
        
        // Updated Window: 10 mins before to 5 mins after
        val startWindow = apptTimeMillis - (10 * 60 * 1000)
        val endWindow = apptTimeMillis + (5 * 60 * 1000)

        return now in startWindow..endWindow
    }

    fun checkIn(appointmentId: String) {
        firebaseService.firestore
            .collection("appointments")
            .document(appointmentId)
            .update(
                mapOf(
                    "status" to AppointmentStatus.CHECKED_IN.name,
                    "checkedInAt" to Date()
                )
            )
    }

    fun cancelAppointment(appointment: Appointment) {

        if (appointment.status != AppointmentStatus.UPCOMING.name &&
            appointment.status != AppointmentStatus.CHECKED_IN.name &&
            appointment.status != AppointmentStatus.RESCHEDULE_REQUESTED.name
        ) return

        doctorRepository.cancelAppointmentAndReleaseSlot(
            appointmentId = appointment.id,
            doctorId = appointment.doctorId,
            slotId = appointment.slotId,
            onSuccess = {},
            onFailure = {
                Log.e("CancelError", it)
            }
        )
    }

    fun getQueuePosition(appointment: Appointment): Int {

        val queueList = _appointments.value.filter {
            it.doctorId == appointment.doctorId &&
                    it.date == appointment.date &&
                    it.status == AppointmentStatus.CHECKED_IN.name
        }.sortedBy { it.checkedInAt }

        return queueList.indexOfFirst {
            it.id == appointment.id
        } + 1
    }

    fun getRemainingSeconds(
        appointment: Appointment
    ): Long {

        val position = getQueuePosition(appointment)
        if (position <= 1) return 0

        val base = appointment.checkedInAt ?: return 0

        val estimatedStart =
            base.time + TimeUnit.MINUTES.toMillis(
                (position - 1) *
                        _averageMinutes.value
            )

        val diff =
            estimatedStart - System.currentTimeMillis()

        return if (diff > 0) diff / 1000 else 0
    }

    fun isDoctorBusy(appointment: Appointment): Boolean {
        return _appointments.value.any {
            it.doctorId == appointment.doctorId &&
                    it.status == AppointmentStatus.ONGOING.name
        }
    }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}
