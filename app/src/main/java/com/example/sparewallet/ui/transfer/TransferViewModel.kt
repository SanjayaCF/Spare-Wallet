package com.example.sparewallet.ui.transfer

import androidx.lifecycle.ViewModel
import com.example.sparewallet.model.TransferRecipient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TransferViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val usersRef = database.getReference("users")
    private val currentUserUid = auth.currentUser?.uid ?: ""
    private val savedRecipientsRef = database.getReference("transferRecipients").child(currentUserUid)

    private val _recipients = MutableStateFlow<List<TransferRecipient>>(emptyList())
    val recipients: StateFlow<List<TransferRecipient>> = _recipients

    fun loadRecipients() {
        if (currentUserUid.isEmpty()) return

        savedRecipientsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipientList = mutableListOf<TransferRecipient>()
                snapshot.children.forEach {
                    val recipient = it.getValue(TransferRecipient::class.java)
                    recipient?.let { recipientList.add(it) }
                }
                _recipients.value = recipientList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun findRecipient(accountNumber: String, onResult: (TransferRecipient?) -> Unit) {
        val query = usersRef.orderByChild("accountNumber").equalTo(accountNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val recipientSnapshot = snapshot.children.first()
                    val recipient = recipientSnapshot.getValue(TransferRecipient::class.java)

                    if (recipient != null) {
                        // Simpan penerima yang ditemukan ke daftar 'savedRecipients' untuk masa depan
                        savedRecipientsRef.child(recipient.accountNumber).setValue(recipient)
                    }
                    onResult(recipient)
                } else {
                    onResult(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(null)
            }
        })
    }
}