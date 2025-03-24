package com.example.sparewallet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sparewallet.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

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

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, SetupPinActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Registration failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.registerButton.isEnabled = true
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                binding.registerButton.isEnabled = true
            }
        }

        binding.goToLoginTextView.setOnClickListener {
            binding.goToLoginTextView.isEnabled = false
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
