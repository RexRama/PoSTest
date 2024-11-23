package com.train.ramarai.postest.ui.main.inventory.add

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.train.ramarai.postest.data.model.InventoryItem
import com.train.ramarai.postest.data.model.ItemCategory
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UpdateInventoryViewmodel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> get() = _isSuccess

    private val _isAuthenticated = MutableLiveData<Boolean>()
    val isAuthenticated: LiveData<Boolean> get() = _isAuthenticated

    private val _inventoryItemDetails = MutableLiveData<InventoryItem?>()
    val inventoryItemDetails: LiveData<InventoryItem?> get() = _inventoryItemDetails


    init {
        checkAuthentication()
    }


    private fun checkAuthentication() {
        val currentUser = auth.currentUser
        _isAuthenticated.value = currentUser != null
    }

    fun addInventory(
        itemID: String,
        itemName: String,
        itemBrand: String,
        itemType: String,
        itemQty: Int,
        itemPrice: Double,
        addedBy: String
    ) {
        if (auth.currentUser == null) {
            _isSuccess.value = false
            return
        }

        val currentDate = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = currentDate.format(formatter)

        _isLoading.value = true
        val inventoryItem = mapOf(
            "itemID" to itemID,
            "itemName" to itemName,
            "itemBrand" to itemBrand,
            "itemType" to itemType,
            "itemQty" to itemQty,
            "itemPrice" to itemPrice,
            "addedBy" to addedBy,
            "updateDate" to formattedDate
        )

        val newStock = "Stok Baru"
        val descItem = "$newStock $itemBrand $itemName"

        db.collection("inventory")
            .add(inventoryItem)
            .addOnSuccessListener {
                _isSuccess.value = true
                _isLoading.value = false
                addToReport(descItem)
            }
            .addOnFailureListener {
                _isSuccess.value = false
                _isLoading.value = false
            }
    }

    @SuppressLint("SimpleDateFormat")
    fun updateInventory(
        docID: String,
        itemID: String,
        itemName: String,
        itemBrand: String,
        itemType: String,
        itemQty: Int,
        itemPrice: Double,
        userID: String,
        updateDesc: String
    ) {
        if (auth.currentUser == null) {
            _isSuccess.value = false
            return
        }

        _isLoading.value = true

        val currentDate = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = currentDate.format(formatter)

        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val date = simpleDateFormat.parse(formattedDate)
        val transactionDate = date?.let { Timestamp(it) }

        val itemData = hashMapOf(
            "itemID" to itemID,
            "itemName" to itemName,
            "itemBrand" to itemBrand,
            "itemType" to itemType,
            "itemQty" to itemQty,
            "itemPrice" to itemPrice,
            "addedBy" to userID,
            "updateDate" to transactionDate!!
        )

        db.collection("inventory").document(docID)
            .update(itemData as Map<String, Any>)
            .addOnSuccessListener {
                _isSuccess.value = true
                addToReport(updateDesc)
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _isSuccess.value = false
                Log.e("UpdateInventoryViewmodel", "Error updating document", e)
            }

    }

    private fun addToReport(updateDesc: String) {
        val currentDate = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = currentDate.format(formatter)

        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val date = simpleDateFormat.parse(formattedDate)
        val transactionDate = date?.let { Timestamp(it) }

        val updateType = "Update data barang"

        val inventoryReport = hashMapOf(
            "reportType" to updateType,
            "reportDescription" to updateDesc,
            "reportDate" to transactionDate

            )

        db.collection("reports")
            .add(inventoryReport)
            .addOnSuccessListener {
                _isLoading.value = false
                _isSuccess.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _isSuccess.value = false
                Log.e("UpdateInventoryViewmodel", "Error updating document inventory", e)
            }
    }

    fun loadInventoryItem(itemID: String) {
        if (auth.currentUser == null) {
            _isSuccess.value = false
            return
        }

        _isLoading.value = true
        db.collection("inventory").document(itemID)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val item = InventoryItem(
                        id = document.id,
                        itemID = document.getString("itemID") ?: "",
                        itemName = document.getString("itemName") ?: "",
                        itemType = document.getString("itemType") ?: "",
                        itemBrand = document.getString("itemBrand") ?: "",
                        itemQty = document.getLong("itemQty")?.toInt() ?: 0,
                        itemPrice = document.getDouble("itemPrice") ?: 0.0
                    )
                    _inventoryItemDetails.value = item
                } else {
                    _inventoryItemDetails.value = null
                }
                _isLoading.value = false
            }
            .addOnFailureListener {
                _inventoryItemDetails.value = null
                _isLoading.value = false
            }
    }
}