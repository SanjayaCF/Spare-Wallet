package com.example.sparewallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sparewallet.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private val transactions: List<TransactionRecord>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US)
    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: TransactionRecord) {
            binding.textTransactionType.text = transaction.type
            binding.textTransactionDetails.text = transaction.details
            val amount = transaction.amount.toDoubleOrNull() ?: 0.0
            binding.textTransactionAmount.text = "Rp. ${numberFormat.format(amount)}"
            binding.textTransactionTime.text = dateFormat.format(Date(transaction.timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size
}
