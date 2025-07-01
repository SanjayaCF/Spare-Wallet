package com.example.sparewallet.ui.main.profile

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sparewallet.ui.auth.PinDisplay
import com.example.sparewallet.ui.auth.PinKeypad
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private enum class ChangePinStage {
    OLD, NEW, CONFIRM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePinScreen(
    viewModel: EditProfileViewModel,
    onPinChangedSuccessfully: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var stage by remember { mutableStateOf(ChangePinStage.OLD) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Change PIN") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Crossfade(targetState = stage, label = "ChangePinCrossfade") { currentStage ->
                when (currentStage) {
                    ChangePinStage.OLD -> EditPinStage(
                        title = "Enter Your Old PIN",
                        pin = oldPin,
                        onPinChange = { oldPin = it },
                        onPinComplete = {
                            val sharedPref = context.getSharedPreferences("SpareWalletPrefs", Context.MODE_PRIVATE)
                            val savedPin = sharedPref.getString("user_pin", null)
                            if (oldPin == savedPin) {
                                stage = ChangePinStage.NEW
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Old PIN is incorrect") }
                                oldPin = ""
                            }
                        }
                    )
                    ChangePinStage.NEW -> EditPinStage(
                        title = "Enter Your New PIN",
                        pin = newPin,
                        onPinChange = { newPin = it },
                        onPinComplete = { stage = ChangePinStage.CONFIRM }
                    )
                    ChangePinStage.CONFIRM -> EditPinStage(
                        title = "Confirm Your New PIN",
                        pin = confirmPin,
                        onPinChange = { confirmPin = it },
                        onPinComplete = {
                            if (newPin == confirmPin) {
                                val sharedPref = context.getSharedPreferences("SpareWalletPrefs", Context.MODE_PRIVATE)
                                sharedPref.edit().putString("user_pin", newPin).apply()
                                scope.launch {
                                    snackbarHostState.showSnackbar("PIN updated successfully!")
                                    delay(500) // Beri jeda agar Snackbar terbaca
                                    onPinChangedSuccessfully() // Panggil callback untuk navigasi
                                }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("PINs do not match, please start over") }
                                newPin = ""
                                confirmPin = ""
                                stage = ChangePinStage.NEW
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditPinStage(
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