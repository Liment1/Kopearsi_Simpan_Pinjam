package com.example.project_map.ui.admin.installments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Installment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class AdminInstallmentAdapter(
    private var items: List<Installment>
) : RecyclerView.Adapter<AdminInstallmentAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // Reusing IDs from 'item_simpanan.xml' as requested
        val tvKeterangan: TextView = v.findViewById(R.id.tvKeterangan)
        val tvTanggal: TextView = v.findViewById(R.id.tvTanggal)
        val tvJumlah: TextView = v.findViewById(R.id.tvJumlah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_simpanan, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        currencyFormat.maximumFractionDigits = 0
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))

        // Bind data from Installment.kt
        holder.tvKeterangan.text = "Angsuran Ke-${item.bulanKe}"
        holder.tvJumlah.text = currencyFormat.format(item.jumlahBayar)

        // Use Payment Date (tanggalBayar) if available, else Due Date (jatuhTempo)
        val displayDate = item.tanggalBayar ?: item.jatuhTempo
        holder.tvTanggal.text = if (displayDate != null) dateFormat.format(displayDate) else "-"
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<Installment>) {
        items = newItems
        notifyDataSetChanged()
    }
}