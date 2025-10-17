package com.example.project_map.ui.loans

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R

class InstallmentAdapter(private val installments: List<Installment>) : RecyclerView.Adapter<InstallmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivTimelineIcon)
        val number: TextView = view.findViewById(R.id.tvInstallmentNumber)
        val totalPaid: TextView = view.findViewById(R.id.tvTotalPaid)
        val type: TextView = view.findViewById(R.id.tvInstallmentType)
        val date: TextView = view.findViewById(R.id.tvPaymentDate)
        val fees: TextView = view.findViewById(R.id.tvOtherFees)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_installment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = installments[position]
        val context = holder.itemView.context

        holder.number.text = "#${item.number}"
      holder.totalPaid.text = "Total Yang Dibayar\n${item.amount}"
        holder.type.text = "${item.type}\n${item.amount}"
        holder.date.text = item.date
        holder.fees.text = "Biaya Lain\n${item.otherFees}"

        if (item.isPaid) {
            holder.icon.setImageResource(R.drawable.ic_timeline_paid)
            holder.totalPaid.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        } else {
            holder.icon.setImageResource(R.drawable.ic_timeline_pending)
            holder.totalPaid.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        }
    }

    override fun getItemCount() = installments.size
}