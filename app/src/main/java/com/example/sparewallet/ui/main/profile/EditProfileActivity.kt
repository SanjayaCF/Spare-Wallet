package com.example.sparewallet.ui.main.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sparewallet.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val userRef = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
            .child(uid)

        // Load existing data
        userRef.child("name").get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java) ?: ""
                binding.editName.setText(name)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load name", Toast.LENGTH_SHORT).show()
            }

        userRef.child("email").get()
            .addOnSuccessListener { snapshot ->
                val email = snapshot.getValue(String::class.java) ?: ""
                binding.editEmail.setText(email)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load email", Toast.LENGTH_SHORT).show()
            }

        userRef.child("phone").get()
            .addOnSuccessListener { snapshot ->
                val phone = snapshot.getValue(String::class.java) ?: ""
                binding.editPhone.setText(phone)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load phone", Toast.LENGTH_SHORT).show()
            }

        binding.saveProfileButton.setOnClickListener {
            val newName = binding.editName.text.toString().trim()
            val newEmail = binding.editEmail.text.toString().trim()
            val newPhone = binding.editPhone.text.toString().trim()

            if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = mapOf(
                "name" to newName,
                "email" to newEmail,
                "phone" to newPhone
            )

            userRef.updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
