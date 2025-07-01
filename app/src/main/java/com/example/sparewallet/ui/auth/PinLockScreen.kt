package com.example.sparewallet.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Tidak ada perubahan di file ini, hanya untuk menunjukkan bagaimana keypad baru akan terlihat
@Composable
fun PinLockScreen(
    savedPin: String,
    onSuccess: () -> Unit,
    onBiometricRequested: (() -> Unit)?
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    val gradientColors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))

    LaunchedEffect(pin) {
        if (pin.length == 6) {
            if (pin == savedPin) {
                onSuccess()
            } else {
                Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                pin = ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            shape = MaterialTheme.shapes.large,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Enter your PIN",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    PinDisplay(pinLength = pin.length)
                }

                // Penggunaan PinKeypad yang sudah direvisi
                PinKeypad(
                    onNumberClick = { number ->
                        if (pin.length < 6) {
                            pin += number
                        }
                    },
                    onBackspaceClick = {
                        if (pin.isNotEmpty()) {
                            pin = pin.dropLast(1)
                        }
                    },
                    onBiometricClick = onBiometricRequested
                )
            }
        }
    }
}