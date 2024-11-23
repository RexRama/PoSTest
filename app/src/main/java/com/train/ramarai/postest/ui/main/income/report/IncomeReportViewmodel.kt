package com.train.ramarai.postest.ui.main.income.report

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.train.ramarai.postest.data.model.InOutInventoryItems
import com.train.ramarai.postest.data.model.IncomeReportItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class IncomeReportViewmodel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _incomeReportItems = MutableLiveData<List<IncomeReportItem>>()
    val incomeReportItems: LiveData<List<IncomeReportItem>> get() = _incomeReportItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val allIncome: MutableList<IncomeReportItem> = mutableListOf()


    private fun isUserAuthenticated(): Boolean {
        val user = auth.currentUser
        return user != null
    }

    fun fetchIncome() {
        if (!isUserAuthenticated()) {
            Log.w("IncomeReportViewmodel", "User not authenticated")
            return
        }

        _isLoading.value = true
        db.collection("transactions")
            .orderBy("transactionDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                allIncome.clear()
                for (document in documents) {
                    val transactionCategory = document.getString("transactionCategory") ?: ""

                    // Hanya ambil dokumen yang memiliki "Penghasilan" di kategori transaksi
                    if (transactionCategory.contains("Penghasilan")) {
                        val transactionID = document.id
                        val item = IncomeReportItem(
                            transactionID = transactionID,
                            transactionCategory = transactionCategory,
                            transactionDate = document.getTimestamp("transactionDate")!!,
                            transactionDescription = document.getString("transactionDescription") ?: "",
                            transactionName = document.getString("transactionName") ?: "",
                            transactionAmount = document.getDouble("transactionAmount") ?: 0.0,
                            userID = document.getString("userID") ?: "",
                            transactionItems = listOf() // Untuk sekarang kosong, nanti diisi
                        )

                        // Ambil item yang ada di dalam sub-koleksi `transactionItems`
                        db.collection("transactions").document(transactionID)
                            .collection("transactionItems")
                            .get()
                            .addOnSuccessListener { itemDocs ->
                                val transactionItems = mutableListOf<InOutInventoryItems>()
                                for (itemDoc in itemDocs) {
                                    val transactionItem = InOutInventoryItems(
                                        itemUID = itemDoc.getString("itemUID") ?: "",
                                        itemID = itemDoc.getString("itemID") ?: "",
                                        itemName = itemDoc.getString("itemName") ?: "",
                                        itemType = itemDoc.getString("itemType") ?: "",
                                        itemBrand = itemDoc.getString("itemBrand") ?: "",
                                        itemQtyStart = itemDoc.getDouble("itemQtyStart")?.toInt() ?: 0,
                                        itemQtyUpdate = itemDoc.getDouble("itemQtyUpdate")?.toInt() ?: 0,
                                        itemPrice = itemDoc.getDouble("itemPrice") ?: 0.0
                                    )
                                    transactionItems.add(transactionItem)
                                }
                                item.transactionItems = transactionItems

                                // Setelah mendapatkan item-itemnya, tambahkan ke dalam allIncome
                                allIncome.add(item)
                                _incomeReportItems.value = allIncome
                            }
                            .addOnFailureListener { e ->
                                Log.w("IncomeReportViewmodel", "Error getting transaction items: ", e)
                            }
                    }
                }
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.w("IncomeReportViewmodel", "Error getting documents: ", e)
                _incomeReportItems.value = emptyList()
                _isLoading.value = false
            }
    }

    fun filterOutcomeByDate(startDate: String, endDate: String?) {
        Log.d(
            "IncomeReportViewmodel",
            "filterIncomeByDate called with StartDate: $startDate, EndDate: $endDate"
        )

        if (!isUserAuthenticated()) {
            Log.w("OutcomeReportViewmodel", "User not authenticated")
            return
        }

        _isLoading.value = true

        val startDateParsed: Date
        val endDateParsed: Date?

        try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            format.timeZone = TimeZone.getTimeZone("GMT+07:00")
            startDateParsed = format.parse(startDate) ?: throw IllegalArgumentException("Start date is null")
            endDateParsed = endDate?.let { format.parse(endDate) }

            Log.d(
                "IncomeReportViewModel",
                "Parsed StartDate: $startDateParsed, Parsed EndDate: $endDateParsed"
            )
        } catch (e: Exception) {
            Log.e("IncomeReportViewmodel", "Invalid date format: ${e.message}")
            _isLoading.value = false
            return
        }

        val calendar = Calendar.getInstance()
        calendar.time = startDateParsed
        calendar.add(Calendar.DAY_OF_YEAR, -1)  // Subtract one day
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val startDatePreviousDayMaxTime = calendar.time

        // Prepare end date with max time (23:59:59.999) if provided, or use startDateWithMaxTime if endDate is null
        val endDateWithMaxTime = endDateParsed?.let {
            val calendarEnd = Calendar.getInstance()
            calendarEnd.time = it
            calendarEnd.set(Calendar.HOUR_OF_DAY, 23)
            calendarEnd.set(Calendar.MINUTE, 59)
            calendarEnd.set(Calendar.SECOND, 59)
            calendarEnd.set(Calendar.MILLISECOND, 999)
            calendarEnd.time
        } ?: run {
            // If endDate is null, use startDateWithZeroTime and set to max time (23:59:59.999)
            val calendarEnd = Calendar.getInstance()
            calendarEnd.time = startDateParsed
            calendarEnd.set(Calendar.HOUR_OF_DAY, 23)
            calendarEnd.set(Calendar.MINUTE, 59)
            calendarEnd.set(Calendar.SECOND, 59)
            calendarEnd.set(Calendar.MILLISECOND, 999)
            calendarEnd.time
        }

        val transactionQuery = db.collection("transactions")
            .whereEqualTo("transactionCategory", "Penghasilan")
            .orderBy("transactionDate", Query.Direction.DESCENDING)
            .apply {
                Log.d(
                    "IncomeReportViewmodel",
                    "Filtering by date range from $startDatePreviousDayMaxTime to $endDateWithMaxTime"
                )
                // Filter by the date range
                whereGreaterThanOrEqualTo("transactionDate", startDatePreviousDayMaxTime)
                    .whereLessThanOrEqualTo("transactionDate", endDateWithMaxTime)
            }
        transactionQuery
            .get()
            .addOnSuccessListener { documents ->
                allIncome.clear()
                Log.d("OutcomeReportViewModel", "Fetched ${documents.size()} filtered transactions")

                documents.forEach { document ->
                    val transactionDate =
                        document.getTimestamp("transactionDate")?.toDate() ?: return@forEach
                    val transactionCategory =
                        document.getString("transactionCategory") ?: return@forEach
                    Log.d(
                        "OutcomeReportViewModel",
                        "Transaction Date: $transactionDate, Category: $transactionCategory"
                    )

                    // Manually filter the results to ensure date matching
                    if (transactionCategory == "Pengeluaran" &&
                        transactionDate.after(startDatePreviousDayMaxTime) &&
                        transactionDate.before(endDateWithMaxTime)
                    ) {
                        val transactionID = document.id
                        val item = IncomeReportItem(
                            transactionID = transactionID,
                            transactionCategory = transactionCategory,
                            transactionDate = document.getTimestamp("transactionDate")!!,
                            transactionDescription = document.getString("transactionDescription")
                                ?: "",
                            transactionName = document.getString("transactionName") ?: "",
                            transactionAmount = document.getDouble("transactionAmount") ?: 0.0,
                            userID = document.getString("userID") ?: "",
                            transactionItems = emptyList() // No items needed
                        )
                        allIncome.add(item)
                    }
                }

                // If no filtered outcomes, log and show empty list
                if (allIncome.isEmpty()) {
                    Log.d(
                        "OutcomeReportViewModel",
                        "No outcomes found for the selected date range."
                    )
                    _incomeReportItems.value = emptyList()
                } else {
                    Log.d(
                        "OutcomeReportViewModel",
                        "Filtered outcome fetched successfully, count: ${allIncome.size}"
                    )
                    _incomeReportItems.value = allIncome
                }

                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e("OutcomeReportViewModel", "Error fetching documents: ", e)
                _incomeReportItems.value = emptyList()
                _isLoading.value = false
            }


    }


}