package com.example.sparewallet.ui.transfer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sparewallet.ui.theme.SpareWalletTheme
import kotlinx.coroutines.launch

class TransferDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recipientName = intent.getStringExtra("recipient_name") ?: "N/A"
        val recipientAccount = intent.getStringExtra("recipient_account") ?: "N/A"

        setContent {
            SpareWalletTheme {
                TransferDetailScreen(
                    recipientName = recipientName,
                    recipientAccount = recipientAccount
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferDetailScreen(
    recipientName: String,
    recipientAccount: String,
    viewModel: TransferDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val amount by remember { derivedStateOf { viewModel.amount } }
    val balance by viewModel.balance.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Transfer to $recipientName") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Recipient: $recipientName", style = MaterialTheme.typography.titleMedium)
            Text("Account: $recipientAccount", style = MaterialTheme.typography.bodyLarge)
            Text("Your Balance: Rp. ${"%,.0f".format(balance)}", style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.onAmountChange(it) },
                label = { Text("Amount to Transfer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val cleanAmountString = amount.text.replace(",", "")
                    val transferAmount = cleanAmountString.toDoubleOrNull()

                    scope.launch {
                        when {
                            transferAmount == null || transferAmount <= 0 -> {
                                snackbarHostState.showSnackbar("Please enter a valid amount")
                            }
                            transferAmount > balance -> {
                                snackbarHostState.showSnackbar("Insufficient balance")
                            }
                            else -> {
                                viewModel.performTransfer(
                                    recipientAccount = recipientAccount,
                                    recipientName = recipientName,
                                    // PERBAIKAN: Mengganti nama parameter dari 'amount' menjadi 'transferAmount'
                                    transferAmount = transferAmount,
                                    onResult = { success, message ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message)
                                            if (success) {
                                                (context as? ComponentActivity)?.finish()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Transfer")
            }
        }
    }
}