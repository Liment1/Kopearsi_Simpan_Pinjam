package com.example.project_map.ui.savings

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R

class TransactionAdapter(
    private var transaksi: List<Transaction>,
    private val onClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKeterangan: TextView = view.findViewById(R.id.tvKeterangan)
        val tvJumlah: TextView = view.findViewById(R.id.tvJumlah)
        val ivProof: ImageView = view.findViewById(R.id.ivProof)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = transaksi.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = transaksi[position]
        holder.tvKeterangan.text = item.keterangan
        holder.tvJumlah.text = item.jumlah

        // Cek apakah jenis simpanan adalah "Simpanan Sukarela"
        if (item.keterangan == "Simpanan Sukarela") {
            // Jika ya, buat ImageView terlihat dan atur gambarnya
            holder.ivProof.visibility = View.VISIBLE
            if (item.imageUri.isNullOrEmpty()) {
                holder.ivProof.setImageResource(R.drawable.placeholder_image)
            } else {
                holder.ivProof.setImageURI(Uri.parse(item.imageUri))
            }
        } else {
            // Jika bukan (Pokok atau Wajib), sembunyikan ImageView
            holder.ivProof.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    fun updateList(newList: List<Transaction>) {
        transaksi = newList
        notifyDataSetChanged()
    }
}