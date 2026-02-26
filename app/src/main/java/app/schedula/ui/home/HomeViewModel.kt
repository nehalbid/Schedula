package app.schedula.ui.home

import androidx.lifecycle.ViewModel
import app.schedula.data.model.Doctor
import app.schedula.data.remote.FirebaseService
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private var doctorsListener: ListenerRegistration? = null

    private val _doctors = MutableStateFlow<List<Doctor>>(emptyList())
    val doctors: StateFlow<List<Doctor>> = _doctors

    init {
        loadDoctors()
    }

    private fun loadDoctors() {
        doctorsListener = firebaseService.getDoctors()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val doctorList = snapshot?.documents?.mapNotNull {
                    it.toObject(Doctor::class.java)?.copy(id = it.id)
                } ?: emptyList()

                _doctors.value = doctorList
            }
    }

    override fun onCleared() {
        super.onCleared()
        doctorsListener?.remove()
    }
}
