package com.example.project_map.ui.loans

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// FIXED: Data Class for Firestore Mapping
// Ensure this is accessible to LoanDetailFragment
data class Installment(
    var number: Int = 0,
    var type: String = "",
    var amount: Double = 0.0,
    var date: Date? = null,
    var otherFees: Double = 0.0,
    @field:JvmField // Helps Firestore map "isPaid" correctly
    var isPaid: Boolean = false
)

class LoanAdapter(private var installments: List<Installment>) : RecyclerView.Adapter<LoanAdapter.ViewHolder>() {

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
        val localeID = Locale("in", "ID")
        val currencyFormat = NumberFormat.getCurrencyInstance(localeID).apply { maximumFractionDigits = 0 }
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", localeID)

        holder.number.text = "#${item.number}"
        holder.fees.text = "Biaya Lain\n${currencyFormat.format(item.otherFees)}"

        if (item.isPaid) {
            // --- PAID STYLE (Green) ---
            holder.icon.setImageResource(R.drawable.ic_check_circle) // Make sure this drawable exists
            holder.icon.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_green_dark))

            holder.totalPaid.text = "Total Yang Dibayar\n${currencyFormat.format(item.amount)}"
            holder.type.text = "${item.type}\n${currencyFormat.format(item.amount)}"

            // Safety check for date
            val paidDate = if(item.date != null) dateFormat.format(item.date!!) else "-"
            holder.date.text = "Dibayar pada $paidDate"

            holder.totalPaid.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        } else {
            // --- UPCOMING STYLE (Yellow/Gray) ---
            holder.icon.setImageResource(R.drawable.ic_access_time) // Make sure this drawable exists
            holder.icon.setColorFilter(ContextCompat.getColor(context, R.color.orange_text))

            holder.totalPaid.text = "Total Yang Harus Dibayar\n${currencyFormat.format(item.amount)}"
            holder.type.text = "${item.type}\n${currencyFormat.format(item.amount)}"

            val futureDate = item.date
            val dateStr = if(futureDate != null) dateFormat.format(futureDate) else "Segera"
            holder.date.text = "Jatuh tempo pada $dateStr"

            holder.totalPaid.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        }
    }

    override fun getItemCount() = installments.size

    fun updateData(newData: List<Installment>) {
        installments = newData
        notifyDataSetChanged()
    }
}