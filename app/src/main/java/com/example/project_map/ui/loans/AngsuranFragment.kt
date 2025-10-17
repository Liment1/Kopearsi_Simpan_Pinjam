package com.example.project_map.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.databinding.FragmentAngsuranBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.*

class AngsuranFragment : Fragment() {

    private var _binding: FragmentAngsuranBinding? = null
    private val binding get() = _binding!!
    // REMOVED: private val args: AngsuranFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAngsuranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- THIS IS THE FIX ---
        val loanId = arguments?.getLong("loanId") ?: -1L
        // --- END OF FIX ---

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }

        binding.tvAmountDue.text = formatter.format(100000)
        // ... (rest of the setup) ...
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnBayar.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Pembayaran")
            .setMessage("Apakah Anda yakin ingin melakukan pembayaran angsuran?")
            .setPositiveButton("Ya, Bayar") { _, _ ->
                Toast.makeText(requireContext(), "Pembayaran berhasil!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack(R.id.pinjamanFragment, false)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    // ... (onDestroyView remains the same) ...
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}