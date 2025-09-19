package com.example.project_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private val data: MutableList<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvKeterangan: TextView = itemView.findViewById(R.id.tvKeterangan)
        val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.tvTanggal.text = item.tanggal
        holder.tvKeterangan.text = item.keterangan
        holder.tvJumlah.text = item.jumlah
    }

    override fun getItemCount(): Int = data.size

    fun addTransaction(transaction: Transaction) {
        data.add(0, transaction) // insert di atas
        notifyItemInserted(0)
    }
}
