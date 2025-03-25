package com.example.sparewallet.ui.transfer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sparewallet.databinding.ItemTransferRecipientBinding
import com.example.sparewallet.model.TransferRecipient

class TransferRecipientAdapter(
    private val recipientList: List<TransferRecipient>,
    private val onItemClick: (TransferRecipient) -> Unit
) : RecyclerView.Adapter<TransferRecipientAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemTransferRecipientBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(recipient: TransferRecipient) {
            binding.recipientName.text = recipient.name
            binding.recipientAccount.text = recipient.accountNumber
            binding.root.setOnClickListener { onItemClick(recipient) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransferRecipientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(recipientList[position])
    }

    override fun getItemCount(): Int = recipientList.size
}
