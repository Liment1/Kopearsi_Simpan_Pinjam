package com.example.project_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.PaymentHistory

class PaymentHistoryAdapter(
    private val items: List<PaymentHistory>
) : RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKeterangan: TextView = view.findViewById(R.id.tvKeterangan)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal)
        val tvJumlah: TextView = view.findViewById(R.id.tvJumlah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvKeterangan.text = item.keterangan
        holder.tvTanggal.text = item.tanggal
        holder.tvJumlah.text = item.jumlah
    }

    override fun getItemCount(): Int = items.size
}
