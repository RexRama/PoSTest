package com.train.ramarai.postest.ui.main.income.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.train.ramarai.postest.R
import com.train.ramarai.postest.data.model.InventoryItem
import com.train.ramarai.postest.databinding.InventorySaleItemBinding
import com.train.ramarai.postest.ui.main.income.IncomeViewmodel

class AddIncomeAdapter(
    private var inventoryItems: List<InventoryItem>,
    private var incomeViewmodel: IncomeViewmodel
) : RecyclerView.Adapter<AddIncomeAdapter.AddIncomeViewHolder>() {

    class AddIncomeViewHolder(var binding: InventorySaleItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddIncomeViewHolder {
        val binding =
            InventorySaleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return AddIncomeViewHolder(binding)
    }

    override fun getItemCount() = inventoryItems.size

    override fun onBindViewHolder(holder: AddIncomeViewHolder, position: Int) {
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
                val dialogView = inflater.inflate(R.layout.dialog_input_quantity, null)

                val editTextQuantity =
                    dialogView.findViewById<TextInputEditText>(R.id.edit_itemQty_dialog)

                dialogBuilder.setView(dialogView)
                    .setTitle("Masukkan Jumlah Barang")
                    .setPositiveButton("Tambahkan") { dialog, _ ->
                        val quantityText = editTextQuantity.text.toString().trim()

                        if (quantityText.isEmpty()) {
                            Toast.makeText(
                                holder.itemView.context,
                                "Jumlah barang tidak boleh kosong!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val quantity = quantityText.toIntOrNull()
                            if (quantity != null && quantity > 0) {
                                if (quantity <= currentItem.itemQty) {
                                    incomeViewmodel.addItemToBag(currentItem, quantity)
                                } else {
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Jumlah stok tidak mencukupi",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Masukkan Jumlah yang Valid",
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
    }

}