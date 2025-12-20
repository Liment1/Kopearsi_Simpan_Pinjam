package com.example.project_map.ui.user.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.databinding.FragmentUserLoansBinding // <--- CHANGE THIS IMPORT

class UserLoansFragment : Fragment() {

    // CHANGE BINDING CLASS HERE
    private var _binding: FragmentUserLoansBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserLoanViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // INFLATE THE CORRECT BINDING
        _binding = FragmentUserLoansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Navigation
        // This will now work because fragment_user_loans.xml has @+id/toolbar
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // 2. Setup RecyclerView
        val adapter = UserLoansAdapter(emptyList()) { loan ->
            val bundle = Bundle().apply { putString("loanId", loan.id) }
            // FIX: Corrected Action ID based on your NavGraph
            findNavController().navigate(R.id.action_loansFragment_to_loanDetailFragment, bundle)
        }

        binding.rvActiveLoans.layoutManager = LinearLayoutManager(context)
        binding.rvActiveLoans.adapter = adapter

        // 3. Observe Data
        viewModel.activeLoans.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvActiveLoans.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvActiveLoans.visibility = View.VISIBLE
            }
        }

        viewModel.totalDebt.observe(viewLifecycleOwner) { amount ->
            binding.tvTotalDebt.text = amount
        }

        // 4. "Apply New Loan" Action
        binding.fabNewLoan.setOnClickListener {
            // Ensure this ID matches your NavGraph action ID
            findNavController().navigate(R.id.action_loansFragment_to_LoanForm)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}