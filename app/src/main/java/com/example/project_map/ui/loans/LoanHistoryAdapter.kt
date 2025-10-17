package com.example.project_map.ui.loans

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import java.text.NumberFormat
import java.util.Locale

class LoanHistoryAdapter(
    private val loans: List<Loan>,
    private val onItemClick: (Loan) -> Unit
) : RecyclerView.Adapter<LoanHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivStatusIcon)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val amount: TextView = view.findViewById(R.id.tvLoanAmount)
        val purpose: TextView = view.findViewById(R.id.tvLoanPurpose)
        val date: TextView = view.findViewById(R.id.tvLoanDate)
        val tenor: TextView = view.findViewById(R.id.tvLoanTenor)
        val reason: TextView = view.findViewById(R.id.tvRejectionReason)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loan_history_v2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val loan = loans[position]
        val context = holder.itemView.context
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }

        holder.amount.text = formatter.format(loan.nominal)
        holder.purpose.text = loan.tujuan
        holder.date.text = "20 Januari 2025" // Dummy date for now
        holder.tenor.text = "${loan.tenor}\nTenor"
        holder.status.text = loan.status

        // Set status icon and text color
        when (loan.status.lowercase()) {
            "proses" -> {
                holder.icon.setImageResource(R.drawable.ic_status_pending)
                holder.status.background = ContextCompat.getDrawable(context, R.drawable.status_tag_pending)
//                holder.status.setTextColor(ContextCompat.getColor(context, R.color.orange_text))
            }
            "disetujui" -> {
                holder.icon.setImageResource(R.drawable.ic_status_active)
//                holder.status.background = ContextCompat.getDrawable(context, R.drawable.status_tag_approved)
//                holder.status.setTextColor(ContextCompat.getColor(context, R.color.blue_text))
            }
            "ditolak" -> {
                holder.icon.setImageResource(R.drawable.ic_status_rejected)
//                holder.status.background = ContextCompat.getDrawable(context, R.drawable.status_tag_rejected)
//                holder.status.setTextColor(ContextCompat.getColor(context, R.color.red_text))
            }
            "lunas" -> {
                holder.icon.setImageResource(R.drawable.ic_status_paid)
//                holder.status.background = ContextCompat.getDrawable(context, R.drawable.status_tag_paid)
//                holder.status.setTextColor(ContextCompat.getColor(context, R.color.green_text))
            }
        }

        // Show rejection reason if available
        if (loan.status.equals("Ditolak", true) && loan.alasanPenolakan.isNotEmpty()) {
            holder.reason.visibility = View.VISIBLE
            holder.reason.text = "Alasan Penolakan: ${loan.alasanPenolakan}"
        } else {
            holder.reason.visibility = View.GONE
        }

        // Set click listener to open detail screen
        holder.itemView.setOnClickListener { onItemClick(loan) }
    }

    override fun getItemCount() = loans.size
}