package com.example.sparewallet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sparewallet.databinding.ActivityTransferDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.Locale

class TransferDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var currentUserRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US)

    private var currentUserBalance: Double = 0.0
    private var currentAmountText = ""

    private lateinit var recipientName: String
    private lateinit var recipientAccount: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
        val currentUid = auth.currentUser?.uid ?: ""
        currentUserRef = database.getReference("users").child(currentUid)
        usersRef = database.getReference("users")

        recipientName = intent.getStringExtra("recipientName") ?: "Unknown"
        recipientAccount = intent.getStringExtra("recipientAccount") ?: "Unknown"

        binding.cardRecipientName.text = recipientName
        binding.cardRecipientAccount.text = recipientAccount

        currentUserRef.child("balance").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balanceStr = snapshot.getValue(String::class.java) ?: "0"
                currentUserBalance = balanceStr.toDoubleOrNull() ?: 0.0
                binding.yourBalance.text = "Your Balance: Rp. ${numberFormat.format(currentUserBalance)}"
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransferDetailActivity, "Failed to load balance", Toast.LENGTH_SHORT).show()
            }
        })

        binding.editTransferAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s == null || s.toString() == currentAmountText) return
                binding.editTransferAmount.removeTextChangedListener(this)
                val cleanString = s.toString().replace("[,]".toRegex(), "")
                if (cleanString.isNotEmpty()) {
                    val parsed = cleanString.toDoubleOrNull() ?: 0.0
                    val formatted = numberFormat.format(parsed)
                    currentAmountText = formatted
                    binding.editTransferAmount.setText(formatted)
                    binding.editTransferAmount.setSelection(formatted.length)
                }
                binding.editTransferAmount.addTextChangedListener(this)
            }
        })

        binding.transferButton.setOnClickListener {
            val amountString = binding.editTransferAmount.text.toString().replace("[,]".toRegex(), "")
            val transferAmount = amountString.toDoubleOrNull()
            if (transferAmount == null || transferAmount <= 0) {
                Toast.makeText(this, "Enter a valid transfer amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (transferAmount > currentUserBalance) {
                Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
    }

    private fun updateRecipientBalance(amount: Double, currentUid: String) {
        // Query for the recipient in the "users" node by account number
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
                                    // Save recipient transaction record
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
