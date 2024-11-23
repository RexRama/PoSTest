package com.train.ramarai.postest.ui.main.outcome.report

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.train.ramarai.postest.data.model.OutcomeReportItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class OutcomeReportViewmodel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _outcomeReportItems = MutableLiveData<List<OutcomeReportItem>>()
    val outcomeReportItems: LiveData<List<OutcomeReportItem>> get() = _outcomeReportItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val allOutcome: MutableList<OutcomeReportItem> = mutableListOf()

    private fun isUserAuthenticated(): Boolean {
        val user = auth.currentUser
        return user != null
    }

    fun fetchOutcome() {
        if (!isUserAuthenticated()) {
            Log.w("OutcomeReportViewModel", "User not authenticated")
            return
        }

        _isLoading.value = true
        db.collection("transactions")
            .orderBy(
                "transactionDate",
                Query.Direction.DESCENDING
            )  // Adjust field name if necessary
            .get()
            .addOnSuccessListener { documents ->
                allOutcome.clear()
                Log.d("OutcomeReportViewModel", "Fetched ${documents.size()} transactions")

                for (document in documents) {
                    val transactionCategory = document.getString("transactionCategory") ?: ""

                    // Filter only transactions with "Pengeluaran" category
                    if (transactionCategory == "Pengeluaran") {
                        val transactionID = document.id
                        val item = OutcomeReportItem(
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

                        // Add the transaction to the list
                        allOutcome.add(item)
                    }
                }

                if (allOutcome.isEmpty()) {
                    Log.d("OutcomeReportViewModel", "No outcomes found.")
                    _outcomeReportItems.value = emptyList()
                } else {
                    Log.d(
                        "OutcomeReportViewModel",
                        "Outcome fetched successfully, count: ${allOutcome.size}"
                    )
                    _outcomeReportItems.value = allOutcome
                }

                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.w("OutcomeReportViewModel", "Error getting documents: ", e)
                _outcomeReportItems.value = emptyList()
                _isLoading.value = false
            }
    }

    fun filterOutcomeByDate(startDate: String, endDate: String?) {
        Log.d(
            "OutcomeReportViewModel",
            "filterOutcomeByDate called with StartDate: $startDate, EndDate: $endDate"
        )

        if (!isUserAuthenticated()) {
            Log.w("OutcomeReportViewModel", "User not authenticated")
            return
        }

        _isLoading.value = true

        val startDateParsed: Date
        val endDateParsed: Date?

        try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            format.timeZone = TimeZone.getTimeZone("GMT+07:00")  // Set to local timezone
            startDateParsed =
                format.parse(startDate) ?: throw IllegalArgumentException("Start date is null")
            endDateParsed = endDate?.let { format.parse(endDate) }

            Log.d(
                "OutcomeReportViewModel",
                "Parsed StartDate: $startDateParsed, Parsed EndDate: $endDateParsed"
            )
        } catch (e: Exception) {
            Log.e("OutcomeReportViewModel", "Invalid date format: ${e.message}")
            _isLoading.value = false
            return
        }

        // Set the time of startDateParsed to 00:00:00 for comparison
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

        // Prepare the query based on whether endDateParsed is provided or not
        val transactionsQuery = db.collection("transactions")
            .whereEqualTo("transactionCategory", "Pengeluaran")
            .orderBy("transactionDate", Query.Direction.DESCENDING)
            .apply {
                Log.d(
                    "OutcomeReportViewModel",
                    "Filtering by date range from $startDatePreviousDayMaxTime to $endDateWithMaxTime"
                )
                // Filter by the date range
                whereGreaterThanOrEqualTo("transactionDate", startDatePreviousDayMaxTime)
                    .whereLessThanOrEqualTo("transactionDate", endDateWithMaxTime)
            }

        transactionsQuery
            .get()
            .addOnSuccessListener { documents ->
                allOutcome.clear()
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
                        val item = OutcomeReportItem(
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
                        allOutcome.add(item)
                    }
                }

                // If no filtered outcomes, log and show empty list
                if (allOutcome.isEmpty()) {
                    Log.d(
                        "OutcomeReportViewModel",
                        "No outcomes found for the selected date range."
                    )
                    _outcomeReportItems.value = emptyList()
                } else {
                    Log.d(
                        "OutcomeReportViewModel",
                        "Filtered outcome fetched successfully, count: ${allOutcome.size}"
                    )
                    _outcomeReportItems.value = allOutcome
                }

                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e("OutcomeReportViewModel", "Error fetching documents: ", e)
                _outcomeReportItems.value = emptyList()
                _isLoading.value = false
            }
    }

}
