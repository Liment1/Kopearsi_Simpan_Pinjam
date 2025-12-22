package com.example.project_map.ui.admin.loans

import android.graphics.Color
import android.icu.text.NumberFormat
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Loan
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class AdminLoanFragment : Fragment(R.layout.fragment_admin_loan) {

    private val viewModel: AdminLoanViewModel by viewModels()
    private lateinit var adapter: AdminLoanAdapter

    private lateinit var tvDaftar: TextView
    private lateinit var tvHistory: TextView
    private lateinit var indicatorDaftar: View
    private lateinit var indicatorHistory: View

    private var isShowingHistory = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerViewLoans)
        tvDaftar = view.findViewById(R.id.tvDaftar)
        tvHistory = view.findViewById(R.id.tvHistory)
        indicatorDaftar = view.findViewById(R.id.indicatorDaftar)
        indicatorHistory = view.findViewById(R.id.indicatorHistory)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = AdminLoanAdapter(emptyList()) { loan, action ->
            when (action) {
                "terima" -> showApprovalDialog(loan)
                "tolak" -> showRejectionDialog(loan)
                "detail" -> {
                    // Navigate to AdminLoanDetailFragment
                    val bundle = Bundle().apply {
                        putString("loanId", loan.id)
                        putString("userId", loan.userId) // Ensure Loan model has userId
                    }
                    try {
                        findNavController().navigate(R.id.action_adminLoanFragment_to_adminLoanDetailFragment, bundle)
                    } catch (e: Exception) {
                        // Fallback if ID is different
                        Snackbar.make(requireView(), "Navigasi ke detail belum diatur di nav_graph", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
        recycler.adapter = adapter

        tvDaftar.setOnClickListener { switchTab(showHistory = false) }
        tvHistory.setOnClickListener { switchTab(showHistory = true) }

        observeViewModel()
        switchTab(false)
    }

    private fun observeViewModel() {
        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT).show()
                viewModel.onMessageShown()
            }
        }

        viewModel.pendingLoans.observe(viewLifecycleOwner) { list ->
            if (!isShowingHistory) adapter.updateList(list)
        }

        viewModel.historyLoans.observe(viewLifecycleOwner) { list ->
            if (isShowingHistory) adapter.updateList(list)
        }
    }

    private fun switchTab(showHistory: Boolean) {
        isShowingHistory = showHistory
        if (showHistory) {
            tvDaftar.setTextColor(Color.GRAY)
            indicatorDaftar.visibility = View.INVISIBLE
            tvHistory.setTextColor(Color.parseColor("#4CAF50"))
            indicatorHistory.visibility = View.VISIBLE
            adapter.updateList(viewModel.historyLoans.value ?: emptyList())
        } else {
            tvDaftar.setTextColor(Color.parseColor("#4CAF50"))
            indicatorDaftar.visibility = View.VISIBLE
            tvHistory.setTextColor(Color.GRAY)
            indicatorHistory.visibility = View.INVISIBLE
            adapter.updateList(viewModel.pendingLoans.value ?: emptyList())
        }
    }

    private fun showApprovalDialog(loan: Loan) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        val formattedNominal = formatter.format(loan.nominal)

        AlertDialog.Builder(requireContext())
            .setTitle("Setujui Pinjaman")
            .setMessage("Setujui pinjaman sebesar $formattedNominal?")
            .setPositiveButton("Ya") { _, _ -> viewModel.approveLoan(loan) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showRejectionDialog(loan: Loan) {
        val input = EditText(requireContext())
        input.hint = "Alasan penolakan..."
        AlertDialog.Builder(requireContext())
            .setTitle("Tolak Pinjaman")
            .setView(input)
            .setPositiveButton("Tolak") { _, _ -> viewModel.rejectLoan(loan, input.text.toString()) }
            .setNegativeButton("Batal", null)
            .show()
    }
}