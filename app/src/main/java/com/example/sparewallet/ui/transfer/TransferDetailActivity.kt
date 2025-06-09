package com.example.sparewallet.ui.transfer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sparewallet.model.TransactionRecord
import com.example.sparewallet.ui.theme.SpareWalletTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.Locale

class TransferDetailActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var currentUserRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US)

    private lateinit var recipientName: String
    private lateinit var recipientAccount: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
        val currentUid = auth.currentUser?.uid ?: ""
        currentUserRef = database.getReference("users").child(currentUid)
        usersRef = database.getReference("users")

        recipientName = intent.getStringExtra("recipientName") ?: "Unknown"
        recipientAccount = intent.getStringExtra("recipientAccount") ?: "Unknown"

        setContent {
            SpareWalletTheme {
                TransferDetailScreen(
                    recipientName = recipientName,
                    recipientAccount = recipientAccount,
                    onTransfer = { amount -> performTransfer(amount, currentUid) }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TransferDetailScreen(
        recipientName: String,
        recipientAccount: String,
        onTransfer: (Double) -> Unit
    ) {
        val context = LocalContext.current
        var currentUserBalance by remember { mutableDoubleStateOf(0.0) }
        var transferAmount by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }

        // Load balance
        LaunchedEffect(Unit) {
            currentUserRef.child("balance").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val balanceStr = snapshot.getValue(String::class.java) ?: "0"
                    currentUserBalance = balanceStr.toDoubleOrNull() ?: 0.0
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load balance", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recipient Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = recipientName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recipientAccount,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Text(
                    text = "Your Balance: Rp. ${numberFormat.format(currentUserBalance)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = transferAmount,
                onValueChange = { newValue ->
                    val cleanString = newValue.replace("[,]".toRegex(), "")
                    if (cleanString.isEmpty()) {
                        transferAmount = ""
                    } else {
                        val parsed = cleanString.toDoubleOrNull()
                        if (parsed != null) {
                            transferAmount = numberFormat.format(parsed)
                        }
                    }
                },
                label = { Text("Enter amount to transfer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val amountString = transferAmount.replace("[,]".toRegex(), "")
                    val amount = amountString.toDoubleOrNull()
                    when {
                        amount == null || amount <= 0 -> {
                            Toast.makeText(context, "Enter a valid transfer amount", Toast.LENGTH_SHORT).show()
                        }

                        amount > currentUserBalance -> {
                            Toast.makeText(context, "Insufficient balance", Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            onTransfer(amount)
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = !isLoading && transferAmount.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Transfer", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

    private fun performTransfer(transferAmount: Double, currentUid: String) {
        currentUserRef.child("balance").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentBalanceStr = currentData.getValue(String::class.java)
                val currentBalance = currentBalanceStr?.toDoubleOrNull() ?: 0.0
                if (currentBalance < transferAmount) return Transaction.abort()
                val newBalance = currentBalance - transferAmount
                currentData.value = newBalance.toString()
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    Toast.makeText(this@TransferDetailActivity, "Transfer failed: ${error.message}", Toast.LENGTH_SHORT).show()
                } else if (committed) {
                    val senderTransaction = TransactionRecord(
                        type = "Transfer Sent",
                        amount = transferAmount.toString(),
                        timestamp = System.currentTimeMillis(),
                        details = "Transferred to $recipientName ($recipientAccount)"
                    )
                    database.getReference("transactions").child(currentUid).push().setValue(senderTransaction)
                    updateRecipientBalance(transferAmount, currentUid)
                }
            }
        })
    }

    private fun updateRecipientBalance(amount: Double, currentUid: String) {
        val query = usersRef.orderByChild("accountNumber").equalTo(recipientAccount)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val recipientUid = child.key ?: continue
                        val recipientBalanceRef = usersRef.child(recipientUid).child("balance")
                        recipientBalanceRef.runTransaction(object : Transaction.Handler {
                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                val currentBalanceStr = currentData.getValue(String::class.java)
                                val currentBalance = currentBalanceStr?.toDoubleOrNull() ?: 0.0
                                val newBalance = currentBalance + amount
                                currentData.value = newBalance.toString()
                                return Transaction.success(currentData)
                            }

                            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                                if (error != null) {
                                    Toast.makeText(this@TransferDetailActivity, "Failed to update recipient balance: ${error.message}", Toast.LENGTH_SHORT).show()
                                } else if (committed) {
                                    val recipientTransaction = TransactionRecord(
                                        type = "Transfer Received",
                                        amount = amount.toString(),
                                        timestamp = System.currentTimeMillis(),
                                        details = "Received from ${auth.currentUser?.email ?: "Unknown"}"
                                    )
                                    database.getReference("transactions").child(recipientUid).push().setValue(recipientTransaction)
                                    Toast.makeText(this@TransferDetailActivity, "Transfer successful!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                        })
                        break
                    }
                } else {
                    Toast.makeText(this@TransferDetailActivity, "Recipient not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransferDetailActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
