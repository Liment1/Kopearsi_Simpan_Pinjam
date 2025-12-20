package com.example.project_map.ui.admin.withdrawal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.databinding.FragmentAdminWithdrawalBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdminWithdrawalFragment : Fragment() {

    private var _binding: FragmentAdminWithdrawalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminWithdrawalViewModel by viewModels()
    private lateinit var adapter: AdminWithdrawalAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminWithdrawalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminWithdrawalAdapter(
            onApprove = { req -> showConfirmDialog(req, true) },
            onReject = { req -> showConfirmDialog(req, false) }
        )

        binding.rvRequests.layoutManager = LinearLayoutManager(context)
        binding.rvRequests.adapter = adapter

        viewModel.requests.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun showConfirmDialog(req: com.example.project_map.data.model.WithdrawalRequest, isApprove: Boolean) {
        val title = if (isApprove) "Setujui Penarikan?" else "Tolak Penarikan?"
        val msg = if (isApprove)
            "Saldo anggota akan dipotong sebesar Rp ${req.amount}."
        else
            "Permintaan akan dibatalkan."

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("Ya") { _, _ ->
                if (isApprove) viewModel.approve(req) else viewModel.reject(req)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}