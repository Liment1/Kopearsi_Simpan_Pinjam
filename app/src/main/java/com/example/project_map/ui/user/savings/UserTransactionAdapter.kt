package com.example.project_map.ui.user.savings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.project_map.R
import com.example.project_map.data.model.Savings

// Note: Input is now List<SavingsHistoryItem>
class UserTransactionAdapter(
    private var items: List<UserSavingsHistoryItem>,
    private val onClick: (Savings) -> Unit
) : RecyclerView.Adapter<UserTransactionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKeterangan: TextView = view.findViewById(R.id.tvKeterangan)
        val tvJumlah: TextView = view.findViewById(R.id.tvJumlah)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.tvKeterangan.text = item.type
        holder.tvJumlah.text = item.amountString
        holder.tvDate.text = item.dateString

        // --- NEW STATUS LOGIC ---
        if (item.originalSavings.status == "Pending") {
            // Case 1: PENDING (Yellow/Orange)
            holder.tvDate.text = "Sedang Diproses" // Or keep date and add status text
            holder.tvDate.setTextColor(context.getColor(android.R.color.holo_orange_dark))

            // Optional: You can change the icon tint too
            holder.ivIcon.setColorFilter(context.getColor(android.R.color.holo_orange_dark))

        } else if (item.originalSavings.status == "Ditolak") {
            // Case 2: REJECTED (Red)
            holder.tvDate.text = "Ditolak"
            holder.tvDate.setTextColor(context.getColor(android.R.color.holo_red_dark))

        } else {
            // Case 3: SUCCESS (Default)
            // Reset colors
            holder.tvDate.setTextColor(context.getColor(android.R.color.darker_gray)) // Or your default color
            holder.ivIcon.clearColorFilter()
        }

        // Color for Amount (Expense vs Income)
        if (item.isExpense) {
            holder.tvJumlah.setTextColor(context.getColor(android.R.color.holo_red_dark))
        } else {
            holder.tvJumlah.setTextColor(context.getColor(android.R.color.holo_green_dark))
        }

        // 2. Safe Image Loading with Coil
        if (!item.imageUrl.isNullOrEmpty()) {
            holder.ivIcon.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_wallet) // Ensure you have this drawable
                error(R.drawable.ic_wallet)
            }
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_wallet)
        }

        // 3. Handle Click (Pass the original transaction object back)
        holder.itemView.setOnClickListener {
            onClick(item.originalSavings)
        }
    }

    fun updateList(newList: List<UserSavingsHistoryItem>) {
        items = newList
        notifyDataSetChanged()
    }
}