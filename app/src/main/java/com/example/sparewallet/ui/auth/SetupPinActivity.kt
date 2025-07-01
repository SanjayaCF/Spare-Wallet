package com.example.sparewallet.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sparewallet.ui.main.MainActivity
import com.example.sparewallet.ui.theme.SpareWalletTheme
import kotlinx.coroutines.launch

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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }

    val gradientColors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                Crossfade(targetState = isConfirming, label = "PinSetupCrossfade") { confirming ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (!confirming) {
                            // Stage 1: Enter New PIN
                            EnterPinStage(
                                title = "Create a New PIN",
                                pin = newPin,
                                onPinChange = { newPin = it },
                                onPinComplete = { isConfirming = true }
                            )
                        } else {
                            // Stage 2: Confirm PIN
                            EnterPinStage(
                                title = "Confirm Your PIN",
                                pin = confirmPin,
                                onPinChange = { confirmPin = it },
                                onPinComplete = {
                                    scope.launch {
                                        if (newPin == confirmPin) {
                                            val sharedPref = context.getSharedPreferences("SpareWalletPrefs", Context.MODE_PRIVATE)
                                            sharedPref.edit().putString("user_pin", newPin).apply()
                                            snackbarHostState.showSnackbar("PIN set successfully!")
                                            context.startActivity(Intent(context, MainActivity::class.java))
                                            (context as? ComponentActivity)?.finish()
                                        } else {
                                            snackbarHostState.showSnackbar("PINs do not match. Please try again.")
                                            newPin = ""
                                            confirmPin = ""
                                            isConfirming = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnterPinStage(
    title: String,
    pin: String,
    onPinChange: (String) -> Unit,
    onPinComplete: () -> Unit
) {
    LaunchedEffect(pin) {
        if (pin.length == 6) {
            onPinComplete()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(32.dp))
            PinDisplay(pinLength = pin.length)
        }
        PinKeypad(
            onNumberClick = { number ->
                if (pin.length < 6) {
                    onPinChange(pin + number)
                }
            },
            onBackspaceClick = {
                if (pin.isNotEmpty()) {
                    onPinChange(pin.dropLast(1))
                }
            }
        )
    }
}