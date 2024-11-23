package com.train.ramarai.postest.ui.main.inventory.add

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.train.ramarai.postest.R
import com.train.ramarai.postest.databinding.FragmentUpdateInventoryBinding
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore
import kotlinx.coroutines.launch

class UpdateInventoryFragment : Fragment() {
    private lateinit var viewModel: UpdateInventoryViewmodel
    private lateinit var binding: FragmentUpdateInventoryBinding
    private lateinit var preferences: SettingPreferences
    private lateinit var navController: NavController

    private var isUpdateMode = false
    private var itemID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUpdateInventoryBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        setupViewModel()
        setupDropDown()

        arguments?.let {
            isUpdateMode = it.getBoolean("isUpdate", false)
            if (isUpdateMode) {
                itemID = it.getString("itemID")
                val docID = itemID
                loadItemDetails(docID)
                setupUpdateMode()
                setupSubmitButton(docID)
            }
        }

        navController = findNavController()


        setupObservers()
        setupSubmitButton(itemID)

    }


    private fun setupViewModel() {
        preferences = SettingPreferences.getInstance(requireContext().dataStore)
        val factory = ViewModelFactory(this.requireContext(), preferences)
        viewModel = ViewModelProvider(this, factory)[UpdateInventoryViewmodel::class]
    }

    private fun loadItemDetails(itemID: String?) {
        itemID?.let {
            Log.d("UpdateInventoryFragment", "Loading item with ID: $it") // Add this line
            viewModel.loadInventoryItem(it)
            viewModel.inventoryItemDetails.observe(viewLifecycleOwner) { item ->
                if (item != null) {
                    binding.editAddInventoryItemID.setText(item.itemID)
                    binding.editAddInventoryItemName.setText(item.itemName)
                    binding.editAddInventoryItemBrand.setText(item.itemBrand)
                    binding.editAddInventoryItemType.setText(item.itemType)
                    binding.editAddInventoryItemQty.setText(item.itemQty.toString())
                    binding.editAddInventoryItemPrice.setText(item.itemPrice.toString())
                    setupDropDown()
                } else {
                    Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: Toast.makeText(requireContext(), "Item ID is null", Toast.LENGTH_SHORT).show()
    }

    private fun setupUpdateMode() {
        binding.editAddInventoryItemID.isEnabled = false
        binding.editAddInventoryItemQty.isEnabled = false
        binding.editAddInventoryItemQty.isEnabled = false
        binding.editUpdateInventoryDescription.isEnabled = true
        binding.inputUpdateInventoryDescription.visibility = View.VISIBLE
    }

    private fun setupDropDown() {
        val itemTypes = resources.getStringArray(R.array.item_types).sortedArray()
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, itemTypes)
        binding.editAddInventoryItemType.setAdapter(adapter)

    }

    private fun setupSubmitButton(docID: String?) {
        val userName = lifecycleScope.launch {
            preferences.getUserDetails().collect { userDetail ->
                userDetail[""]
            }
        }
        binding.btRegister.setOnClickListener {
            val docsID = docID.toString()
            val itemID = binding.editAddInventoryItemID.text.toString()
            val itemName = binding.editAddInventoryItemName.text.toString()
            val itemBrand = binding.editAddInventoryItemBrand.text.toString()
            val itemType = binding.editAddInventoryItemType.text.toString()
            val itemQty = binding.editAddInventoryItemQty.text.toString()
            val itemPrice = binding.editAddInventoryItemPrice.text.toString()
            val updateDescription =
                binding.editUpdateInventoryDescription.text.toString() + " " + itemName

            if (this.isUpdateMode) {
                if (itemID.isEmpty() || itemName.isEmpty() || itemBrand.isEmpty() || itemType.isEmpty() || itemQty
                        .isEmpty() || itemPrice.isEmpty() || updateDescription.isEmpty()
                ) {
                    Toast.makeText(requireContext(), "Semua Field Harus Diisi!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
            } else {
                if (itemID.isEmpty() || itemName.isEmpty() || itemBrand.isEmpty() || itemType.isEmpty() || itemQty
                        .isEmpty() || itemPrice.isEmpty()
                ) {
                    Toast.makeText(requireContext(), "Semua Field Harus Diisi!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
            }


            if (this.isUpdateMode) {
                viewModel.updateInventory(
                    docsID,
                    itemID,
                    itemName,
                    itemBrand,
                    itemType,
                    itemQty.toInt(),
                    itemPrice.toDouble(),
                    userName.toString(),
                    updateDescription
                )
            } else {
                viewModel.addInventory(
                    itemID,
                    itemName,
                    itemBrand,
                    itemType,
                    itemQty.toInt(),
                    itemPrice.toDouble(),
                    userName.toString()
                )
            }
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            setLoading(isLoading)
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                val message =
                    if (isUpdateMode) "Barang berhasil diperbarui" else "Barang berhasil ditambahkan"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                clearFields()
                navController.popBackStack()


            } else {
                Toast.makeText(requireContext(), "Gagal menambahkan barang", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        viewModel.isAuthenticated.observe(viewLifecycleOwner) {

        }


    }

    private fun clearFields() {
        binding.editAddInventoryItemID.text?.clear()
        binding.editAddInventoryItemName.text?.clear()
        binding.editAddInventoryItemBrand.text?.clear()
        binding.editAddInventoryItemType.text?.clear()
        binding.editAddInventoryItemQty.text?.clear()
        binding.editAddInventoryItemPrice.text?.clear()
        binding.editUpdateInventoryDescription.text?.clear()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.pbAddInventory.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}