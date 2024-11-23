package com.train.ramarai.postest.ui.main.outcome.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.train.ramarai.postest.data.model.InOutInventoryItems
import com.train.ramarai.postest.databinding.SaleOutItemBinding
import com.train.ramarai.postest.ui.main.outcome.OutcomeViewmodel

class BsOutcomeAdapter(
    private var itemInBag: MutableList<InOutInventoryItems>,
    private var outcomeViewmodel: OutcomeViewmodel
) : RecyclerView.Adapter<BsOutcomeAdapter.BsOutcomeViewHolder>() {
    class BsOutcomeViewHolder(var binding: SaleOutItemBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BsOutcomeViewHolder {
        val binding = SaleOutItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return BsOutcomeViewHolder(binding)
    }

    override fun getItemCount() = itemInBag.size

    override fun onBindViewHolder(holder: BsOutcomeViewHolder, position: Int) {
        val currentItemInBag = itemInBag[position]

        holder.binding.apply {
            tvItemName.text = currentItemInBag.itemName
            tvItemQty.text = currentItemInBag.itemQtyUpdate.toString()
            tvItemBrand.text = currentItemInBag.itemBrand

            val totalPrice = currentItemInBag.itemQtyUpdate * currentItemInBag.itemPrice

            root.setOnLongClickListener {
                val currentOutcomeItem = outcomeViewmodel.getItemsInBag().firstOrNull {it.itemUID == currentItemInBag.itemUID}
                currentOutcomeItem?.let { outcomeItem ->
                    outcomeViewmodel.removeItemFromBag(outcomeItem)
                }
                notifyItemRemoved(position)
                true
            }
        }
    }
}