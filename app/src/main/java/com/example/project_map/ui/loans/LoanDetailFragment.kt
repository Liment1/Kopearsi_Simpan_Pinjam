package com.example.project_map.ui.loans

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.data.Loan
import com.example.project_map.databinding.FragmentLoanDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

class LoanDetailFragment : Fragment() {

    private var _binding: FragmentLoanDetailBinding? = null
    private val binding get() = _binding!!
    private val TAG = "LoanDetailDebug"

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var loanAdapter: LoanAdapter

    // Store status to check in click listener
    private var currentLoanStatus: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoanDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val loanId = arguments?.getString("loanId")

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        loanAdapter = LoanAdapter(emptyList())
        binding.recyclerAngsuran.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAngsuran.adapter = loanAdapter
        binding.recyclerAngsuran.addItemDecoration(TimelineItemDecoration(requireContext()))

        binding.btnBayarAngsuran.setOnClickListener {
            if (loanId != null) {
                // FEATURE IMPLEMENTATION: Check status before allowing payment
                if (currentLoanStatus.equals("Ditolak", ignoreCase = true)) {
                    Toast.makeText(requireContext(), "Pinjaman ditolak, tidak dapat melakukan pembayaran.", Toast.LENGTH_LONG).show()
                } else if (currentLoanStatus.equals("Lunas", ignoreCase = true)) {
                    Toast.makeText(requireContext(), "Pinjaman sudah lunas!", Toast.LENGTH_SHORT).show()
                } else {
                    val bundle = Bundle().apply { putString("loanId", loanId) }
                    findNavController().navigate(R.id.action_loanDetailFragment_to_angsuranFragment, bundle)
                }
            }
        }

        val userId = auth.currentUser?.uid
        if (userId != null && loanId != null) {
            loadLoanData(userId, loanId)
        } else {
            Toast.makeText(context, "Error: Invalid Loan ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLoanData(userId: String, docId: String) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }

        val loanRef = db.collection("users").document(userId).collection("loans").document(docId)

        loanRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val loan = document.toObject(Loan::class.java)

                    if (loan != null) {
                        currentLoanStatus = loan.status // Save status for button logic

                        // UI Updates
                        binding.tvLoanPurpose.text = loan.tujuan
                        binding.tvStatus.text = loan.status
                        binding.tvNominalPinjaman.text = currencyFormat.format(loan.nominal)
                        binding.tvDiterima.text = "Diterima: ${currencyFormat.format(loan.nominal)}"
                        binding.tvLoanTerm.text = loan.tenor
                        binding.tvDueDate.text = "20 Setiap Bulan"

                        // FEATURE: Disable button visually if Ditolak
                        updateButtonState(loan.status)

                        loadInstallmentTimeline(loanRef, loan)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading loan", e)
            }
    }

    private fun updateButtonState(status: String) {
        if (status.equals("Ditolak", ignoreCase = true)) {
            binding.btnBayarAngsuran.text = "Pengajuan Ditolak"
            binding.btnBayarAngsuran.isEnabled = false
            binding.btnBayarAngsuran.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray)
        } else if (status.equals("Lunas", ignoreCase = true)) {
            binding.btnBayarAngsuran.text = "Pinjaman Lunas"
            binding.btnBayarAngsuran.isEnabled = false
            binding.btnBayarAngsuran.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_dark)
        } else {
            binding.btnBayarAngsuran.text = "Bayar Angsuran"
            binding.btnBayarAngsuran.isEnabled = true
            // Reset color if needed, or leave default from XML
        }
    }

    private fun loadInstallmentTimeline(loanRef: com.google.firebase.firestore.DocumentReference, loan: Loan) {
        loanRef.collection("installments")
            .get()
            .addOnSuccessListener { result ->
                // Use the Installment class defined in LoanAdapter.kt
                val paidList = result.toObjects(Installment::class.java)

                Log.d(TAG, "Installments found in DB: ${paidList.size}")
                paidList.forEach { Log.d(TAG, "DB Item: #${it.number} isPaid=${it.isPaid}") }

                val tenorInt = try {
                    loan.tenor.filter { it.isDigit() }.toInt()
                } catch (e: Exception) { 1 }

                val monthlyAmount = if (tenorInt > 0) loan.nominal / tenorInt else 0.0

                val fullTimeline = mutableListOf<Installment>()
                val startDate = loan.tanggalPengajuan ?: Date()

                for (i in 1..tenorInt) {
                    // Logic: Find if there is a payment in DB with number 'i'
                    val existingPayment = paidList.find { it.number == i }

                    if (existingPayment != null) {
                        // FIX: Explicitly ensure isPaid is true if coming from DB,
                        // though Firestore should handle it if field matches.
                        existingPayment.isPaid = true
                        fullTimeline.add(existingPayment)
                    } else {
                        // Generate Upcoming (Not Paid)
                        val cal = Calendar.getInstance()
                        cal.time = startDate
                        cal.add(Calendar.MONTH, i)

                        fullTimeline.add(Installment(
                            number = i,
                            type = "Angsuran Pokok",
                            amount = monthlyAmount,
                            date = cal.time,
                            isPaid = false
                        ))
                    }
                }

                fullTimeline.sortBy { it.number }
                loanAdapter.updateData(fullTimeline)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}