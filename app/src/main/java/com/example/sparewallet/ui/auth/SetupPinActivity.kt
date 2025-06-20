package com.example.sparewallet.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.sparewallet.ui.main.MainActivity
import com.example.sparewallet.ui.theme.SpareWalletTheme

class SetupPinActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpareWalletTheme {
                SetupPinScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPinScreen() {
    val context = LocalContext.current

    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    val gradientColors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 150.dp),
            shape = MaterialTheme.shapes.large,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) newPin = it },
                    label = { Text("Enter new 6-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        when {
                            newPin.length != 6 || confirmPin.length != 6 -> {
                                Toast.makeText(context, "Both PINs must be 6 digits", Toast.LENGTH_SHORT).show()
                            }
                            newPin != confirmPin -> {
                                Toast.makeText(context, "PINs do not match", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                val sharedPref = context.getSharedPreferences(
                                    "SpareWalletPrefs",
                                    Context.MODE_PRIVATE
                                )
                                sharedPref.edit()
                                    .putString("user_pin", newPin)
                                    .apply()

                                Toast.makeText(
                                    context,
                                    "PIN set successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Navigate to main screen or finish setup
                                context.startActivity(
                                    Intent(context, MainActivity::class.java)
                                )
                                (context as? ComponentActivity)?.finish()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Set PIN")
                }
            }
        }
    }
}
