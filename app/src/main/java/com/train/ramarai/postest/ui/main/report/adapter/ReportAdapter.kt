package com.train.ramarai.postest.ui.main.report.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.train.ramarai.postest.R
import com.train.ramarai.postest.data.model.InOutReportItem
import com.train.ramarai.postest.databinding.FinalReportItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReportAdapter(allItems: List<InOutReportItem>) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {
    private var reportItem: List<InOutReportItem> = allItems

    class ReportViewHolder(var binding: FinalReportItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = FinalReportItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  ReportViewHolder(binding)
    }

    override fun getItemCount() = reportItem.size

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val currentReport = reportItem[position]

        holder.binding.apply {
            bindReportItem(currentReport)
        }
    }

    private fun FinalReportItemBinding.bindReportItem(currentReport: InOutReportItem) {
        val context = root.context
        tvTitleType.text = currentReport.transactionCategory
        tvTransactionDate.text = formatTimestamp(currentReport.transactionDate)
        tvTransactionDescription.text = currentReport.transactionDescription

        if (tvTitleType.text == "Penghasilan") {
            tvTransactionAmountOutcome.visibility = View.GONE
            tvTransactionAmountIncome.text = context.getString(R.string.money_format_in, currentReport.transactionAmount)
            tvTransactionAmountIncome.setTextColor(ContextCompat.getColor(context, R.color.positive))
        } else {
            tvTransactionAmountIncome.visibility = View.GONE
            tvTransactionAmountOutcome.text = context.getString(R.string.money_format_out, currentReport.transactionAmount)
            tvTransactionAmountOutcome.setTextColor(ContextCompat.getColor(context, R.color.negative))
        }
    }

    private fun formatTimestamp(transactionDate: Timestamp): String {
        val date = transactionDate.toDate()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }
}