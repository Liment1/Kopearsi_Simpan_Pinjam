package com.example.project_map.ui.loans

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

class AdminLoanListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminLoanAdapter
    private lateinit var loans: MutableList<Loan>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_loan_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerAdminLoan)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 🔹 Ambil semua data loan dari penyimpanan (List<JSONObject>)
        val jsonList: List<JSONObject> = LoanStorage.getAllLoans(requireContext())

        // 🔹 Konversi ke List<Loan>
        loans = jsonList.mapIndexed { index, obj ->
            Loan(
                id = obj.optLong("id", index.toLong()),
                namaPeminjam = obj.optString("namaPeminjam"),
                nominal = obj.optDouble("nominal"),
                tenor = obj.optString("tenor"),
                tujuan = obj.optString("tujuan"),
                status = obj.optString("status"),
                bunga = obj.optDouble("bunga"),
                sisaAngsuran = obj.optDouble("sisaAngsuran"),
                totalDibayar = obj.optDouble("totalDibayar"),
                alasanPenolakan = obj.optString("alasanPenolakan", "")
            )
        }.toMutableList()

        // 🔹 Inisialisasi adapter
        adapter = AdminLoanAdapter(loans) { loan, action ->
            when (action) {
                "terima" -> {
                    loan.status = "Disetujui"
                    LoanStorage.updateLoan(requireContext(), loan.toJson())
                    Snackbar.make(view, "Pinjaman disetujui!", Snackbar.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                }

                "tolak" -> showRejectDialog(loan)
                "hapus" -> showDeleteDialog(loan)
            }
        }

        recyclerView.adapter = adapter
    }

    private fun showRejectDialog(loan: Loan) {
        val editText = EditText(requireContext())
        editText.hint = "Masukkan alasan penolakan"

        AlertDialog.Builder(requireContext())
            .setTitle("Tolak Pinjaman")
            .setMessage("Masukkan alasan mengapa pinjaman ini ditolak:")
            .setView(editText)
            .setPositiveButton("Tolak") { _, _ ->
                val alasan = editText.text.toString().ifEmpty { "Tidak ada alasan" }
                loan.status = "Ditolak"
                loan.alasanPenolakan = alasan
                LoanStorage.updateLoan(requireContext(), loan.toJson())
                adapter.notifyDataSetChanged()
                Snackbar.make(requireView(), "Pinjaman ditolak", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteDialog(loan: Loan) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pinjaman")
            .setMessage("Apakah kamu yakin ingin menghapus pengajuan ini?")
            .setPositiveButton("Ya") { _: DialogInterface, _: Int ->
                LoanStorage.deleteLoan(requireContext(), loan.id)
                loans.remove(loan)
                adapter.notifyDataSetChanged()
                Snackbar.make(requireView(), "Pinjaman dihapus", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
