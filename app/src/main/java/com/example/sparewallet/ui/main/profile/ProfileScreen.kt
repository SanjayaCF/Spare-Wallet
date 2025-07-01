package com.example.sparewallet.ui.main.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.sparewallet.R
import com.example.sparewallet.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPreferences = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("User Name") }
    var email by remember { mutableStateOf("No Email") }
    var phone by remember { mutableStateOf("No Phone Number") }
    var isLoading by remember { mutableStateOf(false) }

    var refreshKey by remember { mutableStateOf(0) }

    val editProfileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        refreshKey++
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            saveUserProfileLocal(sharedPreferences, it.toString())
        }
    }

    fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
            .child(uid)

        isLoading = true

        userRef.child("name").get().addOnSuccessListener {
            name = it.getValue(String::class.java) ?: "User Name"
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load name", Toast.LENGTH_SHORT).show()
        }

        userRef.child("email").get().addOnSuccessListener {
            email = it.getValue(String::class.java) ?: "No Email"
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load email", Toast.LENGTH_SHORT).show()
        }

        userRef.child("phone").get().addOnSuccessListener {
            phone = it.getValue(String::class.java) ?: "No Phone Number"
            isLoading = false
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load phone", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    LaunchedEffect(refreshKey) {
        loadUserProfileLocal(sharedPreferences)?.let {
            imageUri = Uri.parse(it)
        }

        loadUserData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = imageUri ?: R.drawable.ic_user),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Text(text = name, style = MaterialTheme.typography.titleLarge)
            Text(text = phone, style = MaterialTheme.typography.bodyLarge)
            Text(text = email, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val intent = Intent(context, EditProfileActivity::class.java)
                editProfileLauncher.launch(intent)
            },
            enabled = !isLoading
        ) {
            Text("Edit Profile")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                context.startActivity(Intent(context, ChangePinActivity::class.java))
            },
            enabled = !isLoading
        ) {
            Text("Edit PIN")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                context.startActivity(Intent(context, LoginActivity::class.java))
                (context as? android.app.Activity)?.finish()
            },
            enabled = !isLoading
        ) {
            Text("Log Out")
        }
    }
}

private fun saveUserProfileLocal(sharedPreferences: SharedPreferences, imagePath: String) {
    sharedPreferences.edit().apply {
        putString("profileImage", imagePath)
        apply()
    }
}

private fun loadUserProfileLocal(sharedPreferences: SharedPreferences): String? {
    return sharedPreferences.getString("profileImage", null)
}