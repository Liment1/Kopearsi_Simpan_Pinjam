package com.example.project_map.ui.admin.savings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Savings
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class SimpananAdapter(
    private var data: List<Savings>,
    private val onItemClick: (Savings) -> Unit
) : RecyclerView.Adapter<SimpananAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvKeterangan)
        val tvDetails: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
        val ivProof: ImageView = itemView.findViewById(R.id.ivProof)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simpanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0

        // Logic tampilan
        holder.tvName.text = if (item.userName.isNotEmpty()) item.userName else "Member #${item.userId.take(5)}"

        val sdf = SimpleDateFormat("dd MMM", Locale("in", "ID"))
        val dateStr = if(item.date != null) sdf.format(item.date) else "-"
        holder.tvDetails.text = "${item.type} • $dateStr"

        holder.tvJumlah.text = "+ ${format.format(item.amount)}"
        holder.ivProof.visibility = if (item.type == "Simpanan Sukarela" || item.proofUrl.isNotEmpty()) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = data.size

    fun updateList(newData: List<Savings>) {
        data = newData
        notifyDataSetChanged()
    }
}