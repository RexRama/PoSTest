package com.train.ramarai.postest.ui.main.report

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
import com.train.ramarai.postest.R
import com.train.ramarai.postest.data.model.InOutReportItem
import com.train.ramarai.postest.databinding.FragmentReportBinding
import com.train.ramarai.postest.ui.main.report.adapter.ReportAdapter
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ReportFragment : Fragment() {
    private lateinit var binding: FragmentReportBinding

    private lateinit var pref: SettingPreferences
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var reportViewmodel: ReportViewmodel

    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportBinding.inflate(inflater, container, false)
        val view = binding.root

        setupViewmodel()
        setupDatePicker()

        return view
    }

    private fun setupViewmodel() {
        pref = SettingPreferences.getInstance(requireContext().dataStore)
        val factory = ViewModelFactory(requireContext(), pref)
        reportViewmodel = ViewModelProvider(this, factory)[ReportViewmodel::class]

        reportViewmodel.reportItems.observe(viewLifecycleOwner) { reports ->
            setupRecyclerView(reports)
        }

        reportViewmodel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        reportViewmodel.totalIncome.observe(viewLifecycleOwner) { totalIncome ->
            binding.tvReportIncomeTotal.text = getString(R.string.money_format_in, totalIncome)
            binding.tvReportIncomeTotal.setTextColor(requireContext().getColor(R.color.positive))
        }

        // Observe changes in totalOutcome and update UI
        reportViewmodel.totalOutcome.observe(viewLifecycleOwner) { totalOutcome ->
            binding.tvReportOutcomeTotal.text = getString(R.string.money_format_out, totalOutcome)
            binding.tvReportOutcomeTotal.setTextColor(requireContext().getColor(R.color.negative))
        }
    }


    private fun setupRecyclerView(reports: List<InOutReportItem>) {
        reportAdapter = ReportAdapter(reports)
        binding.reportRvReports.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportAdapter
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.pbOutcome.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setupDatePicker() {
        binding.editReportStartDate.setOnClickListener {
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { _: DatePicker, year : Int, month: Int, day: Int ->
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
            binding.inputReportEndDate.isVisible = isChecked
            binding.inputReportEndDate.isEnabled = isChecked

            binding.editReportEndDate.setOnClickListener {
                val dateSetListener =
                    DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
                        endCalendar.set(Calendar.YEAR, year)
                        endCalendar.set(Calendar.MONTH, month)
                        endCalendar.set(Calendar.DAY_OF_MONTH, day)
                        updateDateInView()
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

        binding.editReportStartDate.setText(startDate)
        binding.editReportEndDate.setText(endDate)

        binding.btIncomeSubmitDate.setOnClickListener {
            val startDatePicked = binding.editReportStartDate.text.toString()
            val endDatePicked = if (binding.cbSetLocation.isChecked) {
                binding.editReportEndDate.text.toString()
            } else {
                null
            }

            Log.d("ReportFragment", "Selected startDate: $startDatePicked, endDate: $endDatePicked")

            try {
                reportViewmodel.filterReportByDate(startDatePicked, endDatePicked)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}