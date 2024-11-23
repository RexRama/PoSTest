package com.train.ramarai.postest.ui.main.inventory

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.train.ramarai.postest.data.model.InventoryItem
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InventoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _inventoryItems = MutableLiveData<List<InventoryItem>>()
    val inventoryItems: LiveData<List<InventoryItem>> get() = _inventoryItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading


    private val allItems: MutableList<InventoryItem> = mutableListOf()

    fun fetchInventoryItems() {
        _isLoading.value = true
        db.collection("inventory")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val itemType = document.getString("itemType") ?: ""
                    val item = InventoryItem(
                        id = document.id,
                        itemID = document.getString("itemID") ?: "",
                        itemName = document.getString("itemName") ?: "",
                        itemType = itemType,
                        itemBrand = document.getString("itemBrand") ?: "",
                        itemQty = document.getLong("itemQty")?.toInt() ?: 0,
                        itemPrice = document.getDouble("itemPrice") ?: 0.0
                    )
                    allItems.add(item)
                }
                _isLoading.value = false
                _inventoryItems.value = allItems
            }
            .addOnFailureListener { exception ->
                // Tangani kesalahan jika terjadi
                Log.w("InventoryViewModel", "Error getting documents: ", exception)
                _inventoryItems.value = emptyList() // Set menjadi daftar kosong jika gagal
                _isLoading.value = false
            }
    }

    fun filterInventory(query: String) {
        _isLoading.value = true
        if (query.isEmpty()) {
            _inventoryItems.value = allItems
            _isLoading.value = false
        } else {
            val filteredList = allItems.filter {
                it.itemBrand.contains(query, ignoreCase = true) ||
                        it.itemType.contains(query, ignoreCase = true) ||
                        it.itemName.contains(query, ignoreCase = true)
            }
            _isLoading.value = false
            _inventoryItems.value = filteredList
        }
    }

    fun deleteInventory(itemId: String, context: Context) {
        Log.d("InventoryViewModel", "Deleting item with ID: $itemId") // Log the itemId
        val itemToDelete = allItems.find { it.id == itemId }

        // Proceed with deletion if the item exists
        if (itemToDelete != null) {
            // Get the current user
            val currentUser = auth.currentUser

            // Ensure the user is logged in
            if (currentUser != null) {
                // Fetch the user document to get the role and username
                db.collection("users").document(currentUser.uid) // Fetch by user ID
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val role = document.getString("role") ?: "Unknown Role"
                            val username = document.getString("username") ?: "Unknown User"

                            // Check if the user has the "Admin" role
                            if (role == "Admin") {
                                val updateQty =
                                    itemToDelete.itemQty.toDouble() // Assuming qty is relevant for reports
                                val updateDesc =
                                    "${itemToDelete.itemName} dihapus dari stok oleh $username"

                                // Add to report before deletion
                                addToReport("Stok Dihapus", updateQty, updateDesc)

                                // Now delete the inventory item
                                db.collection("inventory").document(itemId)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d(
                                            "InventoryViewModel",
                                            "DocumentSnapshot successfully deleted!"
                                        )
                                        Toast.makeText(
                                            context,
                                            "${itemToDelete.itemName} berhasil terhapus dari database.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        fetchInventoryItems() // Refresh the list after successful deletion
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("InventoryViewModel", "Error deleting document", e)
                                    }
                            } else {
                                Log.w("DeleteInventory", "User does not have permission to delete.")
                                // Show a Toast message
                                Toast.makeText(
                                    context,
                                    "Anda tidak memiliki izin untuk menghapus item ini.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Log.w("DeleteInventory", "No user found.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DeleteInventory", "Error fetching user document", e)
                    }
            } else {
                Log.w("DeleteInventory", "User is not logged in.")
                Toast.makeText(
                    context,
                    "Anda harus login untuk menghapus item.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Log.w("InventoryViewModel", "Item not found for deletion.")
        }
    }

    @Suppress("SameParameterValue")
    private fun addToReport(updateType: String, updateQty: Double, updateDesc: String) {
        val currentDate = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = currentDate.format(formatter)

        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val date = simpleDateFormat.parse(formattedDate)
        val transactionDate = date?.let { Timestamp(it) }

        val inventoryReport = hashMapOf(
            "reportDate" to transactionDate,
            "reportType" to updateType,
            "reportDescription" to updateDesc,
            "reportAmount" to updateQty
        )

        db.collection("reports")
            .add(inventoryReport)
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                Log.e("UpdateInventoryViewmodel", "Error adding to report", e)
            }
    }
}

