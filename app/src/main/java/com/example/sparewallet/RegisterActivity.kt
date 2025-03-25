package com.example.sparewallet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sparewallet.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.registerButton.setOnClickListener {
            binding.registerButton.isEnabled = false
            val email = binding.registerEmailEditText.text.toString().trim()
            val password = binding.registerPasswordEditText.text.toString().trim()
            val name = binding.registerNameEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
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

                            userRef.setValue(userData)
                                .addOnCompleteListener { dataTask ->
                                    if (dataTask.isSuccessful) {
                                        startActivity(Intent(this, SetupPinActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Failed to save user data: ${dataTask.exception?.message}", Toast.LENGTH_LONG).show()
                                        binding.registerButton.isEnabled = true
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            binding.registerButton.isEnabled = true
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                binding.registerButton.isEnabled = true
            }
        }

        binding.goToLoginTextView.setOnClickListener {
            binding.goToLoginTextView.isEnabled = false
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun generateAccountNumber(): String {
        val min = 1000000000L
        val max = 9999999999L
        val randomNumber = (min..max).random()
        return randomNumber.toString()
    }
}
