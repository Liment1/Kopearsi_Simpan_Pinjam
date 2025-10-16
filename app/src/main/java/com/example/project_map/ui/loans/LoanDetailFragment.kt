package com.example.project_map.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class LoanDetailFragment : Fragment() {

    private lateinit var txtNominal: TextView
    private lateinit var txtSisa: TextView
    private lateinit var txtTenor: TextView
    private lateinit var txtTujuan: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtAlasan: TextView
    private lateinit var edtBayar: EditText
    private lateinit var btnBayar: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnHapus: ImageButton

    private var loanData: JSONObject? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_loan_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtNominal = view.findViewById(R.id.txtNominal)
        txtSisa = view.findViewById(R.id.txtSisa)
        txtTenor = view.findViewById(R.id.txtTenor)
        txtTujuan = view.findViewById(R.id.txtTujuan)
        txtStatus = view.findViewById(R.id.txtStatus)
        txtAlasan = view.findViewById(R.id.txtAlasan)
        edtBayar = view.findViewById(R.id.edtBayar)
        btnBayar = view.findViewById(R.id.btnBayar)
        btnBack = view.findViewById(R.id.btnBack)
        btnHapus = view.findViewById(R.id.btnHapus)

        btnBack.setOnClickListener { findNavController().popBackStack() }

        val dataString = arguments?.getString("loanData")
        if (dataString.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Data pinjaman tidak ditemukan", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        try {
            loanData = JSONObject(dataString)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Data pinjaman rusak", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        tampilkanDetail()
        btnBayar.setOnClickListener { prosesPembayaran() }
    }

    private fun tampilkanDetail() {
        val loan = loanData ?: return
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        val status = loan.optString("status", "-")
        val alasan = loan.optString("alasanPenolakan", "")

        txtNominal.text = "Nominal: ${formatter.format(loan.optDouble("nominal", 0.0))}"
        txtSisa.text = "Sisa Angsuran: ${formatter.format(loan.optDouble("sisaAngsuran", 0.0))}"
        txtTenor.text = "Tenor: ${loan.optString("tenor", "-")}"
        txtTujuan.text = "Tujuan: ${loan.optString("tujuan", "-")}"
        txtStatus.text = "Status: $status"

        if (status.equals("Ditolak", true) && alasan.isNotEmpty()) {
            txtAlasan.visibility = View.VISIBLE
            txtAlasan.text = "Alasan Penolakan: $alasan"
        } else {
            txtAlasan.visibility = View.GONE
        }

        if (status.equals("Disetujui", true)) {
            edtBayar.isEnabled = true
            btnBayar.isEnabled = true
            btnBayar.alpha = 1f
        } else {
            edtBayar.isEnabled = false
            btnBayar.isEnabled = false
            btnBayar.alpha = 0.5f
            btnBayar.text = when (status.lowercase()) {
                "ditolak" -> "Tidak Dapat Dibayar"
                "proses" -> "Menunggu Persetujuan"
                "lunas" -> "Sudah Lunas"
                else -> "Tidak Tersedia"
            }
        }
    }

    private fun prosesPembayaran() {
        val bayarText = edtBayar.text.toString().replace("[^0-9]".toRegex(), "")
        if (bayarText.isEmpty()) {
            Toast.makeText(requireContext(), "Masukkan jumlah bayar", Toast.LENGTH_SHORT).show()
            return
        }

        val bayar = bayarText.toDouble()
        val loan = loanData ?: return
        val sisa = loan.optDouble("sisaAngsuran", 0.0)
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        if (bayar >= sisa) {
            loan.put("status", "Lunas")
            loan.put("sisaAngsuran", 0.0)
            LoanStorage.updateLoan(requireContext(), loan)
            Toast.makeText(requireContext(), "Pembayaran berhasil! Pinjaman lunas.", Toast.LENGTH_SHORT).show()
            tampilkanDetail()
        } else {
            val newSisa = sisa - bayar
            loan.put("sisaAngsuran", newSisa)
            LoanStorage.updateLoan(requireContext(), loan)
            txtSisa.text = "Sisa Angsuran: ${formatter.format(newSisa)}"
            Toast.makeText(requireContext(), "Pembayaran berhasil! Sisa: ${formatter.format(newSisa)}", Toast.LENGTH_SHORT).show()
        }
    }
}
