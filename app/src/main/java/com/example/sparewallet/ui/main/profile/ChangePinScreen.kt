package com.example.sparewallet.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePinScreen(
    onPinUpdated: () -> Unit // dipanggil ketika PIN berhasil diubah
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("SpareWalletPrefs", Context.MODE_PRIVATE)
    val savedPin = sharedPref.getString("user_pin", null)

    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Change PIN",
            fontSize = 22.sp,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        OutlinedTextField(
            value = oldPin,
            onValueChange = { oldPin = it },
            label = { Text("Old PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newPin,
            onValueChange = { newPin = it },
            label = { Text("New PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmNewPin,
            onValueChange = { confirmNewPin = it },
            label = { Text("Confirm New PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    savedPin == null -> {
                        Toast.makeText(context, "No existing PIN found!", Toast.LENGTH_SHORT).show()
                    }

                    oldPin != savedPin -> {
                        Toast.makeText(context, "Old PIN is incorrect", Toast.LENGTH_SHORT).show()
                    }

                    newPin.length != 6 -> {
                        Toast.makeText(context, "New PIN must be 6 digits", Toast.LENGTH_SHORT).show()
                    }

                    newPin != confirmNewPin -> {
                        Toast.makeText(context, "New PINs do not match", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        with(sharedPref.edit()) {
                            putString("user_pin", newPin)
                            apply()
                        }
                        Toast.makeText(context, "PIN updated successfully!", Toast.LENGTH_SHORT).show()
                        onPinUpdated()
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Save PIN")
        }
    }
}
