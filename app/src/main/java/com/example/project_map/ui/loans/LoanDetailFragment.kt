package com.example.project_map.ui.loans

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.databinding.FragmentLoanDetailBinding
import java.text.NumberFormat
import java.util.*

class LoanDetailFragment : Fragment() {

    private var _binding: FragmentLoanDetailBinding? = null
    private val binding get() = _binding!!
    private val TAG = "LoanDetailDebug" // Tag for filtering logs

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoanDetailBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView: Binding has been inflated.")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Fragment view is created.")


        val loanId = arguments?.getLong("loanId") ?: -1L

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }

        //dummy data
        binding.tvLoanPurpose.text = "Pinjaman Berjangka untuk Pernikahan"
        binding.tvStatus.text = "Pinjaman Berjalan"
        binding.tvNominalPinjaman.text = formatter.format(1000000)
        binding.tvDiterima.text = "Diterima: ${formatter.format(9000000)}"
        binding.tvLoanTerm.text = "10 Bulan" // This will fill "Jangka Waktu Pinjaman"
        binding.tvDueDate.text = "15 Setiap Bulan"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Creates the adapter, and connects it to the RecyclerView.
        val installments = createDummyInstallments()
        val installmentAdapter = InstallmentAdapter(installments)
        binding.recyclerAngsuran.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAngsuran.adapter = installmentAdapter
        binding.recyclerAngsuran.addItemDecoration(TimelineItemDecoration(requireContext()))


        binding.btnBayarAngsuran.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("loanId", loanId)
            }
            Log.d(TAG, "Navigating to angsuranFragment with loanId: $loanId")
            findNavController().navigate(R.id.action_loanDetailFragment_to_angsuranFragment, bundle)
        }
    }

    private fun createDummyInstallments(): List<Installment> {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }
        return listOf(
            Installment(1, "Angsuran Pokok", formatter.format(100000), "Dibayar pada tanggal 20 Maret 2025", formatter.format(0), true),
            Installment(2, "Angsuran Pokok", formatter.format(100000), "Dibayar pada tanggal 20 Maret 2025", formatter.format(0), true)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Binding is now null.")
        _binding = null
    }
}