package com.example.project_map.ui.user.profile

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.graphics.Color
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
import com.example.project_map.databinding.FragmentProfileBinding
import com.example.project_map.ui.user.profile.detail.UserDetailProfileActivity
import com.example.project_map.ui.user.profile.laporan.UserLaporanBulananActivity
import com.example.project_map.ui.user.profile.syarat.UserSyaratKetentuanActivity
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

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
        // Reload data whenever we return to this screen (e.g. from DetailProfile)
        auth.currentUser?.let { user ->
            viewModel.loadUserProfile(user.uid)
        }
    }

    private fun setupUI() {
        binding.btnAdminMenu.visibility = View.GONE
        binding.btnLaporanKeuangan.visibility = View.GONE
        binding.dividerAdmin.visibility = View.GONE
        binding.dividerLaporan.visibility = View.GONE

        // Navigation
        binding.btnDetailProfile.setOnClickListener {
            startActivity(Intent(requireContext(), UserDetailProfileActivity::class.java))
        }
        binding.btnLaporanBulanan.setOnClickListener {
            startActivity(Intent(requireContext(), UserLaporanBulananActivity::class.java))
        }
        binding.btnSyaratKetentuan.setOnClickListener {
            startActivity(Intent(requireContext(), UserSyaratKetentuanActivity::class.java))
        }
        binding.btnKeluar.setOnClickListener {
            viewModel.logout() // Or dedicated AuthViewModel
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    private fun setupObservers() {
        viewModel.userProfileState.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe

            when (state) {
                is UserProfileViewModel.ProfileState.Loading -> {
                    // Optional: Show skeleton or spinner
                }
                is UserProfileViewModel.ProfileState.Success -> {
                    val user = state.user
                    binding.tvName.text = user.name
                    binding.tvEmail.text = user.email
                    binding.tvStatus.text = user.status

                    if (user.admin) {
                        binding.btnAdminMenu.visibility = View.VISIBLE
                        binding.btnLaporanKeuangan.visibility = View.VISIBLE
                        binding.dividerAdmin.visibility = View.VISIBLE
                        binding.dividerLaporan.visibility = View.VISIBLE
                    }

                    // Status Color
                    val statusBackground = binding.tvStatus.background as GradientDrawable
                    statusBackground.setColor(getStatusColor(user.status))

                    // Load Avatar
                    if (user.avatarUrl.isNotEmpty()) {
                        binding.imgProfile.load(user.avatarUrl) {
                            crossfade(true)
                            transformations(CircleCropTransformation())
                            placeholder(R.drawable.account_circle)
                        }
                    }
                }
                is UserProfileViewModel.ProfileState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getStatusColor(status: String): Int {
        return when (status) {
            "Anggota Aktif" -> Color.parseColor("#1E8E3E")
            "Calon Anggota" -> Color.parseColor("#F9AB00")
            "Anggota Tidak Aktif" -> Color.parseColor("#5F6368")
            "Diblokir Sementara" -> Color.parseColor("#E67C73")
            "Dikeluarkan" -> Color.parseColor("#D93025")
            else -> Color.LTGRAY
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}