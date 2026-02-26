package app.schedula.data.repository

import app.schedula.data.model.Appointment
import app.schedula.data.remote.FirebaseService
import kotlinx.coroutines.tasks.await

class AppointmentRepository {

    private val firebase = FirebaseService()

    suspend fun getAppointments(): List<Appointment> {
        val snapshot = firebase.firestore
            .collection("appointments")
            .get()
            .await()

        return snapshot.toObjects(Appointment::class.java)
    }

    suspend fun bookAppointment(appointment: Appointment) {
        firebase.firestore
            .collection("appointments")
            .add(appointment)
            .await()
    }

    suspend fun getUserAppointments(userId: String): List<Appointment> {
        val snapshot = firebase.firestore
            .collection("appointments")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return snapshot.toObjects(Appointment::class.java)
    }
}