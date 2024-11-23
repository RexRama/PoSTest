package com.train.ramarai.postest.ui.main.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.train.ramarai.postest.R
import com.train.ramarai.postest.data.model.ReportItem
import com.train.ramarai.postest.databinding.ReportItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReportsAdapter(
    private var reports: List<ReportItem>
) : RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {
    class ReportViewHolder(var binding: ReportItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding =
            ReportItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun getItemCount() = reports.size

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val currentReport = reports[position]

        holder.binding.apply {
            bindReportItem(currentReport)
        }
    }

    private fun ReportItemBinding.bindReportItem(report: ReportItem) {
        tvTitleType.text = report.reportTitle
        tvTransactionDate.text = formatTimestamp(report.reportDate)
        tvTransactionType.text = report.reportType
        tvTransactionDescription.text = report.reportDescription
        setTransactionDetails(report)
    }

    private fun formatTimestamp(reportDate: Timestamp) : String{
        val date = reportDate.toDate()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun ReportItemBinding.setTransactionDetails(report: ReportItem) {
        val context = root.context
        when {
            report.reportTitle.contains("Penjualan") -> {
                ivIconTransaction.setImageResource(R.drawable.baseline_attach_money_24)
                ivIconTransaction.setBackgroundColor(context.getColor(R.color.positive))
                tvTransactionAmount.text =
                    context.getString(R.string.money_format_in, report.reportAmount)
                tvTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.positive))
            }

            report.reportTitle.contains("Pengeluaran") -> {
                ivIconTransaction.setImageResource(R.drawable.baseline_money_off_24)
                ivIconTransaction.setBackgroundColor(context.getColor(R.color.negative))
                tvTransactionAmount.text =
                    context.getString(R.string.money_format_out, report.reportAmount)
                tvTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.negative))
            }

            report.reportTitle.contains("Restock") -> {
                ivIconTransaction.setImageResource(R.drawable.baseline_money_off_24)
                ivIconTransaction.setBackgroundColor(context.getColor(R.color.negative))
                tvTransactionAmount.text =
                    context.getString(R.string.money_format_out, report.reportAmount)
                tvTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.negative))
            }

            else -> {
                ivIconTransaction.setImageResource(R.drawable.baseline_inventory_2_24)
                tvTransactionAmount.text =
                    context.getString(R.string.double_formate, report.reportAmount)
                if (report.reportType == "Barang Masuk" || report.reportType == "Stok Baru" || report.reportType.contains(
                        "Restock"
                    ) || report.reportDescription.contains("Barang Masuk")
                ) {
                    ivIconTransaction.setBackgroundColor(context.getColor(R.color.positive))
                    tvTransactionAmount.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.positive
                        )
                    )
                } else {
                    ivIconTransaction.setBackgroundColor(context.getColor(R.color.negative))
                    tvTransactionAmount.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.negative
                        )
                    )
                }
            }
        }
    }


}


