package com.example.project_map.ui.admin.installments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Installment
import com.example.project_map.databinding.ItemAdminInstallmentBinding // Ensure you have this XML
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class AdminInstallmentAdapter(
    private var list: List<Installment>,
    private val onItemClick: (Installment) -> Unit // Simple click listener
) : RecyclerView.Adapter<AdminInstallmentAdapter.VH>() {

    class VH(val b: ItemAdminInstallmentBinding) : RecyclerView.ViewHolder(b.root)

    fun updateList(newList: List<Installment>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAdminInstallmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        val formatRp = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }
        val formatDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))

        // 1. Title: User Name & Loan Type
        holder.b.tvMonth.text = if (item.peminjamName.isNotEmpty()) item.peminjamName else "Angsuran Ke-${item.bulanKe}"

        // 2. Amount & Date
        holder.b.tvAmount.text = formatRp.format(item.jumlahBayar)

        // Show Payment Date since it's "Lunas"
        val dateText = item.tanggalBayar?.let { formatDate.format(it) } ?: "Tanggal tidak tercatat"
        holder.b.tvStatus.text = "Lunas • $dateText"
        holder.b.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green)) // Example color

        // 3. Click Action
        holder.itemView.setOnClickListener { onItemClick(item) }

        // Hide buttons if they still exist in your XML
        holder.b.btnApprove.visibility = android.view.View.GONE
        holder.b.btnReject.visibility = android.view.View.GONE
        holder.b.btnProof.visibility = android.view.View.GONE
    }

    override fun getItemCount() = list.size
}