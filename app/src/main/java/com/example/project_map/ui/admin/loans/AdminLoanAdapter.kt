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
    private val onActionClick: (Loan, String) -> Unit
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

        // Status Styling Logic
        // Ensure you have a generic drawable or set background color directly
        // Here we assume txtStatus has a shape background we can tint
        val statusBg = holder.txtStatus.background as? GradientDrawable

        when (loan.status) {
            "Proses" -> {
                statusBg?.setColor(Color.parseColor("#FFF8E1")) // Yellow bg
                holder.txtStatus.setTextColor(Color.parseColor("#FBC02D")) // Dark Yellow text
                holder.layoutActions.visibility = View.VISIBLE
            }
            "Disetujui", "Pinjaman Berjalan" -> {
                statusBg?.setColor(Color.parseColor("#E8F5E9")) // Green bg
                holder.txtStatus.setTextColor(Color.parseColor("#388E3C")) // Green text
                holder.layoutActions.visibility = View.GONE
            }
            "Ditolak" -> {
                statusBg?.setColor(Color.parseColor("#FFEBEE")) // Red bg
                holder.txtStatus.setTextColor(Color.parseColor("#D32F2F")) // Red text
                holder.layoutActions.visibility = View.GONE
            }
            "Lunas" -> {
                statusBg?.setColor(Color.parseColor("#E0F7FA")) // Cyan bg
                holder.txtStatus.setTextColor(Color.parseColor("#0097A7")) // Cyan text
                holder.layoutActions.visibility = View.GONE
            }
        }

        // Show Rejection Reason if applicable
        if (loan.status == "Ditolak" && loan.alasanPenolakan.isNotEmpty()) {
            holder.txtAlasan.visibility = View.VISIBLE
            holder.txtAlasan.text = "Alasan: ${loan.alasanPenolakan}"
        } else {
            holder.txtAlasan.visibility = View.GONE
        }

        // Button Listeners
        holder.btnTerima.setOnClickListener { onActionClick(loan, "terima") }
        holder.btnTolak.setOnClickListener { onActionClick(loan, "tolak") }
    }

    override fun getItemCount(): Int = loans.size

    fun updateList(newLoans: List<Loan>) {
        loans = newLoans
        notifyDataSetChanged()
    }
}