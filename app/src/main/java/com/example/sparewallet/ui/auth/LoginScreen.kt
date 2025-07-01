package com.example.sparewallet.ui.auth

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Tambahkan SnackbarHostState dan CoroutineScope
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Bungkus dengan Scaffold
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Gunakan padding dari Scaffold
        Box(modifier = Modifier.padding(paddingValues)) {
            AuthScreenLayout(screenTitle = "Welcome Back!") {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        context.startActivity(Intent(context, PinLockActivity::class.java))
                                        (context as? android.app.Activity)?.finish()
                                    } else {
                                        // Ganti Toast dengan Snackbar
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Login failed: ${task.exception?.message ?: "Unknown error"}"
                                            )
                                        }
                                    }
                                }
                        } else {
                            isLoading = false
                            // Ganti Toast dengan Snackbar
                            scope.launch {
                                snackbarHostState.showSnackbar("Please enter email and password")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("LOGIN")
                    }
                }

                TextButton(
                    onClick = {
                        context.startActivity(Intent(context, RegisterActivity::class.java))
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Don't have an account? Register")
                }
            }
        }
    }
}