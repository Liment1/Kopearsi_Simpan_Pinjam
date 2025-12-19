package com.example.project_map.ui.user.profile.laporan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R

class UserRiwayatAdapter(private var items: List<UserRiwayatItem>) :
    RecyclerView.Adapter<UserRiwayatAdapter.RiwayatViewHolder>() {

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
        val item = items[position]

        holder.tvDate.text = item.date
        holder.tvDesc.text = item.description
        holder.tvAmount.text = item.amount
        holder.tvAmount.setTextColor(item.color)
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<UserRiwayatItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}