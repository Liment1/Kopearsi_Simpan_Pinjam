package com.example.project_map.ui.profile.laporan

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.CatatanKeuangan
import com.example.project_map.data.TipeCatatan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RiwayatAdapter(private var transactions: List<CatatanKeuangan>) :
    RecyclerView.Adapter<RiwayatAdapter.RiwayatViewHolder>() {

    class RiwayatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvRiwayatTanggal)
        val tvDesc: TextView = view.findViewById(R.id.tvRiwayatDeskripsi)
        val tvAmount: TextView = view.findViewById(R.id.tvRiwayatJumlah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat, parent, false)
        return RiwayatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
        val catatan = transactions[position]

        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        holder.tvDate.text = dateFormat.format(catatan.date)
        holder.tvDesc.text = catatan.description

        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0

        when (catatan.type) {
            TipeCatatan.SIMPANAN -> {
                holder.tvAmount.text = "+ ${format.format(catatan.amount)}"
                holder.tvAmount.setTextColor(Color.parseColor("#1E8E3E")) // Green
            }
            TipeCatatan.ANGSURAN -> {
                holder.tvAmount.text = "- ${format.format(catatan.amount)}"
                holder.tvAmount.setTextColor(Color.parseColor("#D93025")) // Red
            }
            TipeCatatan.PINJAMAN -> {
                holder.tvAmount.text = "+ ${format.format(catatan.amount)}"
                holder.tvAmount.setTextColor(Color.parseColor("#1A73E8")) // Blue
            }
            TipeCatatan.OPERASIONAL -> {
                holder.tvAmount.text = "- ${format.format(catatan.amount)}"
                holder.tvAmount.setTextColor(Color.parseColor("#D93025")) // Red
            }
        }
    }

    override fun getItemCount() = transactions.size

    fun updateData(newTransactions: List<CatatanKeuangan>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}
