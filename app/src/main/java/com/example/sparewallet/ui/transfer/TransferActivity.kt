package com.example.sparewallet.ui.transfer

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sparewallet.model.TransferRecipient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TransferActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var savedRecipientsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
        usersRef = database.getReference("users")
        val currentUid = auth.currentUser?.uid ?: ""
        savedRecipientsRef = database.getReference("transferRecipients").child(currentUid)

        setContent {
            com.example.sparewallet.ui.theme.SpareWalletTheme {
                TransferScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TransferScreen() {
        var accountNumber by remember { mutableStateOf("") }
        var recipientList by remember { mutableStateOf(listOf<TransferRecipient>()) }
        val context = LocalContext.current

        // Load saved recipients
        LaunchedEffect(Unit) {
            loadSavedRecipients { recipients ->
                recipientList = recipients
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Number Input Section
            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = { Text("Enter user account number") },
                placeholder = { Text("Enter user account number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Find User Button
            Button(
                onClick = {
                    if (TextUtils.isEmpty(accountNumber.trim())) {
                        Toast.makeText(context, "Please enter an account number", Toast.LENGTH_SHORT).show()
                    } else {
                        findAndSaveRecipient(accountNumber.trim()) { recipients ->
                            recipientList = recipients
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Find User")
            }

            // Recipients List
            if (recipientList.isNotEmpty()) {
                Text(
                    text = "Saved Recipients",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recipientList) { recipient ->
                        RecipientItem(
                            recipient = recipient,
                            onRecipientClick = { selectedRecipient ->
                                val intent = Intent(context, TransferDetailActivity::class.java)
                                intent.putExtra("recipientName", selectedRecipient.name)
                                intent.putExtra("recipientAccount", selectedRecipient.accountNumber)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved recipients yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    @Composable
    fun RecipientItem(
        recipient: TransferRecipient,
        onRecipientClick: (TransferRecipient) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = { onRecipientClick(recipient) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = recipient.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = recipient.accountNumber ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    private fun findAndSaveRecipient(accountNumber: String, onRecipientsUpdated: (List<TransferRecipient>) -> Unit) {
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

    private fun loadSavedRecipients(onRecipientsLoaded: (List<TransferRecipient>) -> Unit) {
        savedRecipientsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipients = mutableListOf<TransferRecipient>()
                for (child in snapshot.children) {
                    val recipient = child.getValue(TransferRecipient::class.java)
                    if (recipient != null) {
                        recipients.add(recipient)
                    }
                }
                onRecipientsLoaded(recipients)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransferActivity, "Failed to load recipients: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}