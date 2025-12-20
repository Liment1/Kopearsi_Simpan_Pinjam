package com.example.project_map.ui.user.loans.Installments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.data.model.Installment
import com.example.project_map.data.model.Loan
import com.example.project_map.databinding.FragmentLoanDetailBinding
import com.example.project_map.ui.user.loans.UserLoanViewModel
import java.text.NumberFormat
import java.util.Locale

class UserLoanDetailFragment : Fragment() {

    private var _binding: FragmentLoanDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserLoanViewModel by activityViewModels()

    // Variable to hold the NEXT installment to pay
    private var nextInstallmentToPay: Installment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoanDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loanId = arguments?.getString("loanId")
        if (loanId == null) { findNavController().popBackStack(); return }

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // Fix Overlap: Add padding to bottom of RecyclerView
        binding.recyclerAngsuran.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAngsuran.clipToPadding = false
        binding.recyclerAngsuran.setPadding(0, 0, 0, 250) // 250px bottom padding so button doesn't cover last item

        // 1. Observe Installments
        viewModel.installments.observe(viewLifecycleOwner) { list ->
            // Update Adapter (Read Only)
            val adapter = UserInstallmentAdapter(list)
            binding.recyclerAngsuran.adapter = adapter

            // FIND NEXT INSTALLMENT
            // Sort by month, find first one that is NOT 'Lunas' and NOT 'Menunggu'
            nextInstallmentToPay = list.sortedBy { it.bulanKe }
                .firstOrNull { it.status == "Belum Bayar" || it.status == "Telat" }

            updatePayButtonState()
        }

        // 2. Observe Loan Header
        viewModel.loanDetails.observe(viewLifecycleOwner) { loan ->
            if (loan != null) updateHeaderUI(loan)
        }

        // 3. Button Click -> Pay Next Bill
        binding.btnBayarAngsuran.setOnClickListener {
            if (nextInstallmentToPay != null) {
                viewModel.resetState()

                val bundle = bundleOf(
                    "loanId" to loanId,
                    "installmentId" to nextInstallmentToPay!!.id,
                    "amount" to nextInstallmentToPay!!.jumlahBayar
                )
                findNavController().navigate(R.id.action_loanDetailFragment_to_angsuranFragment, bundle)
            } else {
                Toast.makeText(context, "Semua angsuran sudah lunas.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadInstallments(loanId)
        viewModel.loadLoanDetails(loanId)
    }

    private fun updatePayButtonState() {
        if (nextInstallmentToPay != null) {
            binding.btnBayarAngsuran.isEnabled = true
            binding.btnBayarAngsuran.text = "Bayar Angsuran Ke-${nextInstallmentToPay!!.bulanKe}"
            binding.btnBayarAngsuran.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_primary)
        } else {
            binding.btnBayarAngsuran.isEnabled = false
            binding.btnBayarAngsuran.text = "Tidak Ada Tagihan"
            binding.btnBayarAngsuran.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray)
        }
    }

    private fun updateHeaderUI(loan: Loan) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }
        binding.tvLoanPurpose.text = loan.tujuan
        binding.tvStatus.text = loan.status
        binding.tvNominalPinjaman.text = currencyFormat.format(loan.nominal)
        binding.tvDiterima.text = "Diterima: ${currencyFormat.format(loan.nominal)}"
        binding.tvLoanTerm.text = loan.tenor

        // If loan status is Rejected, override button
        if (loan.status == "Ditolak") {
            binding.btnBayarAngsuran.isEnabled = false
            binding.btnBayarAngsuran.text = "Pengajuan Ditolak"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}