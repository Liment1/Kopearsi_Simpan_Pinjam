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

        holder.tvKeterangan.text = item.type
        holder.tvJumlah.text = item.amountString
        holder.tvDate.text = item.dateString

        if (item.isExpense) {
            // Red color for Payments/Withdrawals
            holder.tvJumlah.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
        } else {
            // Green color for Savings/Deposits
            holder.tvJumlah.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
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