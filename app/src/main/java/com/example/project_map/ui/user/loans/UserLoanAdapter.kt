package com.example.project_map.ui.user.loans

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Loan
import com.example.project_map.databinding.ItemLoanActiveBinding
import java.text.NumberFormat
import java.util.Locale

class UserLoansAdapter(
    private var loans: List<Loan>,
    private val onItemClick: (Loan) -> Unit
) : RecyclerView.Adapter<UserLoansAdapter.VH>() {

    class VH(val b: ItemLoanActiveBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemLoanActiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = loans[position]
        val context = holder.itemView.context
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0

        holder.b.tvLoanTitle.text = "Pinjaman: ${item.tujuan}"
        holder.b.tvLoanAmount.text = "Total: ${format.format(item.nominal)}"
        holder.b.tvPaidAmount.text = format.format(item.totalDibayar)
        holder.b.tvRemainingAmount.text = format.format(item.sisaAngsuran)

        holder.b.tvLoanStatus.text = item.status

        // --- STATUS LOGIC ---
        val statusLower = item.status.lowercase()
        val isAccepted = statusLower == "disetujui" || statusLower == "berjalan"

        // --- GREY OUT LOGIC ---
        if (isAccepted) {
            // Active/Accepted Loan: Normal visibility and Clickable
            holder.itemView.alpha = 1.0f
            holder.itemView.setOnClickListener { onItemClick(item) }

            // Progress Bar Logic (Only relevant for active loans)
            val progress = if (item.nominal > 0) ((item.totalDibayar / (item.nominal + (item.nominal * item.bunga))) * 100).toInt() else 0
            holder.b.progressLoan.progress = progress
            holder.b.progressLoan.progressTintList = ColorStateList.valueOf(Color.parseColor("#43A047")) // Green

            // Active Status Color
            holder.b.tvLoanStatus.setTextColor(Color.parseColor("#2E7D32")) // Professional Green
            holder.b.tvLoanStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))

        } else {
            // Not Accepted (e.g., Proses/Ditolak): Greyed out and Unclickable
            holder.itemView.alpha = 0.5f // Dim the entire card to 50% opacity
            holder.itemView.setOnClickListener(null) // Disable clicking

            // Force Progress to 0 for pending/rejected
            holder.b.progressLoan.progress = 0
            holder.b.progressLoan.progressTintList = ColorStateList.valueOf(Color.GRAY)

            // Special handling for "Proses" to keep it slightly distinct even if dimmed
            if (statusLower == "proses") {
                holder.b.tvLoanStatus.setTextColor(ContextCompat.getColor(context, R.color.orange_text))
                holder.b.tvLoanStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
            } else {
                // Default Grey for others (e.g. Ditolak)
                holder.b.tvLoanStatus.setTextColor(Color.DKGRAY)
                holder.b.tvLoanStatus.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
            }
        }
    }

    override fun getItemCount() = loans.size

    fun submitList(newList: List<Loan>) {
        loans = newList
        notifyDataSetChanged()
    }
}