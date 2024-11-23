package com.train.ramarai.postest.ui.main.income.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.train.ramarai.postest.R
import com.train.ramarai.postest.data.model.InOutInventoryItems
import com.train.ramarai.postest.databinding.SaleItemBinding
import com.train.ramarai.postest.ui.main.income.IncomeViewmodel

class BsAddIncomeAdapter(
    private var itemInBag: MutableList<InOutInventoryItems>,
    private var incomeViewmodel: IncomeViewmodel
) : RecyclerView.Adapter<BsAddIncomeAdapter.BsAddIncomeViewHolder>() {
    class BsAddIncomeViewHolder(var binding: SaleItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BsAddIncomeViewHolder {
        val binding = SaleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return BsAddIncomeViewHolder(binding)
    }

    override fun getItemCount() = itemInBag.size

    override fun onBindViewHolder(holder: BsAddIncomeViewHolder, position: Int) {
        val currentItemInBag = itemInBag[position]

        holder.binding.apply {
            tvItemName.text = currentItemInBag.itemName
            tvItemCount.text = currentItemInBag.itemQtyUpdate.toString()
            tvItemPrice.text = holder.binding.root.context.getString(R.string.money_format, currentItemInBag.itemPrice)
            val totalPrice = currentItemInBag.itemQtyUpdate * currentItemInBag.itemPrice
            tvItemTotalPrice.text = holder.binding.root.context.getString(R.string.money_format, totalPrice)

            root.setOnLongClickListener {
                val currentIncomeItem = incomeViewmodel.getItemsInBag().firstOrNull { it.itemUID == currentItemInBag.itemUID }
                currentIncomeItem?.let { incomeItem ->
                    incomeViewmodel.removeItemFromBag(incomeItem)  // Call remove function
                }
                notifyItemRemoved(position)
                true
            }

        }
    }
}