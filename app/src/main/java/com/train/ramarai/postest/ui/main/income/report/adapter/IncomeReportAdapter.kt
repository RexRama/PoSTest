package com.train.ramarai.postest.ui.main.income.report.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.train.ramarai.postest.R
import com.train.ramarai.postest.databinding.ReportItemBinding
import com.train.ramarai.postest.data.model.IncomeReportItem
import java.text.SimpleDateFormat
import java.util.Locale

class IncomeReportAdapter(
    allItems: List<IncomeReportItem>
) : RecyclerView.Adapter<IncomeReportAdapter.IncomeViewHolder>() {
    private var incomeReportItems: List<IncomeReportItem> = allItems
    class IncomeViewHolder(var binding: ReportItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val binding = ReportItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IncomeViewHolder(binding)
    }

    override fun getItemCount() = incomeReportItems.size

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val currentIncome = incomeReportItems[position]

        holder.binding.apply {
            bindIncomeItem(currentIncome)
        }
    }

    private fun ReportItemBinding.bindIncomeItem(income: IncomeReportItem) {
        val context = root.context
        tvTitleType.text = income.transactionCategory
        tvTransactionDate.text = formatTimestamp(income.transactionDate)
        tvTransactionType.text = income.transactionName
        tvTransactionDescription.text = income.transactionDescription

        ivIconTransaction.setImageResource(R.drawable.baseline_attach_money_24)
        ivIconTransaction.setBackgroundColor(context.getColor(R.color.positive))
        tvTransactionAmount.text =
            context.getString(R.string.money_format_in, income.transactionAmount)
        tvTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.positive))
    }

    private fun formatTimestamp(reportDate: Timestamp) : String{
        val date = reportDate.toDate()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }


}