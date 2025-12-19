package com.example.project_map.ui.admin.notification

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.databinding.FragmentAdminNotifikasiBinding

class AdminNotifikasiFragment : Fragment() {

    private var _binding: FragmentAdminNotifikasiBinding? = null
    private val binding get() = _binding!!

    // MVVM
    private val viewModel: AdminNotificationViewModel by viewModels()
    private lateinit var adapter: AdminAnnouncementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminNotifikasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        // 1. Manual Notification
        binding.btnSend.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val message = binding.etMessage.text.toString().trim()
            val isUrgent = binding.cbUrgent.isChecked
            viewModel.sendNotification(title, message, isUrgent)
        }

        // 2. Profit Distribution (SHU)
        binding.btnDistributeProfit.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminAnnouncementAdapter(emptyList())
        binding.rvAnnouncementHistory.layoutManager = LinearLayoutManager(context)
        binding.rvAnnouncementHistory.adapter = adapter
    }

    private fun setupObservers() {
        // List Data
        viewModel.announcements.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
        }

        // Loading State
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSend.isEnabled = !isLoading
            binding.btnDistributeProfit.isEnabled = !isLoading
            // Ideally show a ProgressBar here
        }

        // Toast Messages
        viewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                if (msg == "Pengumuman terkirim") {
                    binding.etTitle.text?.clear()
                    binding.etMessage.text?.clear()
                    binding.cbUrgent.isChecked = false
                }
                viewModel.clearMessage()
            }
        }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Pembagian SHU")
            .setMessage("Anda yakin ingin membagikan 90% dari profit bulan ini ke semua anggota?")
            .setPositiveButton("Ya, Bagikan") { _, _ ->
                viewModel.distributeProfit()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}