package com.example.sparewallet.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinLockScreen(
    savedPin: String,
    onSuccess: () -> Unit,
    onBiometricRequested: (() -> Unit)?
) {
    var pin by remember { mutableStateOf("") }
    val gradientColors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pin) {
        if (pin.length == 6) {
            if (pin == savedPin) {
                onSuccess()
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Incorrect PIN")
                }
                pin = ""
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
}