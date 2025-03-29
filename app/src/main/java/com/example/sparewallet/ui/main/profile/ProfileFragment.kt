package com.example.sparewallet.ui.main.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.sparewallet.databinding.FragmentProfileBinding
import com.example.sparewallet.ui.auth.ChangePinActivity
import com.example.sparewallet.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private lateinit var sharedPreferences: SharedPreferences

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                binding.profilePicture.setImageURI(uri)
                saveUserProfileLocal(uri.toString())
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        loadUserProfileFirebase()
        loadUserProfileLocal()

        binding.profilePicture.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.editPinButton.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePinActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadUserProfileFirebase()
        loadUserProfileLocal()
    }

    private fun saveUserProfileLocal(imagePath: String) {
        sharedPreferences.edit().apply {
            putString("profileImage", imagePath)
            apply()
        }
    }

    private fun loadUserProfileLocal() {
        val imagePath = sharedPreferences.getString("profileImage", null)
        if (!imagePath.isNullOrEmpty()) {
            Picasso.get().load(imagePath).into(binding.profilePicture)
        }
    }

    private fun loadUserProfileFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
            .child(uid)

        userRef.child("name").get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java) ?: "User Name"
                binding.profileName.text = name
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load name", Toast.LENGTH_SHORT).show()
            }

        userRef.child("email").get()
            .addOnSuccessListener { snapshot ->
                val email = snapshot.getValue(String::class.java) ?: "No Email"
                binding.profileEmail.text = email
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load email", Toast.LENGTH_SHORT).show()
            }

        userRef.child("phone").get()
            .addOnSuccessListener { snapshot ->
                val phone = snapshot.getValue(String::class.java) ?: "No Phone Number"
                binding.profilePhone.text = phone
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load phone", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
