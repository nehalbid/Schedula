package app.schedula.data.repository

import android.util.Log
import app.schedula.data.model.Doctor
import app.schedula.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

data class AdminStats(
    val doctors: Int = 0,
    val patients: Int = 0,
    val appointments: Int = 0,
    val topSpecialties: Map<String, Int> = emptyMap()
)

data class AdminAppointment(
    val id: String = "",
    val doctorName: String = "",
    val patientName: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = ""
)

class AdminRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun normalizeToFullFormat(phone: String): String {
        val digits = phone.filter { it.isDigit() }.takeLast(10)
        return if (digits.length == 10) "+91$digits" else ""
    }

    private fun normalizeForMatch(phone: String): String {
        val digits = phone.filter { it.isDigit() }.takeLast(10)
        return if (digits.length == 10) digits else ""
    }

    // ---------------- STATS ----------------

    fun listenDoctorCount(onUpdate: (Int) -> Unit): ListenerRegistration {
        return firestore.collection("doctors")
            .addSnapshotListener { snap, _ ->
                onUpdate(snap?.size() ?: 0)
            }
    }

    fun listenPatientCount(onUpdate: (Int) -> Unit): ListenerRegistration {
        return firestore.collection("users")
            .whereEqualTo("role", "PATIENT")
            .addSnapshotListener { snap, _ ->
                onUpdate(snap?.size() ?: 0)
            }
    }

    fun listenAppointmentCount(onUpdate: (Int) -> Unit): ListenerRegistration {
        return firestore.collection("appointments")
            .addSnapshotListener { snap, _ ->
                onUpdate(snap?.size() ?: 0)
            }
    }

    fun listenTopSpecialties(onUpdate: (Map<String, Int>) -> Unit): ListenerRegistration {
        return firestore.collection("doctors")
            .addSnapshotListener { snap, _ ->
                val counts = snap?.documents?.mapNotNull { it.getString("specialty") }
                    ?.groupingBy { it }
                    ?.eachCount()
                    ?.toList()
                    ?.sortedByDescending { it.second }
                    ?.take(3)
                    ?.toMap() ?: emptyMap()
                onUpdate(counts)
            }
    }

    // ---------------- DOCTORS ----------------

    fun listenDoctors(onUpdate: (List<Doctor>) -> Unit): ListenerRegistration {
        return firestore.collection("doctors")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    val doctor = doc.toObject(Doctor::class.java)
                    doctor?.copy(id = doc.id)
                } ?: emptyList()
                onUpdate(list)
            }
    }

    // ---------------- CREATE DOCTOR ----------------

    fun createDoctor(doctor: Doctor, onResult: (Boolean, String?) -> Unit) {
        val fullPhone = normalizeToFullFormat(doctor.phone)
        val matchDigits = normalizeForMatch(fullPhone)

        if (fullPhone.isEmpty()) {
            onResult(false, "Invalid phone number. Must be 10 digits.")
            return
        }

        firestore.collection("users")
            .get()
            .addOnSuccessListener { query ->
                val batch = firestore.batch()
                
                val existingUserDoc = query.documents.find { 
                    val p = it.getString("phone") ?: ""
                    p.filter { c -> c.isDigit() }.takeLast(10) == matchDigits 
                }

                val uid = existingUserDoc?.id ?: fullPhone

                val doctorRef = firestore.collection("doctors").document(uid)
                val userRef = firestore.collection("users").document(uid)

                val doctorData = doctor.copy(id = uid, phone = fullPhone)
                
                val user = if (existingUserDoc != null) {
                    val existingUser = existingUserDoc.toObject(User::class.java)
                    existingUser?.copy(
                        uid = uid, 
                        role = "DOCTOR", 
                        phone = fullPhone,
                        updatedAt = System.currentTimeMillis()
                    ) ?: User(uid = uid, phone = fullPhone, role = "DOCTOR")
                } else {
                    User(uid = uid, phone = fullPhone, role = "DOCTOR")
                }

                batch.set(doctorRef, doctorData.toSafeMap())
                batch.set(userRef, user.toSafeMap())

                batch.commit().addOnSuccessListener {
                    onResult(true, null)
                }.addOnFailureListener {
                    Log.e("AdminRepository", "Failed to create doctor: ${it.message}")
                    onResult(false, it.message)
                }
            }
            .addOnFailureListener {
                onResult(false, it.message)
            }
    }

    // ---------------- UPDATE DOCTOR ----------------

    fun updateDoctor(doctorId: String, doctor: Doctor, onResult: (Boolean, String?) -> Unit) {
        val fullPhone = normalizeToFullFormat(doctor.phone)
        
        if (fullPhone.isEmpty()) {
            onResult(false, "Invalid phone number. Must be 10 digits.")
            return
        }

        val batch = firestore.batch()
        
        val doctorRef = firestore.collection("doctors").document(doctorId)
        val userRef = firestore.collection("users").document(doctorId)

        batch.set(doctorRef, doctor.copy(id = doctorId, phone = fullPhone).toSafeMap())
        
        val userUpdates = mapOf(
            "phone" to fullPhone,
            "role" to "DOCTOR",
            "updatedAt" to System.currentTimeMillis()
        )
        batch.set(userRef, userUpdates, SetOptions.merge())

        batch.commit().addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener {
            Log.e("AdminRepository", "Failed to update doctor: ${it.message}")
            onResult(false, it.message)
        }
    }

    // ---------------- DELETE DOCTOR ----------------

    fun deleteDoctor(doctorId: String) {
        val batch = firestore.batch()

        val doctorRef = firestore.collection("doctors").document(doctorId)
        val userRef = firestore.collection("users").document(doctorId)

        batch.delete(doctorRef)
        batch.delete(userRef)

        batch.commit()
    }

    // ---------------- APPOINTMENTS ----------------

    fun listenAppointments(
        onUpdate: (List<AdminAppointment>) -> Unit
    ): ListenerRegistration {

        return firestore.collection("appointments")
            .addSnapshotListener { snap, _ ->

                val list = snap?.documents?.map { doc ->

                    val patientMap = doc.get("patientDetails") as? Map<*, *>

                    val patientName = patientMap?.get("fullName") as? String ?: "Unknown"

                    AdminAppointment(
                        id = doc.id,
                        doctorName = doc.getString("doctorName") ?: "",
                        patientName = patientName,
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        status = doc.getString("status") ?: ""
                    )
                } ?: emptyList()

                onUpdate(list)
            }
    }
}
