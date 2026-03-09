package app.schedula.ui.record

import android.util.Log
import androidx.lifecycle.ViewModel
import app.schedula.data.model.Appointment
import app.schedula.data.model.AppointmentStatus
import app.schedula.data.remote.FirebaseService
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecordViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private val listeners = mutableListOf<ListenerRegistration>()

    private val _appointments =
        MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> =
        _appointments

    private val _isLoading =
        MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> =
        _isLoading

    private val _error =
        MutableStateFlow<String?>(null)
    val error: StateFlow<String?> =
        _error

    init {
        loadRecords()
    }

    private fun loadRecords() {

        val userId = firebaseService.auth.currentUser?.uid
        if (userId == null) {
            _isLoading.value = false
            _error.value = "User not logged in"
            return
        }

        _isLoading.value = true

        val recordListener = firebaseService.firestore
            .collection("appointments")
            .whereEqualTo("userId", userId)
            .whereIn(
                "status",
                listOf(
                    AppointmentStatus.COMPLETED.name,
                    AppointmentStatus.CANCELLED.name,
                    AppointmentStatus.NO_SHOW.name
                )
            )
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e(
                        "RecordViewModel",
                        "Error loading records",
                        error
                    )
                    _error.value = error.message
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val appointmentList =
                    snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(Appointment::class.java)
                                ?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e(
                                "RecordViewModel",
                                "Deserialize failed: ${doc.id}",
                                e
                            )
                            null
                        }
                    } ?: emptyList()

                _appointments.value = appointmentList
                _isLoading.value = false
                _error.value = null
            }
        
        listeners.add(recordListener)
    }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}
