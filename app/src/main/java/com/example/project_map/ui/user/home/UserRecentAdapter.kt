package com.example.project_map.ui.user.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.databinding.ItemRecentActivityBinding

// The 'RecentActivity' data class is now imported from its own file.

class UserRecentAdapter(private val items: List<UserRecentItem>) :
    RecyclerView.Adapter<UserRecentAdapter.VH>() {

    class VH(val b: ItemRecentActivityBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemRecentActivityBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.tvTitle.text = item.title
        holder.b.tvDate.text = item.date
        holder.b.tvAmount.text = item.amount
    }

    override fun getItemCount() = items.size
}