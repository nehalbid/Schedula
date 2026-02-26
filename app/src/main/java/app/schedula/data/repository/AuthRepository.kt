package app.schedula.data.repository

import android.app.Activity
import app.schedula.data.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // -------------------------
    // LOGIN STATUS
    // -------------------------

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // -------------------------
    // SEND OTP (Callback based)
    // -------------------------

    fun sendOtp(
        phone: String,
        activity: Activity,
        onCodeSent: (String) -> Unit,
        onError: (String) -> Unit,
        onAutoVerify: () -> Unit
    ) {

        val callbacks =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { result ->
                            result.user?.let { saveUserToFirestore(it) } // NOT suspend
                            onAutoVerify()
                        }
                        .addOnFailureListener {
                            onError(it.message ?: "Auto verification failed")
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onError(e.message ?: "Verification failed")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    resendToken = token
                    onCodeSent(verificationId)
                }
            }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // -------------------------
    // VERIFY OTP (Coroutine Safe)
    // -------------------------

    suspend fun verifyOtp(
        verificationId: String,
        code: String
    ): Result<Unit> {

        return try {

            val credential =
                PhoneAuthProvider.getCredential(verificationId, code)

            val result = auth.signInWithCredential(credential).await()

            result.user?.let { saveUserToFirestore(it) }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------
    // SAVE USER (NON-SUSPEND SAFE VERSION)
    // -------------------------

    private fun saveUserToFirestore(firebaseUser: FirebaseUser) {

        val user = User(
            uid = firebaseUser.uid,
            phone = firebaseUser.phoneNumber ?: "",
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(firebaseUser.uid)
            .set(user, SetOptions.merge())
    }

    // -------------------------
    // LOGOUT
    // -------------------------

    fun logout() {
        auth.signOut()
    }
}