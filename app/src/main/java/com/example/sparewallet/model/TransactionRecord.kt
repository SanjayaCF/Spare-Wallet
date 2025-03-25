package com.example.sparewallet.model

data class TransactionRecord(
    val type: String = "",
    val amount: String = "",
    val timestamp: Long = 0,
    val details: String = ""
)
