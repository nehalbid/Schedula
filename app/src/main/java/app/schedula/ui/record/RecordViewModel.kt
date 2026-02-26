package app.schedula.ui.record

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.schedula.data.model.Appointment
import app.schedula.data.remote.FirebaseService
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RecordViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private var appointmentsListener: ListenerRegistration? = null

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    init {
        loadRecords()
    }

    private fun loadRecords() {
        val userId = firebaseService.auth.currentUser?.phoneNumber
        if (userId == null) return

        appointmentsListener = firebaseService.getAppointments(userId)
            .whereIn("status", listOf("Completed", "Cancelled"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RecordViewModel", "Error loading records", error)
                    return@addSnapshotListener
                }

                val appointmentList = snapshot?.documents?.mapNotNull {
                    try {
                        it.toObject(Appointment::class.java)?.copy(id = it.id)
                    } catch (e: Exception) {
                        Log.e("RecordViewModel", "Failed to deserialize appointment ${it.id}", e)
                        null
                    }
                } ?: emptyList()

                _appointments.value = appointmentList
            }
    }

    override fun onCleared() {
        super.onCleared()
        appointmentsListener?.remove()
    }
}