package com.train.ramarai.postest.ui.main.inventory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.train.ramarai.postest.R
import com.train.ramarai.postest.databinding.InventoryItemsBinding
import com.train.ramarai.postest.data.model.InventoryItem
import com.train.ramarai.postest.ui.main.inventory.InventoryViewModel
import com.train.ramarai.postest.utils.DialogUtils

class InventoryAdapter(
    private var items: List<InventoryItem>,
    private val viewModel: InventoryViewModel
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    private var onItemClickCallBack: OnItemClickCallback? = null

    class InventoryViewHolder(var binding: InventoryItemsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding =
            InventoryItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InventoryViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val currentItem = items[position]

        holder.binding.apply {
            tvItemName.text = currentItem.itemName
            tvItemType.text = currentItem.itemType
            tvItemBrand.text = currentItem.itemBrand
            tvItemStock.text = currentItem.itemQty.toString()
            itemPrice.text =
                holder.binding.root.context.getString(R.string.money_format, currentItem.itemPrice)

            btnDeleteStock.setOnClickListener {
                DialogUtils(holder.binding.root.context).dialogDefault(
                    "Hapus Stok",
                    "Perhatian: Menghapus stok ini akan bersifat permanen. Apakah Anda yakin ingin melanjutkan?"
                ){
                    viewModel.deleteInventory(currentItem.id, holder.binding.root.context)
                }
            }

            btUpdateStock.setOnClickListener {
                onItemClickCallBack?.onItemClicked(currentItem)
            }
        }
    }



    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallBack = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: InventoryItem)
    }
}