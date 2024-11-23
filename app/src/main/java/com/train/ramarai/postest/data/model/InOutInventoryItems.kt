package com.train.ramarai.postest.data.model

data class InOutInventoryItems(
    val itemUID: String? = "",
    val itemID: String = "",
    val itemName: String = "",
    val itemType: String = "",
    val itemBrand: String = "",
    val itemQtyStart: Int = 0,
    val itemQtyUpdate: Int = 0,
    val itemPrice: Double = 0.0
)
