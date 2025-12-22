package com.example.project_map.ui.admin.installments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.project_map.R
import com.example.project_map.databinding.FragmentAdminInstallmentDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class AdminInstallmentDetailFragment : Fragment() {

    private var _binding: FragmentAdminInstallmentDetailBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminInstallmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Get Arguments
        val userId = arguments?.getString("USER_ID")
        val loanId = arguments?.getString("LOAN_ID")
        val installmentId = arguments?.getString("INSTALLMENT_ID")

        // 3. Load Data
        if (userId != null && loanId != null && installmentId != null) {
            loadDetail(userId, loanId, installmentId)
        } else {
            Toast.makeText(context, "Error: Data ID tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDetail(uid: String, lid: String, iid: String) {
        val ref = db.collection("users").document(uid)
            .collection("loans").document(lid)

        // Fetch Installment Data
        ref.collection("installments").document(iid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val amount = doc.getDouble("jumlahBayar") ?: 0.0
                    val month = doc.getLong("bulanKe") ?: 0
                    val date = doc.getDate("tanggalBayar")
                    val proofUrl = doc.getString("buktiBayarUrl")

                    // Formatters
                    val formatRp = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }
                    val formatDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))

                    binding.tvAmountBig.text = formatRp.format(amount)
                    binding.tvSubtitle.text = "Bulan ke-$month"
                    binding.tvDate.text = if (date != null) formatDate.format(date) else "-"

                    // --- CHANGED LOGIC START ---
                    if (!proofUrl.isNullOrEmpty() && proofUrl != "Potong Saldo") {
                        binding.ivProof.visibility = View.VISIBLE
                        binding.tvNoProof.visibility = View.GONE

                        Glide.with(this)
                            .load(proofUrl)
                            .placeholder(R.drawable.ic_menu_gallery)
                            .into(binding.ivProof)

                    } else {
                        // Hide Image
                        binding.ivProof.visibility = View.GONE
                        binding.tvNoProof.visibility = View.VISIBLE

                        // Optional: Set specific text if it is Potong Saldo
                        if (proofUrl == "Potong Saldo") {
                            binding.tvNoProof.text = "Pembayaran via Potong Saldo"
                        } else {
                            // Reset to default text if needed (e.g. "Tidak ada bukti")
                            binding.tvNoProof.text = "Tidak ada bukti pembayaran"
                        }
                    }
                    // --- CHANGED LOGIC END ---
                }
            }

        // Fetch Context (User Name & Loan Type)
        ref.get().addOnSuccessListener { loanDoc ->
            if (loanDoc.exists()) {
                val loanType = loanDoc.getString("jenisPinjaman") ?: "Pinjaman"
                val borrowerName = loanDoc.getString("namaPeminjam")
                    ?: loanDoc.getString("name")
                    ?: "Anggota"

                binding.tvLoanName.text = "$borrowerName - $loanType"
                binding.tvLoanId.text = "ID: ${lid.take(8).uppercase()}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}