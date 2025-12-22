package com.example.project_map.ui.user.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.example.project_map.R
import com.example.project_map.data.model.UserData
import com.example.project_map.databinding.FragmentProfileBinding
import com.example.project_map.databinding.ItemProfileMenuBinding
import com.example.project_map.ui.user.profile.syarat.UserSyaratKetentuanActivity
import com.google.firebase.auth.FirebaseAuth

class UserProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setupUI()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        // Reload data to ensure profile updates (like new photo) are shown
        auth.currentUser?.let { user ->
            viewModel.loadUserProfile(user.uid)
        }
    }

    private fun setupUI() {
        // 1. Setup Menu Item Texts (Using ViewBinding on the <include> layouts)
        // Access the included layout binding directly via the ID
        setupMenuItem(binding.btnDetailProfile, "Profil Lengkap", "Lihat dan edit data diri Anda")
        setupMenuItem(binding.btnLaporanBulanan, "Laporan Bulanan", "Riwayat transaksi per bulan")
        setupMenuItem(binding.btnSyaratKetentuan, "Syarat & Ketentuan", "Aturan layanan koperasi")
        setupMenuItem(binding.btnAdminMenu, "Kelola Anggota", "Menu khusus Admin")
        setupMenuItem(binding.btnLaporanKeuangan, "Laporan Keuangan", "Ringkasan aset koperasi")

        // 2. Setup Click Listeners & Navigation
        // Navigate to the new Fragments (converted from Activities)
        binding.btnDetailProfile.root.setOnClickListener {
            // Ensure you have this action in your nav_graph
            findNavController().navigate(R.id.action_profileFragment_to_userDetailProfileFragment)
        }

        binding.btnLaporanBulanan.root.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_userMonthlyReportFragment)
        }

        // Keep as Activity if not converted, or convert similarly
        binding.btnSyaratKetentuan.root.setOnClickListener {
            startActivity(Intent(requireContext(), UserSyaratKetentuanActivity::class.java))
        }

        // Admin Buttons (Placeholder or Navigation)
        binding.btnAdminMenu.root.setOnClickListener {
            Toast.makeText(context, "Fitur Admin: Kelola Anggota", Toast.LENGTH_SHORT).show()
        }
        binding.btnLaporanKeuangan.root.setOnClickListener {
            Toast.makeText(context, "Fitur Admin: Laporan Keuangan", Toast.LENGTH_SHORT).show()
        }

        // Logout
        binding.btnKeluar.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    private fun setupObservers() {
        viewModel.userProfileState.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe

            when (state) {
                is UserProfileViewModel.ProfileState.Loading -> {
                    // Optional: Show loading state (e.g., disable buttons)
                }
                is UserProfileViewModel.ProfileState.Success -> {
                    updateProfileUI(state.user)
                }
                is UserProfileViewModel.ProfileState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateProfileUI(user: UserData) {
        binding.tvName.text = user.name
        binding.tvEmail.text = user.email

        // Update Status Chip/Badge
        binding.tvStatus.text = user.status
        binding.tvStatus.setChipBackgroundColorResource(getStatusColorRes(user.status))

        // Handle Admin Visibility
        if (user.admin) {
            binding.cardAdmin.visibility = View.VISIBLE
        } else {
            binding.cardAdmin.visibility = View.GONE
        }

        // Load Avatar using Coil
        if (user.avatarUrl.isNotEmpty()) {
            binding.imgProfile.load(user.avatarUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
                placeholder(R.drawable.account_circle)
                error(R.drawable.account_circle)
            }
        }
    }

    /**
     * Helper to set text on the included item_profile_menu layouts.
     * itemBinding: The ViewBinding generated for the <include> (ItemProfileMenuBinding)
     */
    private fun setupMenuItem(itemBinding: ItemProfileMenuBinding, title: String, subtitle: String) {
        itemBinding.tvMenuTitle.text = title
        itemBinding.tvMenuSubtitle.text = subtitle
    }

    private fun getStatusColorRes(status: String): Int {
        return when (status) {
            "Anggota Aktif" -> R.color.green_primary // Ensure this exists in colors.xml
            "Calon Anggota" -> android.R.color.holo_orange_dark
            "Anggota Tidak Aktif" -> android.R.color.darker_gray
            "Diblokir Sementara" -> android.R.color.holo_red_light
            "Dikeluarkan" -> android.R.color.holo_red_dark
            else -> android.R.color.darker_gray
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}