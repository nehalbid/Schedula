package app.schedula.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import app.schedula.data.model.User
import app.schedula.data.remote.FirebaseService
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val firebaseService = FirebaseService()

    private val _verificationId = MutableStateFlow<String?>(null)
    private val _authState = MutableStateFlow<Pair<String, String?>>(Pair("idle", null))
    val authState: StateFlow<Pair<String, String?>> = _authState

    private fun normalizePhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }.takeLast(10)
        return if (digits.length == 10) "+91$digits" else ""
    }

    private fun getMatchDigits(phone: String): String {
        return phone.filter { it.isDigit() }.takeLast(10)
    }

    // ---------------- SEND OTP ----------------

    fun sendOtp(activity: Activity, phoneNumber: String) {
        val formattedPhone = normalizePhone(phoneNumber)
        if (formattedPhone.isEmpty()) {
            _authState.value = Pair("error", "Invalid phone number. Must be 10 digits.")
            return
        }

        _authState.value = Pair("loading", null)

        val options = PhoneAuthOptions.newBuilder(firebaseService.auth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }
                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    _authState.value = Pair("error", e.message)
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _verificationId.value = verificationId
                    _authState.value = Pair("code_sent", null)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // ---------------- VERIFY OTP ----------------

    fun verifyOtp(code: String) {
        _authState.value = Pair("loading", null)
        val credential = PhoneAuthProvider.getCredential(_verificationId.value!!, code)
        signInWithCredential(credential)
    }

    // ---------------- SIGN IN ----------------

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        firebaseService.auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        handleUserRole(firebaseUser)
                    } else {
                        _authState.value = Pair("error", "User not found.")
                    }
                } else {
                    _authState.value = Pair("error", task.exception?.message)
                }
            }
    }

    // ---------------- HANDLE USER ROLE (ID PROTECTION) ----------------

    private fun handleUserRole(firebaseUser: FirebaseUser) {
        val uid = firebaseUser.uid
        val rawPhone = firebaseUser.phoneNumber ?: ""
        val fullPhone = normalizePhone(rawPhone)
        val matchDigits = getMatchDigits(fullPhone)

        val userRef = firebaseService.firestore.collection("users").document(uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // UID exists. Update format and navigate.
                val role = document.getString("role") ?: "PATIENT"
                // Standard update only for essential fields to avoid adding nulls
                userRef.update(
                    "phone", fullPhone, 
                    "updatedAt", System.currentTimeMillis()
                )
                _authState.value = Pair("${role.lowercase()}_success", null)
            } else {
                // New UID - Search for existing profile by phone field
                firebaseService.firestore.collection("users").get().addOnSuccessListener { query ->
                    val existingDoc = query.documents.find { 
                        getMatchDigits(it.getString("phone") ?: "") == matchDigits 
                    }

                    if (existingDoc != null) {
                        // Found account under different ID (like phone-number ID)
                        val oldId = existingDoc.id
                        if (oldId != uid) {
                            migrateUserToUid(oldId, uid, fullPhone, existingDoc.getString("role") ?: "PATIENT")
                        } else {
                            val role = existingDoc.getString("role") ?: "PATIENT"
                            _authState.value = Pair("${role.lowercase()}_success", null)
                        }
                    } else {
                        // Truly brand new user
                        val newUser = User(uid = uid, phone = fullPhone, role = "PATIENT")
                        // Use toSafeMap() here to prevent null fields in Firestore
                        userRef.set(newUser.toSafeMap()).addOnSuccessListener {
                            _authState.value = Pair("patient_success", null)
                        }
                    }
                }
            }
        }
    }

    private fun migrateUserToUid(oldId: String, newId: String, phone: String, role: String) {
        val batch = firebaseService.firestore.batch()
        val oldRef = firebaseService.firestore.collection("users").document(oldId)
        val newRef = firebaseService.firestore.collection("users").document(newId)

        // Read all old data to preserve it (name, email, etc.)
        oldRef.get().addOnSuccessListener { snap ->
            val data = snap.data?.toMutableMap() ?: mutableMapOf()
            data["uid"] = newId
            data["phone"] = phone
            data["updatedAt"] = System.currentTimeMillis()

            // When migrating, we filter out nulls manually to clean up old data
            val cleanData = data.filterValues { it != null }

            batch.set(newRef, cleanData)
            batch.delete(oldRef)

            // Also migrate Doctor profile if it exists
            val oldDocRef = firebaseService.firestore.collection("doctors").document(oldId)
            val newDocRef = firebaseService.firestore.collection("doctors").document(newId)
            
            oldDocRef.get().addOnSuccessListener { docSnap ->
                if (docSnap.exists()) {
                    val docData = docSnap.data?.toMutableMap() ?: mutableMapOf()
                    docData["id"] = newId
                    batch.set(newDocRef, docData.filterValues { it != null })
                    batch.delete(oldDocRef)
                }
                batch.commit().addOnSuccessListener {
                    _authState.value = Pair("${role.lowercase()}_success", null)
                }
            }
        }
    }

    // ---------------- UTILS ----------------

    fun checkLoginStatus(): Boolean = firebaseService.auth.currentUser != null
    fun logout() = firebaseService.auth.signOut()
    fun resetState() { _authState.value = Pair("idle", null) }
}
