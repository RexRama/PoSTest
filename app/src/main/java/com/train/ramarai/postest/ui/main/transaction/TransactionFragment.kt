package com.train.ramarai.postest.ui.main.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.train.ramarai.postest.R
import com.train.ramarai.postest.databinding.FragmentTransactionBinding
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TransactionFragment : Fragment() {

    private lateinit var binding: FragmentTransactionBinding
    private lateinit var preferences: SettingPreferences
    private lateinit var transactionViewModel: AddTransactionViewModel

    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTransactionBinding.inflate(inflater, container, false)
        binding.editTransactionTransactionType.isEnabled = false


        setupViewModel()
        setupButton()
        setupDatePicker()
        setupDropDown()
        return binding.root
    }

    private fun setupButton() {
        binding.btSubmit.setOnClickListener {
            val date = binding.editTransactionTransactionDate.text.toString()
            val total = binding.editTransactionTotal.text.toString()
            val category = binding.editTransactionTransactionCategory.text.toString()
            val type = binding.editTransactionTransactionType.text.toString()
            val method = binding.editTransactionTransactionMethod.text.toString()
            val description = binding.editTransactionTransactionDescription.text.toString()

            // Validasi data sebelum disimpan
            if (date.isEmpty() || total.isEmpty() || category.isEmpty() ||
                type.isEmpty() || method.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Semua data harus diisi!", Toast.LENGTH_SHORT).show()
            } else {
                // Panggil fungsi untuk menambahkan transaksi
                transactionViewModel.addTransaction(
                    date, total, category, type, method, description,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { exception ->
                        Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun setupViewModel() {
        preferences = SettingPreferences.getInstance(requireContext().dataStore)
        val factory = ViewModelFactory(requireContext(), preferences)
        transactionViewModel = ViewModelProvider(this, factory)[AddTransactionViewModel::class]

        transactionViewModel.isLoading.observe(viewLifecycleOwner){isLoading ->
            showLoading(isLoading)
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.pbTransaction.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setupDatePicker() {
        binding.editTransactionTransactionDate.setOnClickListener {
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    updateDateInView()
                }

            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    // Fungsi untuk memperbarui tanggal yang dipilih ke EditText
    private fun updateDateInView() {
        val format = "dd/MM/yyyy" // Format tanggal yang akan ditampilkan
        val sdf = SimpleDateFormat(format, Locale.US)
        binding.editTransactionTransactionDate.setText(sdf.format(calendar.time))
    }

    private fun setupDropDown() {
        val categoryTransaction = arrayOf("Penghasilan", "Pengeluaran")
        val categoryAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, categoryTransaction)
        binding.editTransactionTransactionCategory.setAdapter(categoryAdapter)

        // Transaction type arrays
        val typeTransactionOut = arrayOf("Operasional", "Personal", "Lainnya")
        val typeTransactionIn = arrayOf("Penjualan", "Investasi", "Personal", "Lainnya")

        // Set up the method transaction options
        val methodTransaction = arrayOf("Tunai", "Non-Tunai")
        val methodAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, methodTransaction)
        binding.editTransactionTransactionMethod.setAdapter(methodAdapter)

        // Listen for changes in transaction category
        binding.editTransactionTransactionCategory.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = categoryTransaction[position]
            val typeAdapter = if (selectedCategory == "Penghasilan") {
                ArrayAdapter(requireContext(), R.layout.dropdown_item, typeTransactionIn)
            } else {
                ArrayAdapter(requireContext(), R.layout.dropdown_item, typeTransactionOut)
            }

            binding.editTransactionTransactionType.isEnabled = true
            binding.editTransactionTransactionType.setAdapter(typeAdapter)
        }
    }

}