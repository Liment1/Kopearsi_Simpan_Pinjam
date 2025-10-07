package com.example.project_map.ui.savings

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R

class TransactionAdapter(
    private val data: MutableList<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardTransaction)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvKeterangan: TextView = itemView.findViewById(R.id.tvKeterangan)
        val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.tvTanggal.text = item.tanggal
        holder.tvKeterangan.text = item.keterangan
        holder.tvJumlah.text = item.jumlah

        // Set warna teks jumlah berdasarkan tipe transaksi
        if (item.type == TransactionType.WITHDRAWAL) {
            holder.tvJumlah.setTextColor(Color.RED)
        } else {
            holder.tvJumlah.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green_primary))
        }

        // Tambahkan elevation dan corner radius untuk card
        holder.cardView.cardElevation = 4f
        holder.cardView.radius = 12f
    }

    override fun getItemCount(): Int = data.size

    fun addTransaction(transaction: Transaction) {
        data.add(0, transaction)
        notifyItemInserted(0)
    }
}