package com.train.ramarai.postest.ui.main.outcome

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
import com.train.ramarai.postest.databinding.FragmentOutcomeBinding
import com.train.ramarai.postest.ui.main.outcome.adapter.AddOutcomeAdapter
import com.train.ramarai.postest.ui.main.outcome.adapter.BsOutcomeAdapter
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore


class OutcomeFragment : Fragment() {
    private var _binding: FragmentOutcomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var outcomeViewmodel: OutcomeViewmodel
    private lateinit var preferences: SettingPreferences
    private lateinit var addOutcomeAdapter: AddOutcomeAdapter
    private lateinit var bsOutcomeAdapter: BsOutcomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOutcomeBinding.inflate(inflater, container, false)
        val view = binding.root
        navController = findNavController()

        setupNavigation(navController)

        setupBottomSheet()

        setupViewModel()

        observeViewmodel()

        outcomeViewmodel.fetchInventoryItems()

        setupSearchFilter()

        return view

    }

    private fun setupNavigation(navController: NavController) {
        val addInventory = binding.btOutcomeAddNewItem

        addInventory.setOnClickListener{
            navController.navigate(R.id.action_nav_outcome_to_add_item)
        }
    }

    private fun setupViewModel() {
        preferences = SettingPreferences.getInstance(requireContext().dataStore)
        val factory = ViewModelFactory(requireContext(), preferences)
        outcomeViewmodel = ViewModelProvider(this, factory)[OutcomeViewmodel::class]
    }

    private fun observeViewmodel() {
        outcomeViewmodel.inventoryItem.observe(viewLifecycleOwner) { inventoryItems ->
            setupRecyclerView(inventoryItems)
        }

        outcomeViewmodel.combinedLiveData.observe(viewLifecycleOwner) { (itemInBag, totalOutcome) ->
            binding.tvIncomeTotal.text = getString(R.string.money_format, totalOutcome.toDouble())
            setupBottomSheetRv(itemInBag.toMutableList(), totalOutcome.toDouble())
        }

        outcomeViewmodel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun setupBottomSheetRv(
        itemInBag: MutableList<InOutInventoryItems>,
        totalAmount: Double
    ) {
        bsOutcomeAdapter = BsOutcomeAdapter(itemInBag, outcomeViewmodel)
        binding.bsRvIncomeItem.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bsOutcomeAdapter
            isNestedScrollingEnabled = true
        }

        val transactionName = "Restock Barang"

        binding.bsBtSubmit.setOnClickListener {
            if (totalAmount > 0 && itemInBag.isNotEmpty()) {

                outcomeViewmodel.addTransactionReport(transactionName, totalAmount, itemInBag)
                navController.navigate(R.id.action_nav_outcome_to_nav_home)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Keranjang update stock masih kosong!",
                    Toast.LENGTH_SHORT
                ).show()
                Log.w("setupBottomSheetRv", "Cannot submit, total amount or items are invalid")
            }
        }
    }

    private fun setupRecyclerView(inventoryItems: List<InventoryItem>) {
        addOutcomeAdapter = AddOutcomeAdapter(inventoryItems, outcomeViewmodel)
        binding.rvInventoryStock.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addOutcomeAdapter
            isNestedScrollingEnabled = true
        }
    }

    private fun setupBottomSheet() {
        val outcomeBottomSheet = binding.bottomSheet
        val outcomeBottomSheetBehavior = BottomSheetBehavior.from(outcomeBottomSheet)

        outcomeBottomSheetBehavior.peekHeight = 80
    }

    private fun setupSearchFilter() {
        with(binding) {
            searchViewInventory.setupWithSearchBar(searchInventory)
            searchViewInventory.editText.setOnEditorActionListener { _, actionId, _ ->
                searchInventory.setText(searchViewInventory.text)
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    outcomeViewmodel.filterInventory(searchViewInventory.text.toString())
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