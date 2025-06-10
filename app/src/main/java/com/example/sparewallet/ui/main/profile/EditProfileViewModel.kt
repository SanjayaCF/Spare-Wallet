package com.example.sparewallet.ui.main.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {

    companion object {
        private const val DATABASE_URL = "https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app"
    }

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    throw Exception("User not logged in")
                }

                val uid = currentUser.uid
                val snapshot = FirebaseDatabase.getInstance(DATABASE_URL)
                    .reference
                    .child("users")
                    .child(uid)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    _name.value = snapshot.child("name").getValue(String::class.java) ?: ""
                    _email.value = snapshot.child("email").getValue(String::class.java) ?: currentUser.email ?: ""
                    _phone.value = snapshot.child("phone").getValue(String::class.java) ?: ""
                } else {
                    // Jika data user belum ada, gunakan data dari FirebaseAuth
                    _name.value = currentUser.displayName ?: ""
                    _email.value = currentUser.email ?: ""
                    _phone.value = ""
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load user data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onNameChange(newName: String) {
        _name.value = newName
        _errorMessage.value = null // Clear error when user starts typing
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _errorMessage.value = null
    }

    fun onPhoneChange(newPhone: String) {
        _phone.value = newPhone
        _errorMessage.value = null
    }

    fun updateUserData(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    throw Exception("User not logged in")
                }

                val uid = currentUser.uid
                val userRef = FirebaseDatabase.getInstance(DATABASE_URL)
                    .reference
                    .child("users")
                    .child(uid)

                val userData = mapOf(
                    "name" to _name.value,
                    "email" to _email.value,
                    "phone" to _phone.value
                )

                userRef.updateChildren(userData).await()
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Failed to update profile"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun validateInput(): String? {
        return when {
            _name.value.isBlank() -> "Name cannot be empty"
            _email.value.isBlank() -> "Email cannot be empty"
//            _phone.value.isBlank() -> "Phone cannot be empty"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches() ->
                "Please enter a valid email address"
            else -> null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}