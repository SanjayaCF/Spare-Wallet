package com.example.sparewallet.ui.main.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sparewallet.databinding.ActivityTopUpBinding
import com.example.sparewallet.model.TransactionRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Transaction
import java.text.NumberFormat
import java.util.Locale

class TopUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US)

    private var currentText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
        val uid = auth.currentUser?.uid ?: ""

        binding.editTopUpAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                if (s == null || s.toString() == currentText) return
                binding.editTopUpAmount.removeTextChangedListener(this)
                val cleanString = s.toString().replace("[,]".toRegex(), "")
                if (cleanString.isNotEmpty()) {
                    val parsed = cleanString.toDoubleOrNull() ?: 0.0
                    val formatted = numberFormat.format(parsed)
                    currentText = formatted
                    binding.editTopUpAmount.setText(formatted)
                    binding.editTopUpAmount.setSelection(formatted.length)
                }
                binding.editTopUpAmount.addTextChangedListener(this)
            }
        })

        binding.topUpButton.setOnClickListener {
            val amountString = binding.editTopUpAmount.text.toString().replace("[,]".toRegex(), "")
            val topUpAmount = amountString.toDoubleOrNull()
            if (topUpAmount == null || topUpAmount <= 0) {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val balanceRef = database.getReference("users").child(uid).child("balance")
            balanceRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData): Transaction.Result {
                    val currentBalanceStr = currentData.getValue(String::class.java)
                    val currentBalance = currentBalanceStr?.toDoubleOrNull() ?: 0.0
                    val newBalance = currentBalance + topUpAmount
                    currentData.value = newBalance.toString()
                    return com.google.firebase.database.Transaction.success(currentData)
                }
                override fun onComplete(error: com.google.firebase.database.DatabaseError?, committed: Boolean, currentData: com.google.firebase.database.DataSnapshot?) {
                    if (error != null) {
                        Toast.makeText(this@TopUpActivity, "Top up failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else if (committed) {
                        val transactionRecord = TransactionRecord(
                            type = "Top Up",
                            amount = topUpAmount.toString(),
                            timestamp = System.currentTimeMillis(),
                            details = "Wallet topped up"
                        )
                        database.getReference("transactions").child(uid).push().setValue(transactionRecord)
                        Toast.makeText(this@TopUpActivity, "Top up successful!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            })
        }
    }
}
