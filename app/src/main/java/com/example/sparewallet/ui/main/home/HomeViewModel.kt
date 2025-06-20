package com.example.sparewallet.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeViewModel : ViewModel() {


    private val database = FirebaseDatabase.getInstance("https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
    private val userRef = database.getReference("users").child(userId)

    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String> = _balance

    private val _accountNumber = MutableLiveData<String>()
    val accountNumber: LiveData<String> = _accountNumber

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    init {
        userRef.keepSynced(true)
        fetchUserData()
    }

    private fun fetchUserData() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _balance.value = snapshot.child("balance").getValue(String::class.java) ?: "0"
                _accountNumber.value = snapshot.child("accountNumber").getValue(String::class.java) ?: ""
                _name.value = snapshot.child("name").getValue(String::class.java) ?: ""
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun refreshData() {
        userRef.get().addOnSuccessListener { snapshot ->
            _balance.value = snapshot.child("balance").getValue(String::class.java) ?: "0"
            _accountNumber.value = snapshot.child("accountNumber").getValue(String::class.java) ?: ""
            _name.value = snapshot.child("name").getValue(String::class.java) ?: ""
        }
    }
}
