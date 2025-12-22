package com.example.project_map.ui.user.loans.installments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.databinding.FragmentLoanDetailBinding
import com.example.project_map.data.model.Loan
import com.example.project_map.ui.user.loans.UserLoanViewModel
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

class LoanDetailFragment : Fragment() {

    private var _binding: FragmentLoanDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserLoanViewModel by viewModels()
    private lateinit var adapter: InstallmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupUI()
        observeData()
    }

    private fun setupAdapter() {
        // Initialize with click listener (optional: show detail snackbar on item click)
        adapter = InstallmentAdapter(emptyList()) { installment ->
            Snackbar.make(binding.root, "Angsuran Bulan ke-${installment.bulanKe}", Snackbar.LENGTH_SHORT).show()
        }
        binding.rvInstallments.layoutManager = LinearLayoutManager(context)
        binding.rvInstallments.adapter = adapter
    }

    private fun setupUI() {
        // 1. Get Loan ID
        val loanId = arguments?.getString("loanId")
        if (loanId == null) {
            showError("Data pinjaman tidak ditemukan")
            findNavController().navigateUp()
            return
        }
        viewModel.fetchInstallments(loanId)

        // 2. Back Button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // 3. Pay Button Action
        binding.btnPayInstallment.setOnClickListener {
            handlePaymentClick()
        }
    }

    private fun observeData() {
        viewModel.activeLoan.observe(viewLifecycleOwner) { loan ->
            if (loan != null) {
                updateLoanUI(loan)
            }
        }

        viewModel.installments.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
        }
    }

    private fun updateLoanUI(loan: Loan) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0

        binding.tvRemainingAmount.text = formatter.format(loan.sisaAngsuran)

        // Calculate Progress
        val totalLoan = loan.sisaAngsuran + loan.totalDibayar
        val progress = if (totalLoan > 0) (loan.totalDibayar / totalLoan) * 100 else 0.0

        binding.progressBarLoan.progress = progress.toInt()
        binding.tvPaidAmount.text = "Dibayar: ${formatter.format(loan.totalDibayar)}"
        binding.tvPercentage.text = "${progress.toInt()}%"
    }

    private fun handlePaymentClick() {
        val currentList = viewModel.installments.value
        if (currentList.isNullOrEmpty()) {
            showError("Data angsuran sedang dimuat, harap tunggu...")
            return
        }

        // Logic: Find the earliest installment that is NOT paid
        val nextInstallment = currentList
            .sortedBy { it.bulanKe } // Sort by month (1, 2, 3...)
            .firstOrNull { it.status != "Lunas" }

        if (nextInstallment != null) {
            val bundle = Bundle().apply {
                putString("loanId", nextInstallment.loanId)
                putString("installmentId", nextInstallment.id)
                putDouble("amount", nextInstallment.jumlahBayar)
                putInt("month", nextInstallment.bulanKe)
            }

            // Assumption: You have this action in your nav_graph.xml
            try {
                findNavController().navigate(R.id.action_loanDetailFragment_to_angsuranFragment, bundle)
            } catch (e: Exception) {
                showError("Gagal membuka halaman pembayaran: Navigasi belum diatur.")
            }

        } else {
            Snackbar.make(binding.root, "Selamat! Semua angsuran telah lunas.", Snackbar.LENGTH_LONG)
                .setBackgroundTint(requireContext().getColor(android.R.color.holo_green_dark))
                .show()
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(requireContext().getColor(android.R.color.holo_red_dark))
            .setTextColor(requireContext().getColor(android.R.color.white))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}