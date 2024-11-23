package com.train.ramarai.postest.ui.main.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddTransactionViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _isAuthenticated = MutableLiveData<Boolean>()

    // LiveData untuk status loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        checkAuthentication()
    }

    private fun checkAuthentication() {
        val currentUser = auth.currentUser
        _isAuthenticated.value = currentUser != null
    }

    fun addTransaction(
        date: String,
        total: String,
        category: String,
        type: String,
        method: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        _isLoading.value = true // Mulai loading

        // Buat objek transaksi yang akan disimpan di Firestore
        val transaction = hashMapOf(
            "date" to date,
            "total" to total,
            "category" to category,
            "type" to type,
            "method" to method,
            "description" to description
        )



        // Menyimpan transaksi ke Firestore
        firestore.collection("transactions")
            .add(transaction)
            .addOnSuccessListener {
                _isLoading.value = false
                onSuccess()
                addToReports(date, description, method, category, type, total.toDouble(), onSuccess, onError)
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                onError(exception)
            }
    }

    private fun addToReports(
        date: String,
        description: String,
        method: String,
        category: String,
        type: String,
        total: Double,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val transactionReport = hashMapOf(
            "reportDate" to date,
            "reportDescription" to description,
            "reportType" to method,
            "reportName" to "$category $type",
            "reportAmount" to total
        )

        firestore.collection("reports")
            .add(transactionReport)
            .addOnSuccessListener {
                _isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                onError(exception)
            }

    }
}