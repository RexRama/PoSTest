package com.train.ramarai.postest.ui.main.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.train.ramarai.postest.data.model.ReportItem

class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _reportItems = MutableLiveData<List<ReportItem>>()
    val reportItems: LiveData<List<ReportItem>> get() = _reportItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val allTransaction: MutableList<ReportItem> = mutableListOf()

    fun fetchReports() {
        _isLoading.value = true
        db.collection("reports")
            .orderBy("reportDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                allTransaction.clear()
                for (document in documents) {
                    val reportItem = ReportItem(
                        reportID = document.id,
                        reportTitle = document.getString("reportTitle") ?: "Update Stok",
                        reportDate = document.getTimestamp("reportDate")!!,
                        reportAmount = document.getDouble("reportAmount") ?: 0.0,
                        reportType = document.getString("reportCategory") ?: "",
                        reportDescription = document.getString("reportDescription") ?: ""


                    )
                    allTransaction.add(reportItem)
                }
                _isLoading.value = false
                _reportItems.value = allTransaction

            }
            .addOnFailureListener { exception ->
                Log.w("MainViewModel", "Error getting documents: ", exception)
                _reportItems.value = emptyList()
                _isLoading.value = false
            }
    }
}