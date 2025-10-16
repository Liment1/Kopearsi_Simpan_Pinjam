package com.example.project_map.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import java.text.NumberFormat
import java.util.Locale

class PinjamanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_pinjaman, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAjukan = view.findViewById<Button>(R.id.btnAjukanPinjaman)
        val btnRiwayat = view.findViewById<Button>(R.id.btnLihatRiwayat)
        val containerPinjaman = view.findViewById<LinearLayout>(R.id.containerPinjamanAktif)

        // Navigasi tombol
        btnAjukan.setOnClickListener {
            findNavController().navigate(R.id.action_pinjamanFragment_to_loansFragment)
        }

        btnRiwayat.setOnClickListener {
            findNavController().navigate(R.id.action_pinjamanFragment_to_loanHistoryFragment)
        }

        // Ambil semua data pinjaman dari storage
        val loans = LoanStorage.getAllLoans(requireContext())
        val activeLoans = loans.filter {
            val status = it.optString("status", "")
            status.equals("disetujui", true) ||
                    status.equals("diterima", true) ||
                    status.equals("aktif", true)
        }

        containerPinjaman.removeAllViews()

        if (activeLoans.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "Kamu belum memiliki pinjaman aktif."
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.darker_gray))
                setPadding(16, 16, 16, 16)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            containerPinjaman.addView(emptyText)
        } else {
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            for (loan in activeLoans) {
                val card = layoutInflater.inflate(R.layout.item_pinjaman_aktif, containerPinjaman, false)

                val txtNominal = card.findViewById<TextView>(R.id.txtNominal)
                val txtSisa = card.findViewById<TextView>(R.id.txtSisa)
                val txtTenor = card.findViewById<TextView>(R.id.txtTenor)
                val txtTujuan = card.findViewById<TextView>(R.id.txtTujuan)
                val txtStatus = card.findViewById<TextView>(R.id.txtStatus)

                txtNominal.text = "Nominal: ${formatter.format(loan.optDouble("nominal", 0.0))}"
                txtSisa.text = "Sisa Angsuran: ${formatter.format(loan.optDouble("sisaAngsuran", 0.0))}"
                txtTenor.text = "Tenor: ${loan.optString("tenor", "-")}"
                txtTujuan.text = "Tujuan: ${loan.optString("tujuan", "-")}"
                txtStatus.text = "Status: ${loan.optString("status", "-")}"

                containerPinjaman.addView(card)
            }
        }
    }
}

//
//package com.example.project_map.ui.loans
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.example.project_map.R
//
//class PinjamanFragment : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_pinjaman, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val btnAjukanPinjaman = view.findViewById<Button>(R.id.btnAjukanPinjaman)
//        // --- THIS IS THE FIX ---
//        // Find the new button by its ID from the XML layout
//        val btnRiwayatPembayaran = view.findViewById<Button>(R.id.btnRiwayatPembayaran)
//
//        btnAjukanPinjaman.setOnClickListener {
//            findNavController().navigate(R.id.action_pinjamanFragment_to_loansFragment)
//        }
//
//        // Set the click listener for the history button
//        btnRiwayatPembayaran.setOnClickListener {
//            // It navigates to the PaymentHistoryFragment
//            findNavController().navigate(R.id.action_pinjamanFragment_to_paymentHistoryFragment)
//        }
//    }
//}
