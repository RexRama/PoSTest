package com.train.ramarai.postest.ui.main.outcome.addItem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.train.ramarai.postest.R
import com.train.ramarai.postest.data.model.InOutInventoryItems
import com.train.ramarai.postest.databinding.FragmentOutcomeAddBinding
import com.train.ramarai.postest.ui.main.outcome.OutcomeViewmodel
import com.train.ramarai.postest.utils.SettingPreferences
import com.train.ramarai.postest.utils.ViewModelFactory
import com.train.ramarai.postest.utils.dataStore


class OutcomeAddFragment : Fragment() {
    private var _binding: FragmentOutcomeAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var outcomeViewmodel: OutcomeViewmodel
    private lateinit var preferences: SettingPreferences
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOutcomeAddBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewmodel()
        setupDropDown()

        setupButton()
    }

    private fun setupButton() {
        navController = findNavController()
        binding.btRegister.setOnClickListener {
            val itemID = binding.editAddInventoryItemID.text.toString()
            val itemName = binding.editAddInventoryItemName.text.toString()
            val itemBrand = binding.editAddInventoryItemBrand.text.toString()
            val itemType = binding.editAddInventoryItemType.text.toString()
            val itemPrice = binding.editAddInventoryItemPrice.text.toString()
            val updateDescription =
                "Inventory baru $itemName"



            val dialogBuilder = AlertDialog.Builder(requireContext())
            val inflater = LayoutInflater.from(requireContext())
            val dialogView = inflater.inflate(R.layout.dialog_input_quantity_price, null)

            val editTextBuyPrice =
                dialogView.findViewById<TextInputEditText>(R.id.edit_itemBuyPrice_dialog)
            val editTextQuantity =
                dialogView.findViewById<TextInputEditText>(R.id.edit_OutQty_dialog)

            val transactionName = "Restock Barang"


            dialogBuilder.setView((dialogView))
                .setTitle("Masukkan Harga Beli dan Jumlah Barang")
                .setPositiveButton("Tambahkan") { dialog, _ ->
                    val quantityText = editTextQuantity.text.toString().trim()
                    val buyPriceText = editTextBuyPrice.text.toString().trim()

                    if (quantityText.isEmpty() || buyPriceText.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Harga Beli dan Jumlah barang tidak boleh kosong!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val quantity = quantityText.toIntOrNull()
                        val buyPrice = buyPriceText.toDoubleOrNull()
                        val selectedItem = InOutInventoryItems(
                            itemUID = null,
                            itemID = itemID,
                            itemName = itemName,
                            itemType = itemType,
                            itemBrand = itemBrand,
                            itemQtyStart = 0,
                            itemQtyUpdate = quantity!!.toInt(),
                            itemPrice = itemPrice.toDouble()
                        )
                        if (quantity != null && quantity > 0 && buyPrice != null && buyPrice > 0.0) {
                            val totalAmount = quantity * buyPrice
                            outcomeViewmodel.addTransactionReportNewInventory(transactionName, totalAmount, selectedItem)
                            navController.popBackStack()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Masukkan Harga beli dan Jumlah yang Valid",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }

            val dialog = dialogBuilder.create()
            dialog.show()
        }
    }

    private fun setupDropDown() {
        val itemTypes = resources.getStringArray(R.array.item_types).sortedArray()
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, itemTypes)
        binding.editAddInventoryItemType.setAdapter(adapter)
    }

    private fun setupViewmodel() {
        preferences = SettingPreferences.getInstance(requireContext().dataStore)
        val factory = ViewModelFactory(requireContext(), preferences)
        outcomeViewmodel = ViewModelProvider(this, factory)[OutcomeViewmodel::class]
    }


}