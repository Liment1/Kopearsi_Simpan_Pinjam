package com.example.project_map.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.databinding.ItemRecentActivityBinding

data class RecentActivity(val title: String, val date: String, val amount: String)

class RecentActivityAdapter(private val items: List<RecentActivity>) :
    RecyclerView.Adapter<RecentActivityAdapter.VH>() {

    class VH(val b: ItemRecentActivityBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemRecentActivityBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val it = items[position]
        holder.b.tvTitle.text = it.title
        holder.b.tvDate.text = it.date
        holder.b.tvAmount.text = it.amount
    }

    override fun getItemCount() = items.size
}
