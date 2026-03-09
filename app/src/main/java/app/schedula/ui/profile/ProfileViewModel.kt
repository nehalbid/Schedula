package app.schedula.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import app.schedula.data.model.FamilyMember
import app.schedula.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val listeners = mutableListOf<ListenerRegistration>()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    init {
        loadCurrentUser()
        loadFamilyMembers()
    }

    // -------------------------
    // USER
    // -------------------------

    private fun loadCurrentUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.d("ProfileViewModel", "loadCurrentUser: firebaseUser is null")
            return
        }
        
        val uid = firebaseUser.uid
        Log.d("ProfileViewModel", "Loading user data for UID: $uid")
        
        val userListener = firestore.collection("users")
            .document(uid)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.w("ProfileViewModel", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    Log.d("ProfileViewModel", "User document found: ${document.data}")
                    _user.value = document.toObject(User::class.java)
                } else {
                    Log.d("ProfileViewModel", "No such document for UID: $uid")
                    _user.value = null
                }
            }
        
        listeners.add(userListener)
    }

    fun updateProfile(
        name: String,
        dob: String,
        gender: String,
        bloodGroup: String,
        email: String,
        location: String,
        onResult: (Boolean) -> Unit
    ) {
        Log.d("ProfileViewModel", "updateProfile called")
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        if (uid != null) {

            val updates = mapOf(
                "name" to name,
                "dob" to dob,
                "gender" to gender,
                "bloodGroup" to bloodGroup,
                "email" to email,
                "location" to location,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(uid)
                .update(updates)
                .addOnSuccessListener {
                    Log.d("ProfileViewModel", "User profile updated successfully!")
                    onResult(true)
                }
                .addOnFailureListener { e ->
                    Log.w("ProfileViewModel", "Error updating user profile", e)
                    onResult(false)
                }
        } else {
            Log.w("ProfileViewModel", "updateProfile: User not logged in")
            onResult(false)
        }
    }

    // -------------------------
    // FAMILY MEMBERS
    // -------------------------

    private fun loadFamilyMembers() {
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        if (uid != null) {
            val familyListener = firestore.collection("users")
                .document(uid)
                .collection("familyMembers")
                .addSnapshotListener { snapshot, _ ->

                    if (snapshot != null) {
                        val members = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(FamilyMember::class.java)
                                ?.copy(id = doc.id)
                        }
                        _familyMembers.value = members
                    }
                }
            
            listeners.add(familyListener)
        }
    }

    fun addFamilyMember(name: String, relation: String, age: Int, gender: String) {
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        if (uid != null) {

            val newMemberRef = firestore.collection("users")
                .document(uid)
                .collection("familyMembers")
                .document()

            val member = FamilyMember(
                id = newMemberRef.id,
                name = name,
                relation = relation,
                age = age,
                gender = gender
            )

            newMemberRef.set(member)
        }
    }

    fun deleteFamilyMember(memberId: String) {
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        if (uid != null) {
            firestore.collection("users")
                .document(uid)
                .collection("familyMembers")
                .document(memberId)
                .delete()
        }
    }

    fun updateFamilyMember(
        memberId: String,
        name: String,
        relation: String,
        age: Int,
        gender: String
    ) {
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        if (uid != null) {

            val updatedMember = mapOf(
                "name" to name,
                "relation" to relation,
                "age" to age,
                "gender" to gender
            )

            firestore.collection("users")
                .document(uid)
                .collection("familyMembers")
                .document(memberId)
                .update(updatedMember)
        }
    }

    fun updateAvatar(avatarIcon: String) {

        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        if (uid != null) {

            firestore.collection("users")
                .document(uid)
                .update("avatarIcon", avatarIcon)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}
