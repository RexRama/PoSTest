package com.train.ramarai.postest.data.model

data class InventoryItem(
    val id: String = "",
    val itemID: String = "",
    val itemName: String = "",
    val itemType: String = "",
    val itemBrand: String = "",
    val itemQty: Int = 0,
    val itemPrice: Double = 0.0
)
