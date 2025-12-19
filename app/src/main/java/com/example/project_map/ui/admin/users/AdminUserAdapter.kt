package com.example.project_map.ui.admin.users

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.UserData

class AdminUserAdapter(
    private var anggotaList: List<UserData>,
    private val onItemClick: (UserData) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.AnggotaViewHolder>() {

    class AnggotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitial: TextView = view.findViewById(R.id.tvInitial)

        val tvNama: TextView = view.findViewById(R.id.tvNamaAnggota)
        val tvId: TextView = view.findViewById(R.id.tvIdAnggota)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusAnggota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnggotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return AnggotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnggotaViewHolder, position: Int) {
        val anggota = anggotaList[position]

        holder.tvNama.text = anggota.name
        // Use memberCode if available, otherwise fallback to ID or empty
        holder.tvId.text = if (anggota.memberCode.isNotEmpty()) anggota.memberCode else "ID: ${anggota.id.take(6)}"
        holder.tvStatus.text = anggota.status

        // 1. Logic for Initial (First letter of name)
        val initial = anggota.name.firstOrNull()?.toString()?.uppercase() ?: "?"
        holder.tvInitial.text = initial

        // 2. Modern Status Styling (Pill Colors)
        // Ensure tvStatusAnggota has 'bg_status_active' set in XML as default background
        val statusBg = holder.tvStatus.background as? GradientDrawable

        when (anggota.status) {
            "Aktif", "Anggota Aktif" -> {
                statusBg?.setColor(Color.parseColor("#E8F5E9")) // Light Green
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")) // Dark Green
            }
            "Calon Anggota" -> {
                statusBg?.setColor(Color.parseColor("#FFF8E1")) // Light Yellow
                holder.tvStatus.setTextColor(Color.parseColor("#F9A825")) // Dark Yellow
            }
            "Tidak Aktif", "Dikeluarkan" -> {
                statusBg?.setColor(Color.parseColor("#FFEBEE")) // Light Red
                holder.tvStatus.setTextColor(Color.parseColor("#C62828")) // Dark Red
            }
            else -> {
                // Default Grey
                statusBg?.setColor(Color.parseColor("#F5F5F5"))
                holder.tvStatus.setTextColor(Color.GRAY)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(anggota) }
    }

    override fun getItemCount() = anggotaList.size
}