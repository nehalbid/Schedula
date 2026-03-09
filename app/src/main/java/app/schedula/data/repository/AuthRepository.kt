package app.schedula.data.repository

import android.app.Activity
import app.schedula.data.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    /**
     * Normalizes phone to +91 format (last 10 digits).
     */
    private fun formatToFull(phone: String): String {
        val digits = phone.filter { it.isDigit() }.takeLast(10)
        return if (digits.length == 10) "+91$digits" else ""
    }

    /**
     * Extracts only the last 10 digits for search comparison.
     */
    private fun getMatchDigits(phone: String): String {
        return phone.filter { it.isDigit() }.takeLast(10)
    }

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
    // OTP METHODS
    // -------------------------

    fun sendOtp(
        phone: String,
        activity: Activity,
        onCodeSent: (String) -> Unit,
        onError: (String) -> Unit,
        onAutoVerify: () -> Unit
    ) {
        val fullPhone = formatToFull(phone)
        if (fullPhone.isEmpty()) {
            onError("Invalid phone number. Must be 10 digits.")
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        result.user?.let { saveUserToFirestore(it) }
                        onAutoVerify()
                    }
                    .addOnFailureListener { onError(it.message ?: "Auto verification failed") }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.message ?: "Verification failed")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                resendToken = token
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(fullPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyOtp(verificationId: String, code: String): Result<Unit> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let { saveUserToFirestore(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------
    // CORE LOGIC: PREVENT DUPLICATE IDS
    // -------------------------

    private fun saveUserToFirestore(firebaseUser: FirebaseUser) {
        val uid = firebaseUser.uid
        val fullPhone = formatToFull(firebaseUser.phoneNumber ?: "")
        val matchDigits = getMatchDigits(fullPhone)

        val userRef = firestore.collection("users").document(uid)

        // Step 1: Check if this UID already exists
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // User already has an account linked to this UID. Update phone format if needed.
                userRef.update("phone", fullPhone, "updatedAt", System.currentTimeMillis())
            } else {
                // Step 2: New UID - Search ALL users by phone field to see if they exist under a different ID
                firestore.collection("users").get().addOnSuccessListener { query ->
                    
                    val existingDoc = query.documents.find { 
                        getMatchDigits(it.getString("phone") ?: "") == matchDigits 
                    }

                    if (existingDoc != null) {
                        // Found a matching phone record (Admin-created, or old ID format)
                        val oldId = existingDoc.id
                        
                        if (oldId != uid) {
                            // MIGRATION: Move data from the old ID (like a phone-number-ID) to the real Auth UID
                            val batch = firestore.batch()
                            val existingUser = existingDoc.toObject(User::class.java)
                            
                            val migratedUser = existingUser?.copy(
                                uid = uid,
                                phone = fullPhone,
                                updatedAt = System.currentTimeMillis()
                            ) ?: User(uid = uid, phone = fullPhone, role = "PATIENT")

                            batch.set(userRef, migratedUser)
                            batch.delete(firestore.collection("users").document(oldId))

                            // Also migrate Doctor profile data if it was keyed by the old ID
                            val oldDocRef = firestore.collection("doctors").document(oldId)
                            val newDocRef = firestore.collection("doctors").document(uid)

                            oldDocRef.get().addOnSuccessListener { docSnap ->
                                if (docSnap.exists()) {
                                    batch.set(newDocRef, docSnap.data!!)
                                    batch.update(newDocRef, "id", uid)
                                    batch.delete(oldDocRef)
                                }
                                batch.commit()
                            }
                        }
                    } else {
                        // Step 3: Truly brand new user - Create a new PATIENT document
                        val newUser = User(
                            uid = uid,
                            phone = fullPhone,
                            role = "PATIENT",
                            createdAt = System.currentTimeMillis()
                        )
                        userRef.set(newUser)
                    }
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}
