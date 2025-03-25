package com.example.sparewallet

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sparewallet.databinding.ActivityTransferBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TransferActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var savedRecipientsRef: DatabaseReference

    private lateinit var adapter: TransferRecipientAdapter
    private val recipientList = mutableListOf<TransferRecipient>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
        usersRef = database.getReference("users")
        val currentUid = auth.currentUser?.uid ?: ""
        savedRecipientsRef = database.getReference("transferRecipients").child(currentUid)

        adapter = TransferRecipientAdapter(recipientList) { recipient ->
            val intent = Intent(this, TransferDetailActivity::class.java)
            intent.putExtra("recipientName", recipient.name)
            intent.putExtra("recipientAccount", recipient.accountNumber)
            startActivity(intent)
        }
        binding.recyclerRecipients.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecipients.adapter = adapter

        loadSavedRecipients()

        binding.findRecipientButton.setOnClickListener {
            val enteredAccount = binding.editAccountNumber.text.toString().trim()
            if (TextUtils.isEmpty(enteredAccount)) {
                Toast.makeText(this, "Please enter an account number", Toast.LENGTH_SHORT).show()
            } else {
                findAndSaveRecipient(enteredAccount)
            }
        }
    }

    private fun findAndSaveRecipient(accountNumber: String) {
        val query = usersRef.orderByChild("accountNumber").equalTo(accountNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val name = child.child("name").getValue(String::class.java) ?: "Unknown"
                        val acct = child.child("accountNumber").getValue(String::class.java) ?: accountNumber

                        val recipient = TransferRecipient(name, acct)
                        savedRecipientsRef.child(acct).setValue(recipient)
                        Toast.makeText(this@TransferActivity, "Recipient found: $name", Toast.LENGTH_SHORT).show()
                        break
                    }
                } else {
                    Toast.makeText(this@TransferActivity, "No user found with that account number", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransferActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadSavedRecipients() {
        savedRecipientsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recipientList.clear()
                for (child in snapshot.children) {
                    val recipient = child.getValue(TransferRecipient::class.java)
                    if (recipient != null) {
                        recipientList.add(recipient)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransferActivity, "Failed to load recipients: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
