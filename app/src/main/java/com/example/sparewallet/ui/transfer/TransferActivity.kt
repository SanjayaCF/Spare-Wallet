package com.example.sparewallet.ui.transfer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sparewallet.model.TransferRecipient
import com.example.sparewallet.ui.theme.SpareWalletTheme
import kotlinx.coroutines.launch

class TransferActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpareWalletTheme {
                TransferScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(transferViewModel: TransferViewModel = viewModel()) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var accountNumber by remember { mutableStateOf("") }
    val recipients by transferViewModel.recipients.collectAsState()

    LaunchedEffect(Unit) {
        transferViewModel.loadRecipients()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Transfer") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = { Text("Enter recipient account number") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (accountNumber.isNotBlank()) {
                        transferViewModel.findRecipient(accountNumber) { recipient ->
                            if (recipient != null) {
                                val intent = Intent(context, TransferDetailActivity::class.java)
                                intent.putExtra("recipient_name", recipient.name)
                                intent.putExtra("recipient_account", recipient.accountNumber)
                                context.startActivity(intent)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Recipient not found")
                                }
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter an account number")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Find User")
            }

            Text("Recent Transfers", style = MaterialTheme.typography.titleMedium)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recipients) { recipient ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(context, TransferDetailActivity::class.java)
                                intent.putExtra("recipient_name", recipient.name)
                                intent.putExtra("recipient_account", recipient.accountNumber)
                                context.startActivity(intent)
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(recipient.name, style = MaterialTheme.typography.bodyLarge)
                            Text(recipient.accountNumber, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}