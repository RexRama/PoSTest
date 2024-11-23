package com.train.ramarai.postest.ui.main.income

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.train.ramarai.postest.R
import com.train.ramarai.postest.data.model.InOutInventoryItems
import com.train.ramarai.postest.data.model.InventoryItem
import com.train.ramarai.postest.databinding.FragmentIncomeBinding
import com.train.ramarai.postest.ui.main.income.adapter.AddIncomeAdapter
import com.train.ramarai.postest.ui.main.income.adapter.BsAddIncomeAdapter
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore


class IncomeFragment : Fragment() {
    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var incomeViewmodel: IncomeViewmodel
    private lateinit var preferences: SettingPreferences
    private lateinit var addIncomeAdapter: AddIncomeAdapter
    private lateinit var bsAddIncomeAdapter: BsAddIncomeAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomeBinding.inflate(inflater, container, false)
        val view = binding.root
        navController = findNavController()

        setupBottomSheet()

        setupViewmodel()

        observeViewmodel()

        incomeViewmodel.fetchInventoryItems()

        setupSearchFilter()

        return view
    }

    private fun setupViewmodel() {
        preferences = SettingPreferences.getInstance(requireContext().dataStore)
        val factory = ViewModelFactory(requireContext(), preferences)
        incomeViewmodel = ViewModelProvider(this, factory)[IncomeViewmodel::class]
    }

    private fun observeViewmodel() {
        incomeViewmodel.inventoryItem.observe(viewLifecycleOwner) { inventoryItems ->
            setupRecyclerView(inventoryItems)
        }

        incomeViewmodel.combinedLiveData.observe(viewLifecycleOwner) { (itemInBag, totalIncome) ->
            binding.tvIncomeTotal.text = getString(R.string.money_format, totalIncome.toDouble())
            setupBottomSheetRv(itemInBag.toMutableList(), totalIncome.toDouble())
        }

        incomeViewmodel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun setupBottomSheetRv(
        itemInBag: MutableList<InOutInventoryItems>,
        totalAmount: Double
    ) {
        bsAddIncomeAdapter = BsAddIncomeAdapter(itemInBag, incomeViewmodel)
        binding.bsRvIncomeItem.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bsAddIncomeAdapter
            isNestedScrollingEnabled = true
        }

        val transactionName = "Penjualan Barang"

        binding.bsBtSubmit.setOnClickListener {
            if (totalAmount > 0 && itemInBag.isNotEmpty()) {
                // Add transaction report only if amount is valid and items exist
                incomeViewmodel.addTransactionReport(transactionName, totalAmount, itemInBag)
                navController.popBackStack()
            } else {
                // Handle cases where totalAmount is zero or no items in bag
                Toast.makeText(
                    requireContext(),
                    "Keranjang penjualan masih kosong!",
                    Toast.LENGTH_SHORT
                ).show()
                Log.w("setupBottomSheetRv", "Cannot submit, total amount or items are invalid")
            }
        }
    }

    private fun setupRecyclerView(inventoryItems: List<InventoryItem>) {
        addIncomeAdapter = AddIncomeAdapter(inventoryItems, incomeViewmodel)
        binding.rvInventoryStock.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addIncomeAdapter
            isNestedScrollingEnabled = true
        }
    }

    private fun setupBottomSheet() {
        val incomeBottomSheet = binding.bottomSheet
        val incomeBottomSheetBehavior = BottomSheetBehavior.from(incomeBottomSheet)

        incomeBottomSheetBehavior.peekHeight = 80
    }

    private fun setupSearchFilter() {
        with(binding) {
            searchViewInventory.setupWithSearchBar(searchInventory)
            searchViewInventory.editText.setOnEditorActionListener { _, actionId, _ ->
                searchInventory.setText(searchViewInventory.text)
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    incomeViewmodel.filterInventory(searchViewInventory.text.toString())
                    searchViewInventory.clearFocus()
                }
                searchViewInventory.hide()
                true
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbInventory.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


}