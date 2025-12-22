package com.example.project_map.ui.user.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.databinding.FragmentHomeBinding
import com.example.project_map.databinding.ItemMenuIconBinding

class UserHomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        setupMenuIcons()
        setupObservers()
    }

    private fun setupMenuIcons() {
        // Use binding object for included layouts
        bindMenu(binding.menuSimpanan, R.drawable.ic_wallet, "Simpanan")
        bindMenu(binding.menuPinjaman, R.drawable.ic_loan, "Pinjaman")
        bindMenu(binding.menuAngsuran, R.drawable.ic_installments, "Ajukan Pinjaman")
        bindMenu(binding.menuProfil, R.drawable.ic_person, "Profil")
    }

    private fun bindMenu(itemBinding: ItemMenuIconBinding, iconRes: Int, title: String) {
        itemBinding.imgIcon.setImageResource(iconRes)
        itemBinding.imgIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#2E7D32"))
        itemBinding.tvTitle.text = title
    }

    private fun setupObservers() {
        viewModel.userName.observe(viewLifecycleOwner) { binding.tvUserName.text = it }
        viewModel.totalBalance.observe(viewLifecycleOwner) { binding.tvSaldoValue.text = it }
        viewModel.totalLoanDebt.observe(viewLifecycleOwner) { binding.tvPinjamanValue.text = it }

        viewModel.recentActivities.observe(viewLifecycleOwner) { list ->
            binding.rvHistori.adapter = UserRecentAdapter(list)
        }

        viewModel.unreadNotifCount.observe(viewLifecycleOwner) { count ->
            if (count > 0) {
                binding.viewNotificationBadge.visibility = View.VISIBLE
            } else {
                binding.viewNotificationBadge.visibility = View.GONE
            }
        }

        viewModel.userStatus.observe(viewLifecycleOwner) { status ->
            setupNavigationActions(status)

            if (status == "Calon Anggota") {
                binding.cardVerificationStatus.visibility = View.VISIBLE
                binding.menuPinjaman.root.alpha = 0.5f
                binding.menuAngsuran.root.alpha = 0.5f
                binding.menuSimpanan.tvTitle.text = "Bayar Pokok"
            } else {
                binding.cardVerificationStatus.visibility = View.GONE
                binding.menuPinjaman.root.alpha = 1.0f
                binding.menuAngsuran.root.alpha = 1.0f
                binding.menuSimpanan.tvTitle.text = "Simpanan"
            }
        }
    }

    private fun setupNavigationActions(status: String) {
        val isVerified = status != "Calon Anggota"

        binding.menuSimpanan.root.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_savingsFragment)
        }

        binding.menuPinjaman.root.setOnClickListener {
            if (isVerified) findNavController().navigate(R.id.action_homeFragment_to_loansFragment)
            else showLockedToast()
        }

        binding.menuAngsuran.root.setOnClickListener {
            if (isVerified) findNavController().navigate(R.id.action_homeFragment_to_loanForm)
            else showLockedToast()
        }

        binding.menuProfil.root.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        binding.cardVerificationStatus.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_savingsFragment)
        }

        // Navigate to the new Notification Fragment
        binding.frameNotification.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationFragment)
        }
    }

    private fun showLockedToast() {
        Toast.makeText(context, "Fitur terkunci. Silakan selesaikan pembayaran Simpanan Pokok.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}