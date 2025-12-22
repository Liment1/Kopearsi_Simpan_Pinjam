package com.example.project_map.ui.admin.withdrawal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.databinding.FragmentAdminWithdrawalBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale
import java.text.NumberFormat

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
            onApprove = { req -> showApproveDialog(req) },
            onReject = { req -> showRejectDialog(req) }
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

    private fun showApproveDialog(req: com.example.project_map.data.model.WithdrawalRequest) {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        val formattedAmount = format.format(req.amount)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Setujui Penarikan?")
            // FIX: Show clear details
            .setMessage("Pastikan Anda sudah mentransfer dana sebesar $formattedAmount ke rekening:\n\n${req.bankName} - ${req.accountNumber}\nAtas Nama: ${req.userName}")
            .setPositiveButton("Sudah Transfer") { _, _ ->
                viewModel.approve(req)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showRejectDialog(req: com.example.project_map.data.model.WithdrawalRequest) {
        // FIX: Format the currency
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        val formattedAmount = format.format(req.amount)

        val input = EditText(requireContext())
        input.hint = "Alasan penolakan (Wajib)"
        val container = android.widget.FrameLayout(requireContext())
        val params = android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(60, 20, 60, 0) // Add margin so it looks nice
        input.layoutParams = params
        container.addView(input)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tolak & Refund")
            .setMessage("Dana $formattedAmount akan DIKEMBALIKAN ke saldo anggota secara otomatis.")
            .setView(container)
            .setPositiveButton("Tolak & Refund") { _, _ ->
                val reason = input.text.toString()
                if (reason.isBlank()) {
                    Toast.makeText(context, "Alasan wajib diisi!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.reject(req, reason)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}