package com.train.ramarai.postest.ui.main.outcome.report.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.train.ramarai.postest.R
import com.train.ramarai.postest.data.model.OutcomeReportItem
import com.train.ramarai.postest.databinding.ReportItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class OutcomeReportAdapter(allItems: List<OutcomeReportItem>) :
    RecyclerView.Adapter<OutcomeReportAdapter.OutcomeViewHolder>() {
    private var outcomeReportItem: List<OutcomeReportItem> = allItems

    class OutcomeViewHolder(var binding: ReportItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutcomeViewHolder {
        val binding = ReportItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OutcomeViewHolder(binding)
    }

    override fun getItemCount() = outcomeReportItem.size

    override fun onBindViewHolder(holder: OutcomeViewHolder, position: Int) {
        val currentOutcome = outcomeReportItem[position]

        holder.binding.apply {
            bindOutcomeItem(currentOutcome)
        }
    }

    private fun ReportItemBinding.bindOutcomeItem(currentOutcome: OutcomeReportItem) {
        val context = root.context
        tvTitleType.text = currentOutcome.transactionCategory
        tvTransactionDate.text = formatTimestamp(currentOutcome.transactionDate)
        tvTransactionType.text = currentOutcome.transactionName
        tvTransactionDescription.text = currentOutcome.transactionDescription

        ivIconTransaction.setImageResource(R.drawable.baseline_money_off_24)
        ivIconTransaction.setBackgroundColor(context.getColor(R.color.negative))
        tvTransactionAmount.text =
            context.getString(R.string.money_format_out, currentOutcome.transactionAmount)
        tvTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.negative))

    }

    private fun formatTimestamp(reportDate: Timestamp) : String{
        val date = reportDate.toDate()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }
}