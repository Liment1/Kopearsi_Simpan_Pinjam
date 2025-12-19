package com.example.project_map.ui.user.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.databinding.FragmentHomeBinding

class UserHomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Use viewModels delegate to init ViewModel
    private val viewModel: UserHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvHistori.layoutManager = LinearLayoutManager(requireContext())

        setupObservers()
        setupNavigationActions()
    }

    private fun setupObservers() {
        // Observe Name
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvUserName.text = name
        }

        // Observe Balance
        viewModel.totalBalance.observe(viewLifecycleOwner) { balance ->
            binding.tvSaldoValue.text = balance
        }

        // Observe Loan Debt
        viewModel.totalLoanDebt.observe(viewLifecycleOwner) { debt ->
            binding.tvPinjamanValue.text = debt
        }

        // Observe Recent Activity List
        viewModel.recentActivities.observe(viewLifecycleOwner) { list ->
            // Update your adapter here
            binding.rvHistori.adapter = UserRecentAdapter(list)
        }
    }

    private fun setupNavigationActions() {
        binding.menuSimpanan.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_savingsFragment)
        }
        binding.menuPinjaman.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_pinjamanFragment)
        }
        binding.menuAngsuran.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_angsuranFragment)
        }
        binding.menuProfil.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
        binding.btnNotification.setOnClickListener {
            Toast.makeText(context, "Tidak ada notifikasi baru", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}