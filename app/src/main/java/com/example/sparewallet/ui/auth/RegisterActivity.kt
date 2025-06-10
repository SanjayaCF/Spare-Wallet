package com.example.sparewallet.ui.auth

import RegisterScreen
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.sparewallet.R
import com.example.sparewallet.ui.main.MainActivity
import com.example.sparewallet.ui.theme.SpareWalletTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class RegisterActivity : FragmentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            SpareWalletTheme {
                RegisterScreen(
                    onRegister = { name, email, password, onResult ->
                        registerUser(name, email, password, onResult)
                    },
                    onGoToLogin = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    private fun registerUser(
        name: String,
        email: String,
        password: String,
        onResult: (success: Boolean, message: String?) -> Unit
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            onResult(false, "Please fill in all fields")
            return
        }
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: ""
                val database = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
                val userRef = database.getReference("users").child(uid)
                val accountNumber = generateAccountNumber()
                val userData = mapOf(
                    "name" to name,
                    "balance" to "0",
                    "accountNumber" to accountNumber
                )
                userRef.setValue(userData).addOnCompleteListener { dataTask ->
                    if (dataTask.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, "Failed to save user data: ${dataTask.exception?.message}")
                    }
                }
            } else {
                onResult(false, "Registration failed: ${task.exception?.message}")
            }
        }
    }

    private fun generateAccountNumber(): String {
        val min = 1000000000L
        val max = 9999999999L
        val randomNumber = Random.nextLong(min, max)
        return randomNumber.toString()
    }
}
