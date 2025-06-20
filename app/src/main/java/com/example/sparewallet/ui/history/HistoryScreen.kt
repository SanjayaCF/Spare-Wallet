package com.example.sparewallet.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.sparewallet.model.TransactionRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val uid = auth.currentUser?.uid.orEmpty()
    private val database = FirebaseDatabase.getInstance(
        "https://sparewallet-55512-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).getReference("transactions").child(uid)

    private val _transactions = MutableStateFlow<List<TransactionRecord>>(emptyList())
    val transactions: StateFlow<List<TransactionRecord>> = _transactions

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US)
    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    init {
        database.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = mutableListOf<TransactionRecord>()
                snapshot.children.forEach { child ->
                    child.getValue(TransactionRecord::class.java)?.let { list.add(it) }
                }
                list.sortByDescending { it.timestamp }
                _transactions.value = list
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
            }
        })
    }

    fun formatAmount(amount: String): String {
        val value = amount.toDoubleOrNull() ?: 0.0
        return "Rp. ${numberFormat.format(value)}"
    }

    fun formatTimestamp(ts: Long): String = dateFormat.format(Date(ts))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val transactions by viewModel.transactions.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Transaction History") }) }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { txn ->
                    TransactionItem(
                        transaction = txn,
                        formatAmount = viewModel::formatAmount,
                        formatTime = viewModel::formatTimestamp
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionRecord,
    formatAmount: (String) -> String,
    formatTime: (Long) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = transaction.type,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = transaction.details,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatAmount(transaction.amount),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTime(transaction.timestamp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
