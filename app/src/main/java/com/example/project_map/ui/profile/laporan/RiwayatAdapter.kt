package com.example.project_map.ui.profile.laporan

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.CatatanKeuangan // <-- Diubah
import com.example.project_map.data.TipeCatatan // <-- Diubah
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RiwayatAdapter(private var transactions: List<CatatanKeuangan>) : // <-- Diubah
    RecyclerView.Adapter<RiwayatAdapter.RiwayatViewHolder>() { // <-- Diubah

    class RiwayatViewHolder(view: View) : RecyclerView.ViewHolder(view) { // <-- Diubah
        val tvDate: TextView = view.findViewById(R.id.tvRiwayatTanggal)
        val tvDesc: TextView = view.findViewById(R.id.tvRiwayatDeskripsi)
        val tvAmount: TextView = view.findViewById(R.id.tvRiwayatJumlah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder { // <-- Diubah
        // Menggunakan layout item_riwayat.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat, parent, false) // <-- Diubah
        return RiwayatViewHolder(view) // <-- Diubah
    }

    override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) { // <-- Diubah
        val catatan = transactions[position] // <-- Diubah

        // Format Tanggal
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        holder.tvDate.text = dateFormat.format(catatan.date) // <-- Diubah

        holder.tvDesc.text = catatan.description // <-- Diubah

        // Format Mata Uang
        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0

        when (catatan.type) { // <-- Diubah
            TipeCatatan.SIMPANAN -> { // <-- Diubah
                holder.tvAmount.text = "+ ${format.format(catatan.amount)}" // <-- Diubah
                holder.tvAmount.setTextColor(Color.parseColor("#1E8E3E")) // Hijau
            }
            TipeCatatan.ANGSURAN -> { // <-- Diubah
                holder.tvAmount.text = "- ${format.format(catatan.amount)}" // <-- Diubah
                holder.tvAmount.setTextColor(Color.parseColor("#D93025")) // Merah
            }
            TipeCatatan.PINJAMAN -> { // <-- Diubah
                holder.tvAmount.text = "+ ${format.format(catatan.amount)}" // <-- Diubah
                holder.tvAmount.setTextColor(Color.parseColor("#1A73E8")) // Biru
            }
        }
    }

    override fun getItemCount() = transactions.size

    // Fungsi untuk update data di adapter
    fun updateData(newTransactions: List<CatatanKeuangan>) { // <-- Diubah
        transactions = newTransactions
        notifyDataSetChanged()
    }
}