package app.schedula.ui.doctorpanel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.schedula.data.model.*
import app.schedula.data.remote.FirebaseService
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class DoctorPanelViewModel : ViewModel() {

    private val firebaseService = FirebaseService()

    private val listeners = mutableListOf<ListenerRegistration>()
    private var noShowMonitorJob: Job? = null
    private var slotGeneratorJob: Job? = null

    private val _doctor = MutableStateFlow<Doctor?>(null)
    val doctor: StateFlow<Doctor?> = _doctor

    private val _todayAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val todayAppointments: StateFlow<List<Appointment>> = _todayAppointments

    private val _historyAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val historyAppointments: StateFlow<List<Appointment>> = _historyAppointments

    init {
        loadDoctorData()
        startAutoNoShowMonitor()
        startDailySlotGenerator()
    }

    private fun loadDoctorData() {
        val currentUser = firebaseService.auth.currentUser ?: return
        val uid = currentUser.uid
        loadDoctorProfile(uid)
        listenTodayAppointments(uid)
        listenHistoryAppointments(uid)
    }

    private fun loadDoctorProfile(doctorId: String) {
        val listener = firebaseService.firestore.collection("doctors").document(doctorId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _doctor.value = snapshot.toObject(Doctor::class.java)?.copy(id = snapshot.id)
                }
            }
        listeners.add(listener)
    }

    private fun listenTodayAppointments(doctorId: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())

        val listener = firebaseService.firestore.collection("appointments")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("date", today)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Appointment::class.java)?.copy(id = it.id)
                } ?: emptyList()

                _todayAppointments.value = list
            }
        listeners.add(listener)
    }

    private fun listenHistoryAppointments(doctorId: String) {

        val listener = firebaseService.firestore.collection("appointments")
            .whereEqualTo("doctorId", doctorId)
            .addSnapshotListener { snapshot, _ ->

                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Appointment::class.java)?.copy(id = it.id)
                } ?: emptyList()

                val history = list.filter {
                    it.status == AppointmentStatus.COMPLETED.name ||
                            it.status == AppointmentStatus.CANCELLED.name ||
                            it.status == AppointmentStatus.NO_SHOW.name
                }

                _historyAppointments.value = history
            }
        listeners.add(listener)
    }

    // ---------------- NO SHOW MONITOR ----------------

    private fun startAutoNoShowMonitor() {

        noShowMonitorJob = viewModelScope.launch {

            while (true) {

                val now = Date()

                _todayAppointments.value.forEach { appointment ->

                    val appointmentTime = appointment.appointmentDate ?: return@forEach

                    if (appointment.status == AppointmentStatus.UPCOMING.name) {

                        val tenMinAfter = Date(appointmentTime.time + 10 * 60 * 1000)

                        if (now.after(tenMinAfter)) {
                            markNoShowAndReleaseSlot(appointment)
                        }
                    }
                }

                delay(60000)
            }
        }
    }

    private fun markNoShowAndReleaseSlot(appointment: Appointment) {

        firebaseService.firestore.runTransaction { transaction ->

            val appointmentRef = firebaseService.firestore
                .collection("appointments")
                .document(appointment.id)

            val slotRef = firebaseService.firestore
                .collection("doctors")
                .document(appointment.doctorId)
                .collection("slots")
                .document(appointment.slotId)

            transaction.update(appointmentRef, "status", AppointmentStatus.NO_SHOW.name)
            transaction.update(slotRef, "isBooked", false)
        }
    }

    // ---------------- QUEUE CONTROL ----------------

    fun callNextPatient() {

        val ongoing = _todayAppointments.value.firstOrNull {
            it.status == AppointmentStatus.ONGOING.name
        }

        ongoing?.let { completeConsultation(it.id) }

        val nextPatient = _todayAppointments.value
            .filter {
                it.status == AppointmentStatus.CHECKED_IN.name ||
                        it.status == AppointmentStatus.UPCOMING.name
            }
            .sortedBy { it.appointmentDate }
            .firstOrNull() ?: return

        startConsultation(nextPatient)
    }

    fun forceStart(appointment: Appointment) {

        val ongoing = _todayAppointments.value.firstOrNull {
            it.status == AppointmentStatus.ONGOING.name
        }

        ongoing?.let { completeConsultation(it.id) }

        startConsultation(appointment)
    }

    private fun startConsultation(appointment: Appointment) {

        val updates = mutableMapOf<String, Any>(
            "status" to AppointmentStatus.ONGOING.name,
            "startedAt" to Date()
        )

        if (appointment.consultingType.contains("Online", true)) {
            updates["meetingLink"] =
                "https://meet.jit.si/Schedula_${appointment.id.take(8)}"
        }

        firebaseService.firestore.collection("appointments")
            .document(appointment.id)
            .update(updates)
    }

    fun completeConsultation(appointmentId: String) {

        firebaseService.firestore.collection("appointments")
            .document(appointmentId)
            .update(
                mapOf(
                    "status" to AppointmentStatus.COMPLETED.name,
                    "completedAt" to Date()
                )
            )
    }

    // ---------------- PRESCRIPTION ----------------

    fun submitPrescription(prescription: Prescription, onComplete: () -> Unit) {

        val prescriptionRef = firebaseService.firestore
            .collection("prescriptions")
            .document()

        prescriptionRef.set(
            prescription.copy(
                id = prescriptionRef.id,
                createdAt = Date()
            )
        ).addOnSuccessListener {
            onComplete()
        }
    }

    // ---------------- PROFILE ----------------

    fun updateProfile(
        name: String,
        specialty: String,
        experience: Int,
        fee: Int,
        onResult: (Boolean) -> Unit
    ) {

        val doctorId = _doctor.value?.id ?: return

        firebaseService.firestore.collection("doctors")
            .document(doctorId)
            .update(
                mapOf(
                    "name" to name,
                    "specialty" to specialty,
                    "experience" to experience,
                    "fee" to fee,
                    "profileCompleted" to true
                )
            )
            .addOnSuccessListener {

                firebaseService.firestore.collection("users")
                    .document(doctorId)
                    .update("name", name)
                    .addOnCompleteListener { onResult(true) }
            }
    }

    fun updateProfileFull(
        name: String,
        specialty: String,
        gender: String,
        experience: Int,
        fee: Int,
        onResult: (Boolean) -> Unit
    ) {
        val doctorId = _doctor.value?.id ?: return
        firebaseService.firestore.collection("doctors").document(doctorId)
            .update(
                mapOf(
                    "name" to name,
                    "specialty" to specialty,
                    "gender" to gender,
                    "experience" to experience,
                    "fee" to fee,
                    "profileCompleted" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).addOnSuccessListener {
                firebaseService.firestore.collection("users").document(doctorId).update("name", name)
                    .addOnCompleteListener { onResult(it.isSuccessful) }
            }.addOnFailureListener { onResult(false) }
    }

    // ---------------- SCHEDULE ----------------

    fun updateWeeklySchedule(
        mon: Pair<String, String>, mon2: Pair<String, String>,
        tue: Pair<String, String>, tue2: Pair<String, String>,
        wed: Pair<String, String>, wed2: Pair<String, String>,
        thu: Pair<String, String>, thu2: Pair<String, String>,
        fri: Pair<String, String>, fri2: Pair<String, String>,
        sat: Pair<String, String>, sat2: Pair<String, String>,
        sun: Pair<String, String>, sun2: Pair<String, String>,
        slotDuration: Int,
        onResult: (Boolean) -> Unit
    ) {
        val doctorId = _doctor.value?.id ?: return
        val updates = mapOf(
            "mondayStart" to mon.first, "mondayEnd" to mon.second, "mondayStart2" to mon2.first, "mondayEnd2" to mon2.second,
            "tuesdayStart" to tue.first, "tuesdayEnd" to tue.second, "tuesdayStart2" to tue2.first, "tuesdayEnd2" to tue2.second,
            "wednesdayStart" to wed.first, "wednesdayEnd" to wed.second, "wednesdayStart2" to wed2.first, "wednesdayEnd2" to wed2.second,
            "thursdayStart" to thu.first, "thursdayEnd" to thu.second, "thursdayStart2" to thu2.first, "thursdayEnd2" to thu2.second,
            "fridayStart" to fri.first, "fridayEnd" to fri.second, "fridayStart2" to fri2.first, "fridayEnd2" to fri2.second,
            "saturdayStart" to sat.first, "saturdayEnd" to sat.second, "saturdayStart2" to sat2.first, "saturdayEnd2" to sat2.second,
            "sundayStart" to sun.first, "sundayEnd" to sun.second, "sundayStart2" to sun2.first, "sundayEnd2" to sun2.second,
            "slotDuration" to slotDuration,
            "updatedAt" to System.currentTimeMillis()
        )
        firebaseService.firestore.collection("doctors").document(doctorId).update(updates).addOnSuccessListener {
            // Force refresh local doctor object then generate slots efficiently
            firebaseService.firestore.collection("doctors").document(doctorId).get().addOnSuccessListener { snapshot ->
                val updatedDoctor = snapshot.toObject(Doctor::class.java)?.copy(id = snapshot.id)
                if (updatedDoctor != null) {
                    _doctor.value = updatedDoctor
                    generateSlotsForRange(7) { success, _ ->
                        onResult(success)
                    }
                } else {
                    onResult(true)
                }
            }
        }
    }

    fun generateSlotsForDate(dateStr: String, onResult: (Boolean, String?) -> Unit) {
        val doc = _doctor.value ?: return
        viewModelScope.launch {
            try {
                val dayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val date = sdf.parse(dateStr) ?: Date()
                val dayOfWeek = dayFormat.format(date).lowercase()

                if (doc.offDates.contains(dateStr)) {
                    onResult(false, "This date is marked as an off day.")
                    return@launch
                }

                val startFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val displayFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
                val slotsCollection = firebaseService.firestore.collection("doctors").document(doc.id).collection("slots")

                val shifts = getShiftsForDay(doc, dayOfWeek)
                if (shifts.first.first.isBlank() && shifts.second.first.isBlank()) {
                    onResult(false, "No shifts defined for $dayOfWeek.")
                    return@launch
                }

                // Efficiently commit changes in a single batch for one day
                val batch = firebaseService.firestore.batch()
                
                // Cleanup unbooked
                val existing = slotsCollection.whereEqualTo("date", dateStr).whereEqualTo("isBooked", false).get().await()
                existing.documents.forEach { batch.delete(it.reference) }

                // Add new slots
                generateShiftSlots(
                    shifts.first,
                    slotsCollection,
                    doc.id,
                    dateStr,
                    startFormatter,
                    displayFormatter,
                    doc.slotDuration
                )

                generateShiftSlots(
                    shifts.second,
                    slotsCollection,
                    doc.id,
                    dateStr,
                    startFormatter,
                    displayFormatter,
                    doc.slotDuration
                )

                batch.commit().await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // ---------------- SIMPLE SCHEDULE (NEW UI) ----------------

    fun updateSimpleSchedule(
        template: String,
        workingDays: List<String>,
        slotDuration: Int,
        offDates: List<String>,
        onResult: (Boolean) -> Unit
    ) {

        val doctorId = _doctor.value?.id ?: return

        val (s1Start, s1End, s2Start, s2End) = when (template) {

            "Morning" -> listOf("09:00", "13:00", "", "")
            "Evening" -> listOf("", "", "17:00", "21:00")
            "Full" -> listOf("09:00", "13:00", "17:00", "21:00")
            else -> listOf("", "", "", "")
        }

        val scheduleMap = mutableMapOf<String, Any>()

        val days = mapOf(
            "Mon" to "monday",
            "Tue" to "tuesday",
            "Wed" to "wednesday",
            "Thu" to "thursday",
            "Fri" to "friday",
            "Sat" to "saturday",
            "Sun" to "sunday"
        )

        days.forEach { (short, full) ->

            if (workingDays.contains(short)) {

                scheduleMap["${full}Start"] = s1Start
                scheduleMap["${full}End"] = s1End
                scheduleMap["${full}Start2"] = s2Start
                scheduleMap["${full}End2"] = s2End

            } else {

                scheduleMap["${full}Start"] = ""
                scheduleMap["${full}End"] = ""
                scheduleMap["${full}Start2"] = ""
                scheduleMap["${full}End2"] = ""
            }
        }

        scheduleMap["slotDuration"] = slotDuration
        scheduleMap["offDates"] = offDates
        scheduleMap["updatedAt"] = System.currentTimeMillis()

        firebaseService.firestore.collection("doctors")
            .document(doctorId)
            .update(scheduleMap)
            .addOnSuccessListener {
                // Refresh local doctor object then generate slots efficiently
                firebaseService.firestore.collection("doctors").document(doctorId).get().addOnSuccessListener { snapshot ->
                    val updatedDoctor = snapshot.toObject(Doctor::class.java)?.copy(id = snapshot.id)
                    if (updatedDoctor != null) {
                        _doctor.value = updatedDoctor
                        generateSlotsForRange(7) { success, _ ->
                            onResult(success)
                        }
                    } else {
                        onResult(true)
                    }
                }
            }
    }

    // ---------------- SLOT GENERATION ----------------

    fun generateSlotsForRange(daysCount: Int, onResult: (Boolean, String?) -> Unit) {

        val doc = _doctor.value ?: return

        viewModelScope.launch {

            try {

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val dayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)

                val startFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val displayFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

                val slotsCollection = firebaseService.firestore
                    .collection("doctors")
                    .document(doc.id)
                    .collection("slots")

                for (i in 0 until daysCount) {

                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, i)

                    val dateStr = sdf.format(cal.time)
                    val dayOfWeek = dayFormat.format(cal.time).lowercase()

                    if (doc.offDates.contains(dateStr)) continue

                    val shifts = getShiftsForDay(doc, dayOfWeek)

                    if (shifts.first.first.isBlank() && shifts.second.first.isBlank())
                        continue

                    // delete old unbooked slots
                    val existing = slotsCollection
                        .whereEqualTo("date", dateStr)
                        .whereEqualTo("isBooked", false)
                        .get()
                        .await()

                    existing.documents.forEach {
                        it.reference.delete().await()
                    }

                    // generate shift 1
                    generateShiftSlots(
                        shifts.first,
                        slotsCollection,
                        doc.id,
                        dateStr,
                        startFormatter,
                        displayFormatter,
                        doc.slotDuration
                    )

                    // generate shift 2
                    generateShiftSlots(
                        shifts.second,
                        slotsCollection,
                        doc.id,
                        dateStr,
                        startFormatter,
                        displayFormatter,
                        doc.slotDuration
                    )
                }

                onResult(true, "Slots generated")

            } catch (e: Exception) {

                onResult(false, e.message)
            }
        }
    }

    private suspend fun generateShiftSlots(
        shift: Pair<String, String>,
        col: com.google.firebase.firestore.CollectionReference,
        docId: String,
        date: String,
        startF: DateTimeFormatter,
        dispF: DateTimeFormatter,
        dur: Int
    ) {

        if (shift.first.isBlank() || dur <= 0) return

        val startTime = try {
            LocalTime.parse(shift.first, startF)
        } catch (e: Exception) {
            return
        }

        val endTime = try {
            LocalTime.parse(shift.second, startF)
        } catch (e: Exception) {
            return
        }

        var current = startTime

        while (current.isBefore(endTime)) {

            col.document().set(
                Slot(
                    doctorId = docId,
                    date = date,
                    time = current.format(dispF),
                    isBooked = false
                )
            ).await()

            current = current.plusMinutes(dur.toLong())
        }
    }

    private fun getShiftsForDay(
        doc: Doctor,
        day: String
    ): Pair<Pair<String, String>, Pair<String, String>> {

        return when (day) {

            "monday" -> Pair(
                Pair(doc.mondayStart, doc.mondayEnd),
                Pair(doc.mondayStart2, doc.mondayEnd2)
            )

            "tuesday" -> Pair(
                Pair(doc.tuesdayStart, doc.tuesdayEnd),
                Pair(doc.tuesdayStart2, doc.tuesdayEnd2)
            )

            "wednesday" -> Pair(
                Pair(doc.wednesdayStart, doc.wednesdayEnd),
                Pair(doc.wednesdayStart2, doc.wednesdayEnd2)
            )

            "thursday" -> Pair(
                Pair(doc.thursdayStart, doc.thursdayEnd),
                Pair(doc.thursdayStart2, doc.thursdayEnd2)
            )

            "friday" -> Pair(
                Pair(doc.fridayStart, doc.fridayEnd),
                Pair(doc.fridayStart2, doc.fridayEnd2)
            )

            "saturday" -> Pair(
                Pair(doc.saturdayStart, doc.saturdayEnd),
                Pair(doc.saturdayStart2, doc.saturdayEnd2)
            )

            "sunday" -> Pair(
                Pair(doc.sundayStart, doc.sundayEnd),
                Pair(doc.sundayStart2, doc.sundayEnd2)
            )

            else -> Pair(Pair("", ""), Pair("", ""))
        }
    }

    // ---------------- AUTO SLOT GENERATOR ----------------

    private fun startDailySlotGenerator() {

        slotGeneratorJob = viewModelScope.launch {

            while (_doctor.value == null) {
                delay(1000)
            }

            while (true) {

                generateSlotsForRange(7) { _, _ -> }

                delay(24 * 60 * 60 * 1000)
            }
        }
    }

    fun toggleOffDay(date: String, onResult: (Boolean) -> Unit) {
        val doctorId = _doctor.value?.id ?: return
        val currentOffDates = _doctor.value?.offDates?.toMutableList() ?: mutableListOf()
        if (currentOffDates.contains(date)) currentOffDates.remove(date) else currentOffDates.add(date)
        firebaseService.firestore.collection("doctors").document(doctorId).update("offDates", currentOffDates)
            .addOnSuccessListener {
                if (currentOffDates.contains(date)) clearSlotsForDate(date)
                onResult(true)
            }
    }

    private fun clearSlotsForDate(date: String) {
        val doctorId = _doctor.value?.id ?: return
        viewModelScope.launch {
            val slots = firebaseService.firestore.collection("doctors").document(doctorId).collection("slots")
                .whereEqualTo("date", date).whereEqualTo("isBooked", false).get().await()
            val batch = firebaseService.firestore.batch()
            slots.forEach { batch.delete(it.reference) }
            batch.commit()
        }
    }

    // ---------------- EMERGENCY RESCHEDULE ----------------

    fun requestRescheduleForAllToday(onResult: (Boolean, String?) -> Unit) {

        viewModelScope.launch {

            try {

                val batch = firebaseService.firestore.batch()

                _todayAppointments.value
                    .filter {
                        it.status == AppointmentStatus.UPCOMING.name ||
                                it.status == AppointmentStatus.CHECKED_IN.name
                    }
                    .forEach { appt ->

                        val appointmentRef = firebaseService.firestore
                            .collection("appointments")
                            .document(appt.id)

                        val slotRef = firebaseService.firestore
                            .collection("doctors")
                            .document(appt.doctorId)
                            .collection("slots")
                            .document(appt.slotId)

                        batch.update(
                            appointmentRef,
                            "status",
                            AppointmentStatus.RESCHEDULE_REQUESTED.name
                        )

                        batch.update(
                            slotRef,
                            "isBooked",
                            false
                        )
                    }

                batch.commit().await()

                onResult(true, "Patients notified.")

            } catch (e: Exception) {

                onResult(false, e.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
        listeners.clear()
        noShowMonitorJob?.cancel()
        slotGeneratorJob?.cancel()
    }
}
