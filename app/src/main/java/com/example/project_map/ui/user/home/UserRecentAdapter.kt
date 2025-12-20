package com.example.project_map.ui.user.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.databinding.ItemRecentActivityBinding

class UserRecentAdapter(private val items: List<UserRecentItem>) :
    RecyclerView.Adapter<UserRecentAdapter.VH>() {

    class VH(val b: ItemRecentActivityBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemRecentActivityBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.b.tvTitle.text = item.title
        holder.b.tvDate.text = item.date
        holder.b.tvAmount.text = item.amount

        // Dynamic Styling
        when (item.type) {
            TransactionType.SAVINGS -> {
                holder.b.imgIcon.setImageResource(R.drawable.ic_wallet)
                holder.b.imgIcon.setColorFilter(Color.parseColor("#2E7D32")) // Green
                holder.b.tvAmount.setTextColor(Color.parseColor("#2E7D32"))
            }
            TransactionType.LOAN -> {
                holder.b.imgIcon.setImageResource(R.drawable.ic_loan)
                holder.b.imgIcon.setColorFilter(Color.parseColor("#1565C0")) // Blue
                holder.b.tvAmount.setTextColor(Color.parseColor("#1565C0"))
            }
            TransactionType.WITHDRAWAL -> {
                holder.b.imgIcon.setImageResource(R.drawable.ic_wallet) // Need icon
                holder.b.imgIcon.setColorFilter(Color.parseColor("#C62828")) // Red
                holder.b.tvAmount.setTextColor(Color.parseColor("#C62828"))
            }
            TransactionType.EXPENSE -> {
                holder.b.imgIcon.setImageResource(R.drawable.ic_installments)
                holder.b.imgIcon.setColorFilter(Color.parseColor("#C62828")) // Red
                holder.b.tvAmount.setTextColor(Color.parseColor("#C62828"))
            }
        }
    }

    override fun getItemCount() = items.size
}