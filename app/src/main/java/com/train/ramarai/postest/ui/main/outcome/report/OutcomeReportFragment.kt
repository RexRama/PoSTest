package com.train.ramarai.postest.ui.main.outcome.report

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
import com.train.ramarai.postest.data.model.OutcomeReportItem
import com.train.ramarai.postest.databinding.FragmentOutcomeReportBinding
import com.train.ramarai.postest.ui.main.outcome.report.adapter.OutcomeReportAdapter
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class OutcomeReportFragment : Fragment() {
    private var _binding: FragmentOutcomeReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: SettingPreferences
    private lateinit var outcomeReportAdapter: OutcomeReportAdapter
    private lateinit var outcomeReportViewmodel: OutcomeReportViewmodel

    private val startCalendar = Calendar.getInstance()  // Separate calendar for start date
    private val endCalendar = Calendar.getInstance()    // Separate calendar for end date

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOutcomeReportBinding.inflate(inflater, container, false)
        val view = binding.root

        setupViewmode()
        setupDatePicker()

        outcomeReportViewmodel.fetchOutcome()

        return view
    }

    private fun updateDateInView() {
        val format = "dd/MM/yyyy" // Make sure format is consistent
        val sdf = SimpleDateFormat(format, Locale.US)
        val startDate = sdf.format(startCalendar.time)  // Use startCalendar for start date
        val endDate = sdf.format(endCalendar.time)      // Use endCalendar for end date

        // Update the view with formatted date
        binding.editOutcomeStartDate.setText(startDate)
        binding.editOutcomeEndDate.setText(endDate)

        binding.btOutcomeSubmitDate.setOnClickListener {
            val startDatePicked = binding.editOutcomeStartDate.text.toString()
            val endDatePicked = if (binding.cbSetLocation.isChecked) {
                binding.editOutcomeEndDate.text.toString()
            } else {
                null
            }

            // Log the selected date values for debugging
            Log.d("OutcomeReportFragment", "Selected startDate: $startDatePicked, endDate: $endDatePicked")
            try {
                // Pass the dates to ViewModel
                outcomeReportViewmodel.filterOutcomeByDate(startDatePicked, endDatePicked)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupDatePicker() {
        binding.editOutcomeStartDate.setOnClickListener{
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
                    startCalendar.set(Calendar.YEAR, year)
                    startCalendar.set(Calendar.MONTH, month)
                    startCalendar.set(Calendar.DAY_OF_MONTH, day)
                    updateDateInView()  // Update start date in the view
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
            binding.inputOutcomeEndDate.isVisible = isChecked
            binding.inputOutcomeEndDate.isEnabled = isChecked
            binding.editOutcomeEndDate.setOnClickListener{
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

    private fun setupViewmode() {
        preferences = SettingPreferences.getInstance(requireContext().dataStore)
        val factory = ViewModelFactory(requireContext(), preferences)
        outcomeReportViewmodel = ViewModelProvider(this, factory)[OutcomeReportViewmodel::class]

        outcomeReportViewmodel.outcomeReportItems.observe(viewLifecycleOwner) { outcomes ->
            setupRecyclerView(outcomes)
        }

        outcomeReportViewmodel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun setupRecyclerView(outcomes: List<OutcomeReportItem>) {
        outcomeReportAdapter = OutcomeReportAdapter(outcomes)
        binding.rvNewestOutcome.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = outcomeReportAdapter
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.pbOutcome.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
