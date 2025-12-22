package com.example.project_map.ui.admin.loans

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Loan
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.*

class AdminLoanAdapter(
    private var loans: List<Loan>,
    private val onActionClick: (Loan, String) -> Unit // Actions: "terima", "tolak", "detail"
) : RecyclerView.Adapter<AdminLoanAdapter.LoanViewHolder>() {

    class LoanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNama: TextView = view.findViewById(R.id.txtNamaPeminjam)
        val txtNominal: TextView = view.findViewById(R.id.txtNominal)
        val txtTenor: TextView = view.findViewById(R.id.txtTenor)
        val txtTujuan: TextView = view.findViewById(R.id.txtTujuan)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val txtAlasan: TextView = view.findViewById(R.id.txtAlasan)
        val layoutActions: LinearLayout = view.findViewById(R.id.layoutActions)
        val btnTerima: MaterialButton = view.findViewById(R.id.btnTerima)
        val btnTolak: MaterialButton = view.findViewById(R.id.btnTolak)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_loan, parent, false)
        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loans[position]
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0

        holder.txtNama.text = loan.namaPeminjam
        holder.txtNominal.text = formatter.format(loan.nominal)
        holder.txtTenor.text = loan.tenor
        holder.txtTujuan.text = loan.tujuan
        holder.txtStatus.text = loan.status

        // --- ADDED CLICK LISTENER FOR DETAIL ---
        holder.itemView.setOnClickListener {
            onActionClick(loan, "detail")
        }

        // Status Styling
        val statusBg = holder.txtStatus.background as? GradientDrawable
        when (loan.status) {
            "Proses" -> {
                statusBg?.setColor(Color.parseColor("#FFF8E1"))
                holder.txtStatus.setTextColor(Color.parseColor("#FBC02D"))
                holder.layoutActions.visibility = View.VISIBLE
            }
            "Disetujui", "Pinjaman Berjalan" -> {
                statusBg?.setColor(Color.parseColor("#E8F5E9"))
                holder.txtStatus.setTextColor(Color.parseColor("#388E3C"))
                holder.layoutActions.visibility = View.GONE
            }
            "Ditolak" -> {
                statusBg?.setColor(Color.parseColor("#FFEBEE"))
                holder.txtStatus.setTextColor(Color.parseColor("#D32F2F"))
                holder.layoutActions.visibility = View.GONE
            }
            "Lunas" -> {
                statusBg?.setColor(Color.parseColor("#E0F7FA"))
                holder.txtStatus.setTextColor(Color.parseColor("#0097A7"))
                holder.layoutActions.visibility = View.GONE
            }
        }

        if (loan.status == "Ditolak" && loan.alasanPenolakan.isNotEmpty()) {
            holder.txtAlasan.visibility = View.VISIBLE
            holder.txtAlasan.text = "Alasan: ${loan.alasanPenolakan}"
        } else {
            holder.txtAlasan.visibility = View.GONE
        }

        holder.btnTerima.setOnClickListener { onActionClick(loan, "terima") }
        holder.btnTolak.setOnClickListener { onActionClick(loan, "tolak") }

        // Fallback if name is still missing
        holder.txtNama.text = if (loan.namaPeminjam.isNotEmpty()) loan.namaPeminjam else "Nama Tidak Ditemukan"

        // Ensure text color is visible (e.g., Black/Dark Grey)
        holder.txtNama.setTextColor(Color.BLACK)
    }

    override fun getItemCount(): Int = loans.size

    fun updateList(newLoans: List<Loan>) {
        loans = newLoans
        notifyDataSetChanged()
    }
}