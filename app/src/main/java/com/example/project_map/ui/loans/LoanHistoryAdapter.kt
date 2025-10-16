package com.example.project_map.ui.loans

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R

class LoanHistoryAdapter(
    private val loans: MutableList<Loan>,
    private val onActionClick: (Loan, String) -> Unit
) : RecyclerView.Adapter<LoanHistoryAdapter.LoanViewHolder>() {

    inner class LoanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.cardContainer)
        val txtNama: TextView = view.findViewById(R.id.txtNamaPeminjam)
        val txtNominal: TextView = view.findViewById(R.id.txtNominal)
        val txtTenor: TextView = view.findViewById(R.id.txtTenor)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val txtAlasan: TextView = view.findViewById(R.id.txtAlasan)
        val btnHapus: ImageButton = view.findViewById(R.id.btnHapus)
        val btnBatalkan: Button = view.findViewById(R.id.btnTolak) // bisa reuse tombol
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_loan, parent, false)
        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loans[position]
        holder.txtNama.text = loan.namaPeminjam
        holder.txtNominal.text = "Nominal: Rp ${loan.nominal}"
        holder.txtTenor.text = "Tenor: ${loan.tenor}"
        holder.txtStatus.text = loan.status

        // tampilkan alasan jika ada
        if (loan.alasanPenolakan.isNotEmpty()) {
            holder.txtAlasan.visibility = View.VISIBLE
            holder.txtAlasan.text = "Alasan: ${loan.alasanPenolakan}"
        } else {
            holder.txtAlasan.visibility = View.GONE
        }

        // background warna sesuai status
        when (loan.status) {
            "Proses" -> holder.container.setBackgroundColor(Color.parseColor("#FFF59D"))
            "Disetujui" -> holder.container.setBackgroundColor(Color.parseColor("#90CAF9"))
            "Ditolak", "Dibatalkan" -> holder.container.setBackgroundColor(Color.parseColor("#EF9A9A"))
            "Lunas" -> holder.container.setBackgroundColor(Color.parseColor("#A5D6A7"))
            else -> holder.container.setBackgroundColor(Color.WHITE)
        }

        holder.btnHapus.setOnClickListener { onActionClick(loan, "hapus") }
        holder.btnBatalkan.setOnClickListener { onActionClick(loan, "batalkan") }

        // hanya bisa batalkan jika status Proses
        holder.btnBatalkan.isEnabled = loan.status == "Proses"
    }

    override fun getItemCount(): Int = loans.size
}
