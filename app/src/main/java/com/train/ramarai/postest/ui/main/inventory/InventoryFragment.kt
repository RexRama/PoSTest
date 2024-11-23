package com.train.ramarai.postest.ui.main.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.train.ramarai.postest.R
import com.train.ramarai.postest.databinding.FragmentInventoryBinding
import com.train.ramarai.postest.data.model.InventoryItem
import com.train.ramarai.postest.ui.main.inventory.adapter.InventoryAdapter
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore

class InventoryFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var inventoryViewModel: InventoryViewModel
    private lateinit var preferences: SettingPreferences
    private lateinit var inventoryAdapter: InventoryAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        val view = binding.root

        navController = findNavController()

        setupViewModel()
        setupNavigation(navController)

        observeViewModel()
        inventoryViewModel.fetchInventoryItems()

        setupSearchFilter()


        return view
    }

    private fun setupSearchFilter() {


        with(binding) {
            searchViewInventory.setupWithSearchBar(searchInventory)
            searchViewInventory.editText.setOnEditorActionListener { _, actionId, _ ->
                searchInventory.setText(searchViewInventory.text)
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    inventoryViewModel.filterInventory(searchViewInventory.text.toString())
                    searchViewInventory.clearFocus()
                }
                searchViewInventory.hide()
                true
            }
        }

    }

    private fun observeViewModel() {
        inventoryViewModel.inventoryItems.observe(viewLifecycleOwner){items ->
            setupRecyclerView(items)
        }
        inventoryViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
           showLoading(isLoading)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbInventory.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView(items: List<InventoryItem>) {
        inventoryAdapter = InventoryAdapter(items, inventoryViewModel)
        binding.rvInventoryStock.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = inventoryAdapter
            isNestedScrollingEnabled = true
        }

        inventoryAdapter.setOnItemClickCallback(object : InventoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: InventoryItem) {
                val bundle = Bundle().apply {
                    putBoolean("isUpdate", true)
                    putString("itemID", data.id)
                }
                navController.navigate(R.id.inventory_to_addInventory, bundle)
            }

        })
    }

    private fun setupViewModel() {
        preferences = SettingPreferences.getInstance(requireContext().dataStore)
        val factory = ViewModelFactory(requireContext(),preferences)
        inventoryViewModel = ViewModelProvider(this, factory)[InventoryViewModel::class]
    }


    private fun setupNavigation(navController: NavController) {
        val fabAddInventory = binding.fabAddInventory

    }

}