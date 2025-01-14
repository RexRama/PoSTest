package com.train.ramarai.postest.ui.main.report

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.train.ramarai.postest.data.model.InOutReportItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ReportViewmodel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _reportItems = MutableLiveData<List<InOutReportItem>>()
    val reportItems: LiveData<List<InOutReportItem>> get() = _reportItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> get() = _totalIncome

    private val _totalOutcome = MutableLiveData<Double>()
    val totalOutcome: LiveData<Double> get() = _totalOutcome

    private val allReports: MutableList<InOutReportItem> = mutableListOf()

    private fun isUserAuthenticated(): Boolean {
        val user = auth.currentUser
        return user != null
    }

    fun filterReportByDate(startDate: String, endDate: String?) {
        Log.d(
            "ReportViewmodel",
            "filterIncomeByDate called with StartDate: $startDate, EndDate: $endDate"
        )
        if (!isUserAuthenticated()) {
            Log.w("ReportViewmodel", "User not Authenticated")
            return
        }

        _isLoading.value = true

        val startDateParsed: Date
        val endDateParsed: Date?

        try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            format.timeZone = TimeZone.getTimeZone("GMT+07:00")
            startDateParsed =
                format.parse(startDate) ?: throw IllegalArgumentException("Start date is null")
            endDateParsed = endDate?.let { format.parse(endDate) }

            Log.d(
                "ReportViewmodel",
                "Parsed StartDate: $startDateParsed, Parsed EndDate: $endDateParsed"
            )
        } catch (e: Exception) {
            Log.e("ReportViewmodel", "Invalid date format: ${e.message}")
            _isLoading.value = false
            return
        }

        val calendar = Calendar.getInstance()
        calendar.time = startDateParsed
        calendar.add(Calendar.DAY_OF_YEAR, -1)
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
            .orderBy("transactionDate", Query.Direction.DESCENDING)
            .apply {
                Log.d(
                    "ReportViewmodel",
                    "Filtering by date range from $startDatePreviousDayMaxTime to $endDateWithMaxTime"
                )
                // Filter by the date range
                whereGreaterThanOrEqualTo("transactionDate", startDatePreviousDayMaxTime)
                    .whereLessThanOrEqualTo("transactionDate", endDateWithMaxTime)

            }

        transactionQuery
            .get()
            .addOnSuccessListener { documents ->
                allReports.clear()
                Log.d(
                    "ReportViewmodel",
                    "Fetched ${documents.size()} filtered transactions"
                )

                documents.forEach { document ->
                    val transactionDate =
                        document.getTimestamp("transactionDate")?.toDate() ?: return@forEach
                    val transactionCategory =
                        document.getString("transactionCategory") ?: return@forEach
                    Log.d(
                        "ReportViemodel",
                        "Transaction Date: $transactionDate, Category: $transactionCategory"
                    )

                    if ((transactionCategory == "Pengeluaran" || transactionCategory == "Penghasilan") &&
                        transactionDate.after(startDatePreviousDayMaxTime) &&
                        transactionDate.before(endDateWithMaxTime)
                    ) {
                        val transactionID = document.id
                        val item = InOutReportItem(
                            transactionID = transactionID,
                            transactionCategory = transactionCategory,
                            transactionDate = document.getTimestamp("transactionDate")!!,
                            transactionDescription = document.getString("transactionDescription")
                                ?: "",
                            transactionName = document.getString("transactionName") ?: "",
                            transactionAmount = document.getDouble("transactionAmount") ?: 0.0,
                            userID = document.getString("userID") ?: "",
                            transactionItems = emptyList()
                        )
                        allReports.add(item)
                    }
                }

                if (allReports.isEmpty()) {
                    Log.d(
                        "ReportViewmodel",
                        "No transactions found for the selected date range."
                    )
                    _reportItems.value = emptyList()
                } else {
                    Log.d(
                        "ReportViewmodel",
                        "Filtered transactions fetched successfully, count: ${allReports.size}"
                    )
                    _reportItems.value = allReports
                    updateTotal()
                }
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e("ReportViewmodel", "Error fetching documents: ", e)
                _reportItems.value = emptyList()
                _isLoading.value = false
            }
    }

    private fun updateTotal() {
        val income = allReports.filter { it.transactionCategory == "Penghasilan" }
            .sumOf { it.transactionAmount }
        val outcome = allReports.filter { it.transactionCategory == "Pengeluaran" }
            .sumOf { it.transactionAmount }

        _totalIncome.value = income
        _totalOutcome.value = outcome

        Log.d("ReportViewmodel", "Total Income:$totalIncome, Total Outcome:$totalOutcome")
    }
}