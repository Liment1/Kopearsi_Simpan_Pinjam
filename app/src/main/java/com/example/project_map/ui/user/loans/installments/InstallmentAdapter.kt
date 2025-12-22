package com.example.project_map.ui.user.loans.installments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Installment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class InstallmentAdapter(
    private var items: List<Installment>,
    private val onClick: (Installment) -> Unit
) : RecyclerView.Adapter<InstallmentAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvInstallmentTitle)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDueDate)
        val chipStatus: Chip = view.findViewById(R.id.chipStatus)
        val rootCard: MaterialCardView = view.findViewById(R.id.rootCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_installment, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        // FIX: Use 'bulanKe' from your model
        holder.tvTitle.text = "Angsuran Ke-${item.bulanKe}"

        // FIX: Use 'jumlahBayar' from your model
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        currencyFormat.maximumFractionDigits = 0
        holder.tvAmount.text = currencyFormat.format(item.jumlahBayar)

        // FIX: Use 'jatuhTempo' from your model
        if (item.jatuhTempo != null) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
            holder.tvDate.text = "Jatuh Tempo: ${dateFormat.format(item.jatuhTempo)}"
        } else {
            holder.tvDate.text = "Jatuh Tempo: -"
        }

        // FIX: Logic for status
        val isPaid = item.status == "Lunas"
        if (isPaid) {
            holder.chipStatus.text = "Lunas"
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_dark)
            holder.rootCard.strokeColor = ContextCompat.getColor(context, android.R.color.holo_green_light)
        } else {
            holder.chipStatus.text = "Belum Lunas"
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_red_dark)
            holder.rootCard.strokeColor = ContextCompat.getColor(context, android.R.color.transparent)
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    fun updateList(newList: List<Installment>) {
        items = newList
        notifyDataSetChanged()
    }
}