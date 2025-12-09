package com.example.project_map.ui.savings

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transaksi: List<Transaction>,
    private val onClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKeterangan: TextView = view.findViewById(R.id.tvKeterangan)
        val tvJumlah: TextView = view.findViewById(R.id.tvJumlah)
        val tvDate: TextView = view.findViewById(R.id.tvDate)

        // CHANGE THIS LINE: Use R.id.ivIcon (matches your new XML)
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = transaksi.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = transaksi[position]

        // Format Currency
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0

        holder.tvKeterangan.text = item.type
        holder.tvJumlah.text = "+ ${numberFormat.format(item.amount)}"

        // Format Date
        val dateFormat = SimpleDateFormat("dd MMM yyyy", localeID)
        val dateStr = if (item.date != null) dateFormat.format(item.date) else "-"
        holder.tvDate.text = dateStr

        // Handle Image Logic (Thumbnail)
        // If imageUri exists, load it into the icon. Otherwise, keep default icon.
        if (!item.imageUri.isNullOrEmpty()) {
            try {
                holder.ivIcon.setImageURI(Uri.parse(item.imageUri))
                // Make it round or add styling if needed
            } catch (e: Exception) {
                // Ignore, keep default icon
            }
        } else {
            // Reset to default icon in case of recycling views
            holder.ivIcon.setImageResource(R.drawable.ic_savings)
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    fun updateList(newList: List<Transaction>) {
        transaksi = newList
        notifyDataSetChanged()
    }
}