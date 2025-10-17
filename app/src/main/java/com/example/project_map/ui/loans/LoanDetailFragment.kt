package com.example.project_map.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.databinding.FragmentLoanDetailV2Binding
import java.text.NumberFormat
import java.util.*

class LoanDetailFragment : Fragment() {

    private var _binding: FragmentLoanDetailV2Binding? = null
    private val binding get() = _binding!!
    // REMOVED: private val args: LoanDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoanDetailV2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- THIS IS THE FIX (RECEIVING) ---
        // Retrieve the arguments manually from the fragment's 'arguments' property.
        // We use the same key "loanId" and provide a default value (-1) for safety.
        val loanId = arguments?.getLong("loanId") ?: -1L
        // --- END OF FIX ---

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }

        binding.tvLoanPurpose.text = "Pinjaman Berjangka untuk Pernikahan"
        binding.tvStatus.text = "Pinjaman Berjalan"
        // ... (rest of the data setup) ...

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnBayarAngsuran.setOnClickListener {
            // --- THIS IS THE FIX (SENDING) ---
            // Create another bundle to pass the loanId to the next screen.
            val bundle = Bundle().apply {
                putLong("loanId", loanId)
            }
            findNavController().navigate(R.id.action_loanDetailFragment_to_angsuranFragment, bundle)
            // --- END OF FIX ---
        }
    }

    // ... (createDummyInstallments and onDestroyView remain the same) ...
    private fun createDummyInstallments(): List<Installment> {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }
        return listOf(
            Installment(1, "Angsuran Pokok", formatter.format(100000), "Dibayar pada tanggal 20 Maret 2025", formatter.format(0), true),
            Installment(2, "Angsuran Pokok", formatter.format(100000), "Dibayar pada tanggal 20 Maret 2025", formatter.format(0), true),
            Installment(3, "Angsuran Pokok", formatter.format(100000), "Dibayar pada tanggal 20 Maret 2025", formatter.format(0), true),
            Installment(4, "Angsuran Pokok", formatter.format(100000), "Harus dibayar pada tanggal 20 April 2025", formatter.format(0), false),
            Installment(5, "Angsuran Pokok", formatter.format(100000), "Harus dibayar pada tanggal 20 Mei 2025", formatter.format(0), false),
            Installment(6, "Angsuran Pokok", formatter.format(100000), "Harus dibayar pada tanggal 20 Juni 2025", formatter.format(0), false)
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}