package com.example.project_map.ui.user.loans.Installments

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Installment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale


class UserInstallmentAdapter(private val list: List<Installment>) : RecyclerView.Adapter<UserInstallmentAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val tvNumber: TextView = v.findViewById(R.id.tvInstallmentNumber)
        val tvAmount: TextView = v.findViewById(R.id.tvTotalPaid)
        val tvStatus: TextView = v.findViewById(R.id.tvInstallmentType)
        val tvDate: TextView = v.findViewById(R.id.tvPaymentDate)
        val ivIcon: ImageView = v.findViewById(R.id.ivTimelineIcon)
        // Helper to change background color if needed
        val cardContainer: View = v.findViewById(R.id.rootCard) // Make sure CardView has this ID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_installment, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = list[position]
        val context = holder.itemView.context
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))

        holder.tvNumber.text = "#${item.bulanKe}"
        holder.tvAmount.text = fmt.format(item.jumlahBayar)

        // --- STATUS STYLING ---
        when (item.status) {
            "Lunas" -> {
                holder.tvStatus.text = "Lunas"
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
                holder.ivIcon.setColorFilter(Color.parseColor("#4CAF50"))
                holder.ivIcon.setImageResource(R.drawable.ic_check_circle) // Change icon if you have checkmark
                val datePaid = if (item.tanggalBayar != null) sdf.format(item.tanggalBayar) else "-"
                holder.tvDate.text = "Dibayar: $datePaid"
            }
            "Menunggu Konfirmasi" -> {
                holder.tvStatus.text = "Menunggu Konfirmasi"
                holder.tvStatus.setTextColor(Color.parseColor("#FFA000")) // Orange
                holder.ivIcon.setColorFilter(Color.parseColor("#FFA000"))
                holder.tvDate.text = "Sedang diproses admin"
            }
            else -> { // Belum Bayar
                val isLate = item.status == "Telat"
                holder.tvStatus.text = if(isLate) "Telat Bayar" else "Belum Dibayar"
                holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")) // Red
                holder.ivIcon.setColorFilter(Color.parseColor("#D32F2F"))
                val dueDate = if (item.jatuhTempo != null) sdf.format(item.jatuhTempo) else "-"
                holder.tvDate.text = "Jatuh Tempo: $dueDate"
            }
        }
        // Removing click listener logic entirely
    }

    override fun getItemCount() = list.size
}