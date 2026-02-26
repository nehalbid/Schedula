package app.schedula.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.schedula.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null && firebaseUser.phoneNumber != null) {
                firestore.collection("users").document(firebaseUser.phoneNumber!!).get()
                    .addOnSuccessListener { document ->
                        _user.value = document.toObject(User::class.java)
                    }
            }
        }
    }

    fun updateProfile(name: String, email: String, location: String) {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null && firebaseUser.phoneNumber != null) {
                val updatedUser = User(
                    uid = firebaseUser.uid,
                    name = name,
                    email = email,
                    location = location,
                    phone = firebaseUser.phoneNumber!!
                )
                firestore.collection("users").document(firebaseUser.phoneNumber!!).set(updatedUser)
                    .addOnSuccessListener {
                        _user.value = updatedUser
                    }
            }
        }
    }
}