package app.schedula.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import app.schedula.data.model.User
import app.schedula.data.remote.FirebaseService
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val firebaseService = FirebaseService()

    private val _verificationId = MutableStateFlow<String?>(null)

    private val _authState = MutableStateFlow<Pair<String, String?>>(Pair("idle", null))
    val authState: StateFlow<Pair<String, String?>> = _authState

    fun sendOtp(activity: Activity, phoneNumber: String) {
        _authState.value = Pair("loading", null)

        val options = PhoneAuthOptions.newBuilder(firebaseService.auth)
            .setPhoneNumber("+91$phoneNumber")
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

    fun verifyOtp(code: String) {
        _authState.value = Pair("loading", null)
        val credential = PhoneAuthProvider.getCredential(_verificationId.value!!, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        firebaseService.auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null && firebaseUser.phoneNumber != null) {
                        saveUserToFirestore(firebaseUser)
                    } else {
                        _authState.value = Pair("error", "Could not retrieve user phone number.")
                    }
                } else {
                    _authState.value = Pair("error", task.exception?.message)
                }
            }
    }

    private fun saveUserToFirestore(firebaseUser: FirebaseUser) {
        val userRef = firebaseService.firestore.collection("users").document(firebaseUser.phoneNumber!!)

        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val newUser = User(
                    uid = firebaseUser.uid, // Still store the original Firebase UID
                    phone = firebaseUser.phoneNumber!!,
                    createdAt = System.currentTimeMillis()
                )
                userRef.set(newUser).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = Pair("success", null)
                    } else {
                        _authState.value = Pair("error", "Failed to create user profile.")
                    }
                }
            } else {
                // User already exists, proceed to login
                _authState.value = Pair("success", null)
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseService.auth.currentUser
    }

    fun checkLoginStatus(): Boolean {
        return firebaseService.auth.currentUser != null
    }

    fun logout() {
        firebaseService.auth.signOut()
    }

    fun resetState() {
        _authState.value = Pair("idle", null)
    }
}
