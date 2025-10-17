package com.example.project_map.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import org.json.JSONObject

class LoanHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loan_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<View>(R.id.toolbar)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerLoanHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val allLoans = LoanStorage.getAllLoans(requireContext()).map { parseJsonToLoan(it) }

        val adapter = LoanHistoryAdapter(allLoans.reversed()) { clickedLoan ->
            val bundle = Bundle().apply {
                putLong("loanId", clickedLoan.id)
            }
            // Navigate using the action ID and pass the bundle as the second parameter.
            findNavController().navigate(R.id.action_loanHistoryFragment_to_loanDetailFragment, bundle)
        }

        recyclerView.adapter = adapter
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
}