package com.example.project_map.ui.user.profile.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.databinding.FragmentMonthlyReportBinding // Ensure XML name matches
import com.example.project_map.ui.user.profile.report.UserHistoryAdapter
import com.example.project_map.ui.user.profile.report.UserReportViewModel

class UserMonthlyReportFragment : Fragment() {

    private var _binding: FragmentMonthlyReportBinding? = null
    private val binding get() = _binding!!

    // MVVM: ViewModel
    private val viewModel: UserReportViewModel by viewModels()
    private lateinit var userHistoryAdapter: UserHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonthlyReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.navigationIcon?.setTint(android.graphics.Color.BLACK)

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        // Toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Init Adapter
        userHistoryAdapter = UserHistoryAdapter(emptyList())
        binding.rvTransactions.layoutManager = LinearLayoutManager(context)
        binding.rvTransactions.adapter = userHistoryAdapter

        // Actions
        binding.btnPreviousMonth.setOnClickListener { viewModel.changeMonth(-1) }
        binding.btnNextMonth.setOnClickListener { viewModel.changeMonth(1) }
    }

    private fun setupObservers() {
        // 1. Observe Month Title
        viewModel.monthTitle.observe(viewLifecycleOwner) { title ->
            binding.tvCurrentMonth.text = title
            binding.tvSummaryTitle.text = "Ringkasan Bulan $title"
        }

        // 2. Observe Financial Summary
        viewModel.financialSummary.observe(viewLifecycleOwner) { summary ->
            binding.tvTotalSimpanan.text = summary.totalSimpanan
            binding.tvTotalPinjaman.text = summary.totalPinjaman
            binding.tvTotalAngsuran.text = summary.totalAngsuran
        }

        // 3. Observe List Data
        viewModel.transactionList.observe(viewLifecycleOwner) { list ->
            userHistoryAdapter.updateData(list)
        }

        // 4. Observe Empty State
        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            if (isEmpty) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvTransactions.visibility = View.GONE
                binding.cardSummary.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvTransactions.visibility = View.VISIBLE
                binding.cardSummary.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}