package com.example.project_map.ui.loans

import android.graphics.Color
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.util.*

class AdminLoanAdapter(
    private val loans: MutableList<Loan>,
    private val onActionClick: (Loan, String) -> Unit
) : RecyclerView.Adapter<AdminLoanAdapter.LoanViewHolder>() {

    class LoanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: MaterialCardView = view.findViewById(R.id.cardContainer)
        val txtNama: TextView = view.findViewById(R.id.txtNamaPeminjam)
        val txtNominal: TextView = view.findViewById(R.id.txtNominal)
        val txtTenor: TextView = view.findViewById(R.id.txtTenor)
        val txtTujuan: TextView = view.findViewById(R.id.txtTujuan)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val txtAlasan: TextView = view.findViewById(R.id.txtAlasan)
        val btnTerima: Button = view.findViewById(R.id.btnTerima)
        val btnTolak: Button = view.findViewById(R.id.btnTolak)
        val btnHapus: ImageButton = view.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_loan, parent, false)
        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loans[position]
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.txtNama.text = loan.namaPeminjam
        holder.txtNominal.text = "Nominal: ${formatter.format(loan.nominal)}"
        holder.txtTenor.text = "Tenor: ${loan.tenor}"
        holder.txtTujuan.text = "Tujuan: ${loan.tujuan}"
        holder.txtStatus.text = "Status: ${loan.status}"

        if (loan.status == "Ditolak" && loan.alasanPenolakan.isNotEmpty()) {
            holder.txtAlasan.visibility = View.VISIBLE
            holder.txtAlasan.text = "Alasan: ${loan.alasanPenolakan}"
        } else {
            holder.txtAlasan.visibility = View.GONE
        }

        when (loan.status) {
            "Proses" -> holder.container.setCardBackgroundColor(Color.parseColor("#FFF59D"))
            "Disetujui" -> holder.container.setCardBackgroundColor(Color.parseColor("#90CAF9"))
            "Ditolak" -> holder.container.setCardBackgroundColor(Color.parseColor("#EF9A9A"))
            "Lunas" -> holder.container.setCardBackgroundColor(Color.parseColor("#A5D6A7"))
            else -> holder.container.setCardBackgroundColor(Color.WHITE)
        }

        holder.btnTerima.setOnClickListener { onActionClick(loan, "terima") }
        holder.btnTolak.setOnClickListener { onActionClick(loan, "tolak") }
        holder.btnHapus.setOnClickListener { onActionClick(loan, "hapus") }

        val isProses = loan.status == "Proses"
        holder.btnTerima.isEnabled = isProses
        holder.btnTolak.isEnabled = isProses
    }

    override fun getItemCount(): Int = loans.size
}
