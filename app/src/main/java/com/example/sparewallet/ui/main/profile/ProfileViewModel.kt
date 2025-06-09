package com.example.sparewallet.ui.main.profile

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

    private val _name = MutableStateFlow("User Name")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("No Email")
    val email: StateFlow<String> = _email

    private val _phone = MutableStateFlow("No Phone Number")
    val phone: StateFlow<String> = _phone

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri

    init {
        loadUserProfile()
    }

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
        saveUserProfileLocal(uri.toString())
    }

    private fun saveUserProfileLocal(imagePath: String) {
        sharedPreferences.edit().putString("profileImage", imagePath).apply()
    }

    private fun loadUserProfile() {
        sharedPreferences.getString("profileImage", null)?.let {
            _imageUri.value = Uri.parse(it)
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users").child(uid)

        userRef.child("name").get().addOnSuccessListener {
            _name.value = it.getValue(String::class.java) ?: _name.value
        }

        userRef.child("email").get().addOnSuccessListener {
            _email.value = it.getValue(String::class.java) ?: _email.value
        }

        userRef.child("phone").get().addOnSuccessListener {
            _phone.value = it.getValue(String::class.java) ?: _phone.value
        }
    }
}
