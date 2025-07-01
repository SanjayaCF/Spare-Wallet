package com.example.sparewallet.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sparewallet.model.TransactionRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransferDetailViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val usersRef = database.getReference("users")
    private val currentUserUid = auth.currentUser?.uid ?: ""

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance

    init {
        fetchCurrentUserBalance()
    }

    private fun fetchCurrentUserBalance() {
        if (currentUserUid.isEmpty()) return

        usersRef.child(currentUserUid).child("balance").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _balance.value = snapshot.getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error fetching balance, maybe log it
            }
        })
    }

    fun performTransfer(
        recipientAccount: String,
        recipientName: String,
        amount: Double,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        if (currentUserUid.isEmpty()) {
            onResult(false, "User not logged in.")
            return
        }

        val senderRef = usersRef.child(currentUserUid).child("balance")

        // 1. Jalankan transaksi untuk mengurangi saldo pengirim
        senderRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentBalance = currentData.getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
                if (currentBalance < amount) {
                    return Transaction.abort() // Batalkan jika saldo tidak cukup
                }
                val newBalance = currentBalance - amount
                currentData.value = newBalance.toString()
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    onResult(false, "Transfer failed: ${error.message}")
                } else if (committed) {
                    // Jika saldo pengirim berhasil dikurangi, lanjutkan ke penerima
                    recordSenderTransaction(recipientName, recipientAccount, amount)
                    updateRecipientBalance(recipientAccount, recipientName, amount, onResult)
                } else {
                    onResult(false, "Insufficient balance.")
                }
            }
        })
    }

    private fun updateRecipientBalance(
        recipientAccount: String,
        recipientName: String,
        amount: Double,
        onResult: (Boolean, String) -> Unit
    ) {
        val query = usersRef.orderByChild("accountNumber").equalTo(recipientAccount)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val recipientSnapshot = snapshot.children.first()
                    val recipientUid = recipientSnapshot.key ?: ""
                    val recipientBalanceRef = usersRef.child(recipientUid).child("balance")

                    // 2. Jalankan transaksi untuk menambah saldo penerima
                    recipientBalanceRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val currentBalance = currentData.getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
                            val newBalance = currentBalance + amount
                            currentData.value = newBalance.toString()
                            return Transaction.success(currentData)
                        }

                        override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                            if (committed) {
                                recordRecipientTransaction(recipientUid, amount)
                                onResult(true, "Transfer successful!")
                            } else {
                                onResult(false, "Failed to update recipient's balance.")
                                // TODO: Handle refund logic if recipient update fails
                            }
                        }
                    })
                } else {
                    onResult(false, "Recipient account not found.")
                    // TODO: Handle refund logic if recipient not found
                }
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(false, "Database error: ${error.message}")
            }
        })
    }

    private fun recordSenderTransaction(recipientName: String, recipientAccount: String, amount: Double) {
        val senderTransaction = TransactionRecord(
            type = "Transfer Sent",
            amount = amount.toString(),
            timestamp = System.currentTimeMillis(),
            details = "To: $recipientName ($recipientAccount)"
        )
        database.getReference("transactions").child(currentUserUid).push().setValue(senderTransaction)
    }

    private fun recordRecipientTransaction(recipientUid: String, amount: Double) {
        val senderName = auth.currentUser?.displayName ?: "Unknown Sender"
        val recipientTransaction = TransactionRecord(
            type = "Transfer Received",
            amount = amount.toString(),
            timestamp = System.currentTimeMillis(),
            details = "From: $senderName"
        )
        database.getReference("transactions").child(recipientUid).push().setValue(recipientTransaction)
    }
}