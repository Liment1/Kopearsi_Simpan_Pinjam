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

        // --- STYLING (Green Theme) ---
        // Progress Bar
        val progress = if (item.nominal > 0) ((item.totalDibayar / (item.nominal + (item.nominal * item.bunga))) * 100).toInt() else 0
        holder.b.progressLoan.progress = progress
        holder.b.progressLoan.progressTintList = ColorStateList.valueOf(Color.parseColor("#43A047")) // Green

        // Status Colors
        when (item.status.lowercase()) {
            "proses" -> {
                holder.b.tvLoanStatus.setTextColor(ContextCompat.getColor(context, R.color.orange_text))
                holder.b.tvLoanStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
            }
            "disetujui", "berjalan" -> {
                holder.b.tvLoanStatus.setTextColor(Color.parseColor("#2E7D32")) // Professional Green
                holder.b.tvLoanStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
            }
            else -> {
                holder.b.tvLoanStatus.setTextColor(Color.GRAY)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = loans.size

    fun submitList(newList: List<Loan>) {
        loans = newList
        notifyDataSetChanged()
    }
}