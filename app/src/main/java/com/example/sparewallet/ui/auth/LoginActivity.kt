package com.example.sparewallet.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sparewallet.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


//        if (auth.currentUser != null) {
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        }

        binding.loginButton.setOnClickListener {
            binding.loginButton.isEnabled = false
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, SetupPinActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Login failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.loginButton.isEnabled = true
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                binding.loginButton.isEnabled = true
            }
        }

        binding.goToRegisterTextView.setOnClickListener {
            binding.goToRegisterTextView.isEnabled = false
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
