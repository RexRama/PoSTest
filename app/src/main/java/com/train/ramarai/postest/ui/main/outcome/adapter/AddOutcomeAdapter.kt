package com.train.ramarai.postest.ui.main.outcome.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.train.ramarai.postest.R
import com.train.ramarai.postest.data.model.InventoryItem
import com.train.ramarai.postest.databinding.InventorySaleItemBinding
import com.train.ramarai.postest.ui.main.outcome.OutcomeViewmodel

class AddOutcomeAdapter(
    private var inventoryItems: List<InventoryItem>,
    private var outcomeViewmodel: OutcomeViewmodel
) : RecyclerView.Adapter<AddOutcomeAdapter.AddOutcomeViewholder>() {

    class AddOutcomeViewholder(var binding: InventorySaleItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddOutcomeViewholder {
        val binding =
            InventorySaleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return AddOutcomeViewholder(binding)
    }

    override fun getItemCount() = inventoryItems.size

    override fun onBindViewHolder(holder: AddOutcomeViewholder, position: Int) {
        val currentItem = inventoryItems[position]

        holder.binding.apply {
            tvItemName.text = currentItem.itemName
            tvItemStock.text = currentItem.itemQty.toString()
            itemPrice.text = buildString {
                append("@")
                append(
                    holder.binding.root.context.getString(
                        R.string.money_format,
                        currentItem.itemPrice
                    )
                )
            }

            btAddtoSale.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(holder.itemView.context)
                val inflater = LayoutInflater.from(holder.itemView.context)
                val dialogView = inflater.inflate(R.layout.dialog_input_quantity_price, null)

                val editTextBuyPrice =
                    dialogView.findViewById<TextInputEditText>(R.id.edit_itemBuyPrice_dialog)
                val editTextQuantity =
                    dialogView.findViewById<TextInputEditText>(R.id.edit_OutQty_dialog)


                dialogBuilder.setView(dialogView)
                    .setTitle("Masukkan Harga Beli dan Jumlah Barang")
                    .setPositiveButton("Tambahkan") {dialog, _ ->
                        val quantityText = editTextQuantity.text.toString().trim()
                        val buyPriceText = editTextBuyPrice.text.toString().trim()

                        if (quantityText.isEmpty() || buyPriceText.isEmpty()) {
                            Toast.makeText(
                                holder.itemView.context,
                                "Harga Beli dan Jumlah barang tidak boleh kosong!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val quantity = quantityText.toIntOrNull()
                            val buyPrice = buyPriceText.toDoubleOrNull()
                            if (quantity != null && quantity > 0 && buyPrice != null && buyPrice > 0.0) {
                                outcomeViewmodel.addItemToBag(
                                    currentItem,
                                    quantity,
                                    buyPrice
                                )
                            } else {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Masukkan Harga beli dan Jumlah yang Valid",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Batal") {dialog, _ ->
                        dialog.dismiss()
                    }
                val dialog = dialogBuilder.create()
                dialog.show()

            }
        }
    }
}