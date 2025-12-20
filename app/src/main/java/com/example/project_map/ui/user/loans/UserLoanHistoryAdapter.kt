package com.example.project_map.ui.user.loans

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Loan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class UserLoanHistoryAdapter(
    private val loans: List<Loan>,
    private val onItemClick: (Loan) -> Unit
) : RecyclerView.Adapter<UserLoanHistoryAdapter.ViewHolder>() {

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loan_active, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val loan = loans[position]
        val context = holder.itemView.context
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID).apply { maximumFractionDigits = 0 }
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", localeID)

        holder.amount.text = formatter.format(loan.nominal)
        holder.purpose.text = loan.tujuan
        holder.date.text = if (loan.tanggalPengajuan != null) dateFormatter.format(loan.tanggalPengajuan!!) else "-"
        holder.tenor.text = "${loan.tenor}\nTenor"
        holder.status.text = loan.status

        // Set status icon and text color
        when (loan.status.lowercase()) {
            "proses" -> {
                holder.icon.setImageResource(R.drawable.ic_status_pending)
                holder.status.background = ContextCompat.getDrawable(context, R.drawable.status_tag_pending)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.orange_text))
            }
            "disetujui", "pinjaman berjalan" -> {
                holder.icon.setImageResource(R.drawable.ic_status_active)
                holder.status.background = ContextCompat.getDrawable(context, R.drawable.status_tag_approved)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.blue_text))
            }
            "ditolak" -> {
                holder.icon.setImageResource(R.drawable.ic_status_rejected)
                holder.status.background = ContextCompat.getDrawable(context, R.drawable.status_tag_rejected)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.red_text))
            }
            "lunas" -> {
                holder.icon.setImageResource(R.drawable.ic_status_paid)
                holder.status.background = ContextCompat.getDrawable(context, R.drawable.status_tag_paid)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.green_text))
            }
        }

        if (loan.status.equals("Ditolak", true) && loan.alasanPenolakan.isNotEmpty()) {
            holder.reason.visibility = View.VISIBLE
            holder.reason.text = "Alasan: ${loan.alasanPenolakan}"
        } else {
            holder.reason.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(loan) }
    }

    override fun getItemCount() = loans.size
}