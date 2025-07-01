package com.example.sparewallet.ui.main.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.sparewallet.model.TransactionRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Transaction
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class TopUpViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance(
        "https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app"
    )
    private val uid: String = auth.currentUser?.uid.orEmpty()

    var amountText by mutableStateOf(TextFieldValue(""))
        private set

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US)

    fun onAmountChange(newValue: TextFieldValue) {
        if (newValue.text == amountText.text) return
        val clean = newValue.text.replace(",", "")
        if (clean.isEmpty()) {
            amountText = TextFieldValue("", TextRange(0))
            return
        }
        val parsed = clean.toDoubleOrNull() ?: return
        val formatted = numberFormat.format(parsed)
        amountText = TextFieldValue(
            text = formatted,
            selection = TextRange(formatted.length)
        )
    }

    fun performTopUp(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val clean = amountText.text.replace(",", "")
        val topUpAmount = clean.toDoubleOrNull()
        if (topUpAmount == null || topUpAmount <= 0) {
            onError("Enter a valid amount")
            return
        }

        val balanceRef = database.getReference("users").child(uid).child("balance")
        balanceRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData)
                    : Transaction.Result {
                val currentBalance = currentData.getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
                val newBalance = currentBalance + topUpAmount
                currentData.value = newBalance.toString()
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    onError(error.message ?: "Top up failed")
                } else if (committed) {
                    val recordRef = database.getReference("transactions").child(uid).push()
                    val transactionRecord = TransactionRecord(
                        type = "Top Up",
                        amount = topUpAmount.toString(),
                        timestamp = System.currentTimeMillis(),
                        details = "Wallet topped up"
                    )
                    recordRef.setValue(transactionRecord)
                    onSuccess()
                }
            }
        })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(
    viewModel: TopUpViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onFinished: () -> Unit
) {
    val amountState by remember { derivedStateOf { viewModel.amountText } }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Top Up Your Wallet") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = amountState,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Enter amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.performTopUp(
                        onSuccess = { onFinished() },
                        onError = { msg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Top Up")
            }
        }
    }
}