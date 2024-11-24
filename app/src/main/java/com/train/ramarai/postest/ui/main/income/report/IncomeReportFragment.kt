package com.train.ramarai.postest.ui.main.income.report

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.train.ramarai.postest.data.model.IncomeReportItem
import com.train.ramarai.postest.databinding.FragmentIncomeReportBinding
import com.train.ramarai.postest.ui.main.income.report.adapter.IncomeReportAdapter
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class IncomeReportFragment : Fragment() {
    private var _binding: FragmentIncomeReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: SettingPreferences
    private lateinit var incomeReportAdapter: IncomeReportAdapter
    private lateinit var incomeReportViewmodel: IncomeReportViewmodel

    private val startCalendar = Calendar.getInstance()  // Separate calendar for start date
    private val endCalendar = Calendar.getInstance()    // Separate calendar for end date

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomeReportBinding.inflate(inflater, container, false)
        val view = binding.root

        setUpViewModel()
        setupDatePicker()

        incomeReportViewmodel.fetchIncome()



        return view

    }

    private fun setupDatePicker() {
        binding.editIncomeStartDate.setOnClickListener {
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { _: DatePicker, year : Int , month: Int, day: Int ->
                    startCalendar.set(Calendar.YEAR, year)
                    startCalendar.set(Calendar.MONTH, month)
                    startCalendar.set(Calendar.DAY_OF_MONTH, day)
                    updateDateInView()
                }
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.cbSetLocation.setOnCheckedChangeListener { _, isChecked ->
            binding.inputIncomeEndDate.isVisible = isChecked
            binding.inputIncomeEndDate.isEnabled = isChecked
            binding.editIncomeEndDate.setOnClickListener{
                val dateSetListener =
                    DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
                        endCalendar.set(Calendar.YEAR, year)  // Use endCalendar for end date
                        endCalendar.set(Calendar.MONTH, month)
                        endCalendar.set(Calendar.DAY_OF_MONTH, day)
                        updateDateInView()  // Update end date in the view
                    }
                DatePickerDialog(
                    requireContext(),
                    dateSetListener,
                    endCalendar.get(Calendar.YEAR),
                    endCalendar.get(Calendar.MONTH),
                    endCalendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    private fun updateDateInView() {
        val format = "dd/MM/yyyy" // Make sure format is consistent
        val sdf = SimpleDateFormat(format, Locale.US)
        val startDate = sdf.format(startCalendar.time)  // Use startCalendar for start date
        val endDate = sdf.format(endCalendar.time)      // Use endCalendar for end date

        // Update the view with formatted date
        binding.editIncomeStartDate.setText(startDate)
        binding.editIncomeEndDate.setText(endDate)

        binding.btIncomeSubmitDate.setOnClickListener {
            val startDatePicked = binding.editIncomeStartDate.text.toString()
            val endDatePicked = if (binding.cbSetLocation.isChecked) {
                binding.editIncomeEndDate.text.toString()
            } else {
                null
            }

            // Log the selected date values for debugging
            Log.d("IncomeReportFragment", "Selected startDate: $startDatePicked, endDate: $endDatePicked")

            try {
                // Pass the dates to ViewModel
                incomeReportViewmodel.filterOutcomeByDate(startDatePicked, endDatePicked)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun setUpViewModel() {
        preferences = SettingPreferences.getInstance(requireContext().dataStore)
        val factory = ViewModelFactory(requireContext(), preferences)
        incomeReportViewmodel = ViewModelProvider(this, factory)[IncomeReportViewmodel::class.java]



        incomeReportViewmodel.incomeReportItems.observe(viewLifecycleOwner) { incomes ->
            setupRecyclerView(incomes)
        }

        incomeReportViewmodel.isLoading.observe(viewLifecycleOwner)
        { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun setupRecyclerView(
        incomes: List<IncomeReportItem>
    ) {

        incomeReportAdapter = IncomeReportAdapter(incomes)
        binding.rvNewestIncome.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                incomeReportAdapter

        }

    }

    private fun showLoading(loading: Boolean) {
        binding.pbIncome.visibility = if (loading) View.VISIBLE else View.GONE
    }


}