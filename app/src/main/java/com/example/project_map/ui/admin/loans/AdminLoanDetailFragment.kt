package com.example.project_map.ui.admin.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.project_map.data.model.Loan
import com.example.project_map.databinding.FragmentAdminLoanDetailBinding
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

class AdminLoanDetailFragment : Fragment() {

    private var _binding: FragmentAdminLoanDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminLoanDetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminLoanDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loanId = arguments?.getString("loanId")
        val userId = arguments?.getString("userId")

        if (loanId.isNullOrEmpty()) {
            Toast.makeText(context, "ID Pinjaman Hilang", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        viewModel.loadData(loanId, userId ?: "")

        // Observe Data
        viewModel.loan.observe(viewLifecycleOwner) { loan ->
            if (loan != null) {
                val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                format.maximumFractionDigits = 0
                binding.tvLoanAmount.text = format.format(loan.nominal)
                binding.tvLoanReason.text = "Tujuan: ${loan.tujuan}"

                if (loan.ktpUrl.isNotEmpty()) {
                    binding.ivLoanProof.visibility = View.VISIBLE
                    Glide.with(this).load(loan.ktpUrl).into(binding.ivLoanProof)
                } else {
                    binding.ivLoanProof.visibility = View.GONE
                }

                // --- FIX 1: HIDE BUTTONS IF NOT "PROSES" ---
                if (loan.status.equals("Proses", ignoreCase = true)) {
                    binding.btnApprove.visibility = View.VISIBLE
                    binding.btnReject.visibility = View.VISIBLE
                } else {
                    binding.btnApprove.visibility = View.GONE
                    binding.btnReject.visibility = View.GONE

                    // Optional: Show a message instead
                    // Snackbar.make(binding.root, "Status saat ini: ${loan.status}", Snackbar.LENGTH_SHORT).show()
                }

                // --- FIX 2: ATTACH DIALOGS TO BUTTONS ---
                binding.btnApprove.setOnClickListener {
                    showApproveDialog(loanId, userId ?: "", loan.nominal)
                }

                binding.btnReject.setOnClickListener {
                    showRejectDialog(loanId, userId ?: "")
                }
            }
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = user.name
                binding.tvUserEmail.text = user.email
                if (user.avatarUrl.isNotEmpty()) {
                    Glide.with(this).load(user.avatarUrl).circleCrop().into(binding.ivUserAvatar)
                }
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldBack ->
            if (shouldBack) findNavController().popBackStack()
        }
    }

    // --- DIALOG HELPERS ---

    private fun showApproveDialog(loanId: String, userId: String, nominal: Double) {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        val amountStr = format.format(nominal)

        AlertDialog.Builder(requireContext())
            .setTitle("Setujui Pinjaman?")
            .setMessage("Anda akan menyetujui pinjaman sebesar $amountStr. Lanjutkan?")
            .setPositiveButton("Ya, Setujui") { _, _ ->
                viewModel.updateStatus(loanId, userId, "Disetujui")
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showRejectDialog(loanId: String, userId: String) {
        val input = EditText(requireContext())
        input.hint = "Masukkan alasan penolakan..."

        // Add some margin/padding to the input if needed, or use a layout
        // For simplicity here, just setting the view

        AlertDialog.Builder(requireContext())
            .setTitle("Tolak Pinjaman")
            .setView(input)
            .setPositiveButton("Tolak") { _, _ ->
                val reason = input.text.toString()
                if (reason.isBlank()) {
                    Toast.makeText(context, "Alasan wajib diisi!", Toast.LENGTH_SHORT).show()
                } else {
                    // Call the NEW function in ViewModel
                    viewModel.rejectLoan(loanId, userId, reason)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}