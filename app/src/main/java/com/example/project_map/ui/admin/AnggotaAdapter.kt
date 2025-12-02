package com.example.project_map.ui.admin

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.UserData

class AnggotaAdapter(
    private var anggotaList: List<UserData>,
    private val onItemClick: (UserData) -> Unit // <-- Tambahkan parameter ini
) : RecyclerView.Adapter<AnggotaAdapter.AnggotaViewHolder>() {

    // ... (class AnggotaViewHolder tetap sama) ...
    class AnggotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaAnggota)
        val tvId: TextView = view.findViewById(R.id.tvIdAnggota)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusAnggota)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnggotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anggota, parent, false)
        return AnggotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnggotaViewHolder, position: Int) {
        val anggota = anggotaList[position]
        holder.tvNama.text = anggota.name
//        holder.tvId.text = "ID: ${anggota.id}"
        holder.tvStatus.text = anggota.status

        val statusBackground = holder.tvStatus.background as GradientDrawable
        statusBackground.setColor(getStatusColor(anggota.status))

        // ▼▼▼ Tambahkan listener klik di sini ▼▼▼
        holder.itemView.setOnClickListener { onItemClick(anggota) }
    }

    // ... (fungsi getItemCount dan getStatusColor tetap sama) ...
    override fun getItemCount() = anggotaList.size

    private fun getStatusColor(status: String): Int {
        return when (status) {
            "Anggota Aktif" -> Color.parseColor("#1E8E3E") // Hijau
            "Calon Anggota" -> Color.parseColor("#F9AB00") // Kuning
            "Anggota Tidak Aktif" -> Color.parseColor("#5F6368") // Abu-abu
            "Diblokir Sementara" -> Color.parseColor("#E67C73") // Oranye
            "Dikeluarkan" -> Color.parseColor("#D93025") // Merah
            else -> Color.LTGRAY
        }
    }
}