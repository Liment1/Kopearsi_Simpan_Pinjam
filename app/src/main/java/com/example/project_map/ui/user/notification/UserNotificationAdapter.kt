package com.example.project_map.ui.user.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Notification
import java.text.SimpleDateFormat
import java.util.Locale

class UserNotificationAdapter(private var items: List<Notification>) :
    RecyclerView.Adapter<UserNotificationAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tvNotifTitle)
        val tvMessage: TextView = v.findViewById(R.id.tvNotifMessage)
        val tvDate: TextView = v.findViewById(R.id.tvNotifDate)
        val viewStatus: View = v.findViewById(R.id.viewStatusColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_notification, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvMessage.text = item.message

        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
        holder.tvDate.text = item.date?.toDate()?.let { sdf.format(it) } ?: "-"

        // Color coding for Urgent items
        if (item.isUrgent) {
            holder.viewStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red)) // Ensure you have this color or use Color.RED
            holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
        } else {
            holder.viewStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.teal_200)) // Or your primary color
            holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<Notification>) {
        items = newItems
        notifyDataSetChanged()
    }
}