package com.train.ramarai.postest.data.model

import com.google.firebase.Timestamp

data class ReportItem(
    val reportID: String,
    val userID: String = " ",
    val reportDate: Timestamp,
    val reportTitle: String,
    val reportType: String,
    val reportDescription: String,
    val reportAmount: Double,
    val reportItem: List<InOutInventoryItems> = emptyList()
)
