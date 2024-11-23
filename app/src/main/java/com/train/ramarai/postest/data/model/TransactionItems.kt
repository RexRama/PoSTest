package com.train.ramarai.postest.data.model

import com.google.firebase.Timestamp

data class TransactionItems(
    val transactionID: String = "",
    val userID: String = "",
    val transactionCategory: String = "",
    val transactionDate: Timestamp,
    val transactionDescription: String = "",
    val transactionName: String = "",
    val transactionAmount: Double = 0.0,
    val transactionItems: List<InOutInventoryItems>

)
