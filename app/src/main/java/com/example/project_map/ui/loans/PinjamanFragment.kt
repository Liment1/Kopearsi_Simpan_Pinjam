package com.example.project_map.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.databinding.FragmentPinjamanBinding
import org.json.JSONObject

class PinjamanFragment : Fragment() {

    private var _binding: FragmentPinjamanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPinjamanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAjukanPinjaman.setOnClickListener {
            // This navigation doesn't pass data, so it remains the same
            findNavController().navigate(R.id.action_pinjamanFragment_to_loansFragment)
        }

        val allLoans = LoanStorage.getAllLoans(requireContext()).map { parseJsonToLoan(it) }

        val historyAdapter = LoanHistoryAdapter(allLoans.reversed()) { clickedLoan ->
            // --- THIS IS THE FIX ---
            // Instead of using the Directions class, we create a Bundle manually.
            val bundle = Bundle().apply {
                // We put the loan's ID into the bundle with the key "loanId".
                // This key MUST match the argument name in the nav_graph.
                putLong("loanId", clickedLoan.id)
            }
            // We then navigate using the action ID and pass the bundle as the second parameter.
            findNavController().navigate(R.id.action_pinjamanFragment_to_loanDetailFragment, bundle)
            // --- END OF FIX ---
        }
        binding.recyclerLoanHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLoanHistory.adapter = historyAdapter
    }

    private fun parseJsonToLoan(obj: JSONObject): Loan {
        return Loan(
            id = obj.optLong("id"),
            namaPeminjam = obj.optString("namaPeminjam", "Pengguna"),
            nominal = obj.optDouble("nominal"),
            tenor = obj.optString("tenor"),
            tujuan = obj.optString("tujuan"),
            status = obj.optString("status"),
            alasanPenolakan = obj.optString("alasanPenolakan", "")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}