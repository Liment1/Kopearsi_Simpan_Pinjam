package com.example.project_map.ui.admin.notification

import android.R
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.data.model.Notification
import java.text.SimpleDateFormat
import java.util.*

class AdminAnnouncementAdapter(private var list: List<Notification>) :
    RecyclerView.Adapter<AdminAnnouncementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text1)
        val subtitle: TextView = view.findViewById(R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // Handle Urgent Title
        if (item.isUrgent) {
            holder.title.text = "[URGENT] ${item.title}"
            holder.title.setTextColor(Color.RED)
            holder.title.setTypeface(null, Typeface.BOLD)
        } else {
            holder.title.text = item.title
            holder.title.setTextColor(Color.BLACK)
            holder.title.setTypeface(null, Typeface.NORMAL)
        }

        // Handle Date (Using 'date' field from screenshot)
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val dateStr = item.date?.toDate()?.let { dateFormat.format(it) } ?: "Just Now"

        holder.subtitle.text = "$dateStr • ${item.message}"
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Notification>) {
        list = newList
        notifyDataSetChanged()
    }
}