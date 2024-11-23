package com.train.ramarai.postest.ui.main.outcome

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.train.ramarai.postest.data.model.InOutInventoryItems
import com.train.ramarai.postest.data.model.InventoryItem
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OutcomeViewmodel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _inventoryItems = MutableLiveData<List<InventoryItem>>()
    val inventoryItem: LiveData<List<InventoryItem>> get() = _inventoryItems

    private val _inOutItems = MutableLiveData<List<InOutInventoryItems>>()
    private val inOutItems: LiveData<List<InOutInventoryItems>> get() = _inOutItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val allInventoryItem: MutableList<InventoryItem> = mutableListOf()

    private val inOutItem: MutableList<InOutInventoryItems> = mutableListOf()

    private val _totalOutcome = MutableLiveData<Int>()
    private val totalOutcome: LiveData<Int> = _totalOutcome

    val combinedLiveData = MediatorLiveData<Pair<List<InOutInventoryItems>, Int>>()

    init {
        combinedLiveData.addSource(inOutItems) { items ->
            val currentIncome = totalOutcome.value ?: 0
            combinedLiveData.value = Pair(items, currentIncome)
        }

        combinedLiveData.addSource(totalOutcome) { total ->
            val currentItems = inOutItems.value ?: emptyList()
            combinedLiveData.value = Pair(currentItems, total)
        }
    }


    private fun isUserAuthenticated(): Boolean {
        val user = auth.currentUser
        return user != null
    }

    fun fetchInventoryItems() {
        if (!isUserAuthenticated()) {
            Log.w("AddIncomeViewModel", "User not authenticated")
            return
        }

        _isLoading.value = true

        if (allInventoryItem.isEmpty()) {
            db.collection("inventory")
                .get()
                .addOnSuccessListener { documents ->
                    allInventoryItem.clear()
                    for (document in documents) {
                        val item = InventoryItem(
                            id = document.id,
                            itemID = document.getString("itemID") ?: "",
                            itemName = document.getString("itemName") ?: "",
                            itemType = document.getString("itemType") ?: "",
                            itemBrand = document.getString("itemBrand") ?: "",
                            itemQty = document.getLong("itemQty")?.toInt() ?: 0,
                            itemPrice = document.getDouble("itemPrice") ?: 0.0
                        )
                        allInventoryItem.add(item)
                    }
                    _isLoading.value = false
                    _inventoryItems.value = allInventoryItem
                }
                .addOnFailureListener { exception ->
                    // Tangani kesalahan jika terjadi
                    Log.w("InventoryViewModel", "Error getting documents: ", exception)
                    _inventoryItems.value = emptyList() // Set menjadi daftar kosong jika gagal
                    _isLoading.value = false
                }
        } else {
            Log.d("AddIncomeViewModel", "List Not Empty")
            _isLoading.value = false
            _inventoryItems.value = allInventoryItem
        }
    }

    fun filterInventory(query: String) {
        _isLoading.value = true
        if (query.isEmpty()) {
            _inventoryItems.value = allInventoryItem
            _isLoading.value = false
        } else {
            val filteredList = allInventoryItem.filter {
                it.itemBrand.contains(query, ignoreCase = true) ||
                        it.itemType.contains(query, ignoreCase = true) ||
                        it.itemName.contains(query, ignoreCase = true)
            }
            _isLoading.value = false
            _inventoryItems.value = filteredList
        }
    }

    fun addItemToBag(
        selectedItem: InventoryItem,
        quantity: Int,
        buyPrice: Double,
    ) {
        if (!isUserAuthenticated()) {
            Log.w("AddIncomeViewModel", "User not authenticated")
            return
        }

        val existingItemIndex = inOutItem.indexOfFirst { it.itemUID == selectedItem.id }

        if (existingItemIndex != -1) {
            // Jika item sudah ada, buat salinan item dengan kuantitas yang diperbarui
            val existingItem = inOutItem[existingItemIndex]
            val updatedItem =
                existingItem.copy(itemQtyUpdate = existingItem.itemQtyUpdate + quantity)

            // Ganti item lama dengan item yang telah diperbarui
            inOutItem[existingItemIndex] = updatedItem
        } else {
            val newItem = InOutInventoryItems(
                itemUID = selectedItem.id,
                itemID = selectedItem.itemID,
                itemName = selectedItem.itemName,
                itemQtyStart = selectedItem.itemQty,
                itemQtyUpdate = quantity,
                itemType = selectedItem.itemType,
                itemPrice = buyPrice,
                itemBrand = selectedItem.itemBrand
            )
            inOutItem.add(newItem)
        }

        _inOutItems.value = inOutItem
        updateTotalIncome()
    }

    fun removeItemFromBag(item: InOutInventoryItems) {
        val existingItemIndex = inOutItem.indexOfFirst { it.itemUID == item.itemUID }

        if (existingItemIndex != -1) {
            inOutItem.removeAt(existingItemIndex)
            _inOutItems.value = inOutItem
            updateTotalIncome()  // Update total income after removal
        }
    }

    private fun updateTotalIncome() {
        val total = inOutItem.sumOf { it.itemPrice * it.itemQtyUpdate }
        _totalOutcome.value = total.toInt()
    }

    fun getItemsInBag(): List<InOutInventoryItems> {
        return inOutItem
    }

    fun addTransactionReport(
        transactionName: String,
        transactionAmount: Double,
        transactionItems: List<InOutInventoryItems>
    ) {
        if (!isUserAuthenticated()) {
            Log.w("AddIncomeViewModel", "User not authenticated")
            return
        }

        val userID = auth.currentUser?.uid

        val currentDate = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = currentDate.format(formatter)

        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val date = simpleDateFormat.parse(formattedDate)
        val transactionDate = date?.let { Timestamp(it) }

        val updateDesc = StringBuilder("Barang Masuk :\n")
        val transactionCategory = "Pengeluaran"

        transactionItems.forEach { items ->
            updateDesc.append("${items.itemName}: ${items.itemQtyUpdate}\n")
        }



        Log.d("AddIncomeViewModel", "Update Description: \n$updateDesc")

        val transactionReport = hashMapOf(
            "userID" to userID,
            "transactionCategory" to transactionCategory,
            "transactionDate" to transactionDate,
            "transactionName" to transactionName,
            "transactionDescription" to updateDesc.toString(),
            "transactionAmount" to transactionAmount,
        )

        db.collection("transactions").add(transactionReport)
            .addOnSuccessListener { documentReference ->
                val transactionID = documentReference.id
                addToGeneralReports(
                    transactionID,
                    userID.toString(),
                    transactionCategory,
                    transactionName,
                    transactionDate,
                    updateDesc.toString(),
                    transactionAmount
                )

                transactionItems.forEach { item ->
                    val itemData = hashMapOf(
                        "itemUID" to item.itemUID,
                        "itemID" to item.itemID,
                        "itemName" to item.itemName,
                        "itemType" to item.itemType,
                        "itemBrand" to item.itemBrand,
                        "itemQtyStart" to item.itemQtyStart,
                        "itemQtyUpdate" to item.itemQtyUpdate,
                        "itemPrice" to item.itemPrice,
                        "transactionID" to transactionID
                    )

                    db.collection("transactions")
                        .document(transactionID)
                        .collection("transactionItems")
                        .add(itemData)
                        .addOnSuccessListener {
                            updateInventory(
                                item.itemUID,
                                item.itemQtyStart,
                                item.itemQtyUpdate,
                                userID.toString(),
                                transactionID,
                                transactionDate!!,
                                item.itemName
                            )
                            Log.d(
                                "AddIncomeViewModel",
                                "Item added to subcollection: ${item.itemName}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w("AddIncomeViewModel", "Error adding item to subcollection", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("AddIncomeViewModel", "Error adding transaction report", e)
            }


    }

    fun addTransactionReportNewInventory(
        transactionName: String,
        transactionAmount: Double,
        transactionItems: InOutInventoryItems
    ) {
        if (!isUserAuthenticated()) {
            Log.w("AddIncomeViewModel", "User not authenticated")
            return
        }

        val userID = auth.currentUser?.uid

        val currentDate = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = currentDate.format(formatter)

        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val date = simpleDateFormat.parse(formattedDate)
        val transactionDate = date?.let { Timestamp(it) }

        val updateDesc = StringBuilder("Barang Masuk :\n")
        val transactionCategory = "Pengeluaran"

        updateDesc.append("${transactionItems.itemName}: ${transactionItems.itemQtyUpdate}\n")



        Log.d("AddIncomeViewModel", "Update Description: \n$updateDesc")

        val transactionReport = hashMapOf(
            "userID" to userID,
            "transactionCategory" to transactionCategory,
            "transactionDate" to transactionDate,
            "transactionName" to transactionName,
            "transactionDescription" to updateDesc.toString(),
            "transactionAmount" to transactionAmount,
        )

        db.collection("transactions").add(transactionReport)
            .addOnSuccessListener { documentReference ->
                val transactionID = documentReference.id
                addToGeneralReports(
                    transactionID,
                    userID.toString(),
                    transactionCategory,
                    transactionName,
                    transactionDate,
                    updateDesc.toString(),
                    transactionAmount
                )

                val itemData = hashMapOf(
                    "itemUID" to transactionItems.itemUID,
                    "itemID" to transactionItems.itemID,
                    "itemName" to transactionItems.itemName,
                    "itemType" to transactionItems.itemType,
                    "itemBrand" to transactionItems.itemBrand,
                    "itemQtyStart" to transactionItems.itemQtyStart,
                    "itemQtyUpdate" to transactionItems.itemQtyUpdate,
                    "itemPrice" to transactionItems.itemPrice,
                    "transactionID" to transactionID
                )


                db.collection("transactions")
                    .document(transactionID)
                    .collection("transactionItems")
                    .add(itemData)
                    .addOnSuccessListener {
                        addNewInventory(
                            transactionItems.itemID,
                            transactionItems.itemName,
                            transactionItems.itemBrand,
                            transactionItems.itemType,
                            transactionItems.itemQtyUpdate,
                            transactionItems.itemPrice,
                            userID!!
                        )
                        Log.d("AddIncomeViewModel", "Item added to subcollection: ${transactionItems.itemName}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("AddIncomeViewModel", "Error adding item to subcollection", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("AddIncomeViewModel", "Error adding transaction report", e)
            }


    }

    private fun addNewInventory(itemID: String,
                                itemName: String,
                                itemBrand: String,
                                itemType: String,
                                itemQty: Int,
                                itemPrice: Double,
                                addedBy: String
    ) {
        if (!isUserAuthenticated()) {
            Log.w("AddIncomeViewModel", "User not authenticated")
            return
        }

        val currentDate = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = currentDate.format(formatter)

        _isLoading.value = true
        val inventoryItem = hashMapOf(
            "itemID" to itemID,
            "itemName" to itemName,
            "itemBrand" to itemBrand,
            "itemType" to itemType,
            "itemQty" to itemQty,
            "itemPrice" to itemPrice,
            "addedBy" to addedBy,
            "updateDate" to formattedDate
        )

        db.collection("inventory").add(inventoryItem)
            .addOnSuccessListener {
                Log.d("AddIncomeViewModel", "Inventory added successfully")
            }
            .addOnFailureListener {
                Log.e("AddIncomeViewModel", "Error adding inventory", it)
            }

    }

    private fun updateInventory(
        itemUID: String?,
        itemQtyStart: Int,
        itemQtyUpdate: Int,
        userID: String,
        transactionID: String,
        formattedDate: Timestamp,
        itemName: String,
    ) {
        if (!isUserAuthenticated()) {
            Log.w("AddIncomeViewModel", "User not authenticated")
            return
        }

        val updateCategory: String = if (itemUID!!.isEmpty()) {
            "Barang Baru"
        } else {
            "Restock Barang"
        }
        val updateName = "Update Stok"
        val updateDesc = "Barang Masuk $itemName"
        val qtyFinal = itemQtyStart + itemQtyUpdate
        val inventoryData = hashMapOf<String, Any>(
            "addedBy" to userID,
            "updateDate" to formattedDate,
            "itemQty" to qtyFinal

        )
        if (itemUID.isNullOrEmpty()) {
            db.collection("inventory").add(inventoryData)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        "AddIncomeViewModel",
                        "New item added to inventory with ID: ${documentReference.id}"
                    )
                    addToGeneralReports(
                        transactionID,
                        userID,
                        updateCategory,
                        updateName,
                        formattedDate,
                        updateDesc,
                        itemQtyUpdate.toDouble()
                    )
                }
        } else {
            db.collection("inventory").document(itemUID)
                .update(inventoryData)
                .addOnSuccessListener {
                    addToGeneralReports(
                        transactionID,
                        userID,
                        updateCategory,
                        updateName,
                        formattedDate,
                        updateDesc,
                        itemQtyUpdate.toDouble()
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("AddIncomeViewModel", "Error updating document", e)
                }
        }

    }


    private fun addToGeneralReports(
        transactionID: String,
        userID: String,
        transactionCategory: String,
        transactionName: String,
        formattedDate: Timestamp?,
        transactionDescription: String,
        transactionAmount: Double,
    ) {
        if (!isUserAuthenticated()) {
            Log.w("AddIncomeViewModel", "User not authenticated")
            return
        }
        val generalReport = hashMapOf(
            "transactionID" to transactionID,
            "userID" to userID,
            "reportCategory" to transactionCategory,
            "reportTitle" to transactionName,
            "reportDate" to formattedDate,
            "reportDescription" to transactionDescription,
            "reportAmount" to transactionAmount
        )

        db.collection("reports")
            .add(generalReport)
            .addOnSuccessListener {
                Log.d("IncomeViewmodel", "Income Transactions added to report successfully!")
            }
            .addOnFailureListener { e ->
                Log.w("AddIncomeViewModel", "Error adding income to report", e)

            }
    }

}