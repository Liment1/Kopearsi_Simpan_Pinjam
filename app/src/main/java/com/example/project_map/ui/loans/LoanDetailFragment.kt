package com.example.project_map.ui.loans

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoanDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val loanId = arguments?.getString("loanId")

        Log.d(TAG, "Opened Loan Detail with ID: $loanId") // Check Logcat for this!

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        loanAdapter = LoanAdapter(emptyList())
        binding.recyclerAngsuran.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAngsuran.adapter = loanAdapter
        binding.recyclerAngsuran.addItemDecoration(TimelineItemDecoration(requireContext()))

        binding.btnBayarAngsuran.setOnClickListener {
            if (loanId != null) {
                val bundle = Bundle().apply { putString("loanId", loanId) }
                findNavController().navigate(R.id.action_loanDetailFragment_to_angsuranFragment, bundle)
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

        // 1. FETCH LOAN HEADER
        val loanRef = db.collection("users").document(userId).collection("loans").document(docId)

        loanRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val loan = document.toObject(Loan::class.java)

                    if (loan != null) {
                        Log.d(TAG, "Loan Data Loaded: Nominal=${loan.nominal}, Tenor=${loan.tenor}")

                        // Update Header UI
                        binding.tvLoanPurpose.text = loan.tujuan
                        binding.tvStatus.text = loan.status
                        binding.tvNominalPinjaman.text = currencyFormat.format(loan.nominal)
                        binding.tvDiterima.text = "Diterima: ${currencyFormat.format(loan.nominal)}"
                        binding.tvLoanTerm.text = loan.tenor
                        binding.tvDueDate.text = "20 Setiap Bulan"

                        // 2. FETCH INSTALLMENTS (Nested Collection)
                        loadInstallmentTimeline(loanRef, loan)
                    } else {
                        Log.e(TAG, "Loan object is null after conversion")
                    }
                } else {
                    Log.e(TAG, "Loan Document does not exist at path: ${loanRef.path}")
                    binding.tvLoanPurpose.text = "Data Error"
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading loan", e)
            }
    }

    private fun loadInstallmentTimeline(loanRef: com.google.firebase.firestore.DocumentReference, loan: Loan) {
        // Look strictly inside the LOAN document for 'installments'
        loanRef.collection("installments")
            .get()
            .addOnSuccessListener { result ->
                val paidList = result.toObjects(Installment::class.java)
                Log.d(TAG, "Found ${paidList.size} paid installments in subcollection")

                // Parse Tenor (e.g., "1 Bulan" -> 1)
                val tenorInt = try {
                    loan.tenor.filter { it.isDigit() }.toInt()
                } catch (e: Exception) { 1 }

                // Avoid division by zero
                val monthlyAmount = if (tenorInt > 0) loan.nominal / tenorInt else 0.0

                val fullTimeline = mutableListOf<Installment>()
                val startDate = loan.tanggalPengajuan ?: Date()

                // Generate 1 to Tenor
                for (i in 1..tenorInt) {
                    // Check if this number exists in the PAID list
                    val existingPayment = paidList.find { it.number == i }

                    if (existingPayment != null) {
                        // Use the Paid record
                        fullTimeline.add(existingPayment)
                    } else {
                        // Generate Upcoming record
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

                // Sort and Update
                fullTimeline.sortBy { it.number }
                loanAdapter.updateData(fullTimeline)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}