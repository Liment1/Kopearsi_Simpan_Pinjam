package com.example.project_map.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.databinding.FragmentAngsuranBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

class AngsuranFragment : Fragment() {

    private var _binding: FragmentAngsuranBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var loanId: String? = null

    // Payment Data
    private var paymentAmount = 0.0
    private var isUploadSuccess = false // Mock for image upload

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAngsuranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        loanId = arguments?.getString("loanId")

        setupUI()
        fetchLoanDetails() // To get exact amount needed
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // Logic for Radio Group
        binding.rgMetodePembayaran.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbVirtualAccount) {
                binding.btnUploadBukti.visibility = View.VISIBLE
                binding.btnBayar.text = "Konfirmasi Pembayaran"
            } else {
                binding.btnUploadBukti.visibility = View.GONE
                binding.btnBayar.text = "Bayar Sekarang"
            }
        }

        // Mock Upload Button
        binding.btnUploadBukti.setOnClickListener {
            // In real app: Open Gallery -> Upload to Storage -> Get URL
            isUploadSuccess = true
            binding.tvUploadStatus.isVisible = true
            binding.tvUploadStatus.text = "Bukti Terupload (Dummy)"
            binding.tvUploadStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            Toast.makeText(context, "Bukti berhasil diupload", Toast.LENGTH_SHORT).show()
        }

        binding.btnBayar.setOnClickListener {
            if (binding.rbVirtualAccount.isChecked && !isUploadSuccess) {
                Toast.makeText(context, "Harap upload bukti transfer terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else {
                showConfirmationDialog()
            }
        }
    }

    private fun fetchLoanDetails() {
        val userId = auth.currentUser?.uid ?: return
        if (loanId == null) return

        db.collection("users").document(userId).collection("loans").document(loanId!!).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nominal = doc.getDouble("nominal") ?: 0.0
                    val tenorString = doc.getString("tenor") ?: "1 Bulan"

                    // Simple logic: Monthly = Nominal / Tenor Int
                    val tenorCount = tenorString.filter { it.isDigit() }.toIntOrNull() ?: 1
                    // Adding interest logic if you have it, else flat division
                    paymentAmount = (nominal / tenorCount)

                    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                    formatter.maximumFractionDigits = 0
                    binding.tvAmountDue.text = formatter.format(paymentAmount)
                }
            }
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi")
            .setMessage("Lanjutkan pembayaran sebesar ${binding.tvAmountDue.text}?")
            .setPositiveButton("Ya") { _, _ -> processPaymentFirestore() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun processPaymentFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val lid = loanId ?: return

        // 1. First, Count existing payments to determine the number (1, 2, 3...)
        val loanRef = db.collection("users").document(userId).collection("loans").document(lid)

        loanRef.collection("installments").get()
            .addOnSuccessListener { snapshot ->
                val currentCount = snapshot.size()
                val nextInstallmentNumber = currentCount + 1 // This ensures it saves as "1", "2", etc.

                val batch = db.batch()

                // 2. Update Main Loan Totals
                batch.update(loanRef, "totalDibayar", FieldValue.increment(paymentAmount))
                batch.update(loanRef, "sisaAngsuran", FieldValue.increment(-paymentAmount))

                // 3. Add New Installment Record with CORRECT Number
                val installmentRef = loanRef.collection("installments").document()

                val newInstallment = hashMapOf(
                    "amount" to paymentAmount,
                    "date" to Date(),
                    "number" to nextInstallmentNumber, // <--- FIXED: Uses 1, 2, 3... instead of random timestamp
                    "type" to "Angsuran Pokok",
                    "otherFees" to 0.0,
                    "isPaid" to true
                )
                batch.set(installmentRef, newInstallment)

                batch.commit().addOnSuccessListener {
                    Toast.makeText(context, "Pembayaran Berhasil!", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }.addOnFailureListener {
                    Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal memproses data", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}