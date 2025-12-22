package com.example.project_map.ui.user.loans

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.databinding.FragmentLoanFormBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

class LoanFormFragment : Fragment() {

    private var _binding: FragmentLoanFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserLoanViewModel by viewModels()

    private var selectedKtpUri: Uri? = null
    private var loadingDialog: AlertDialog? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { /* Ignore */ }

            selectedKtpUri = uri
            binding.imgKtpPreview.post {
                binding.imgKtpPreview.setImageURI(uri)
                binding.imgKtpPreview.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLoadingDialog()
        setupListeners()
        setupCalculations()
        setupObservers()
    }

    private fun setupLoadingDialog() {
        loadingDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnUploadKtp.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnAjukan.setOnClickListener {
            validateAndSubmit()
        }

        // Refresh Score Action
        binding.btnRefreshScore.setOnClickListener {
            loadingDialog?.show()
            viewModel.refreshCreditScore()
        }

        val satuanAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.satuan_tenor,
            android.R.layout.simple_spinner_item
        )
        satuanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSatuan.adapter = satuanAdapter
    }

    private fun setupObservers() {
        // --- CREDIT SCORE OBSERVER ---
        viewModel.creditScore.observe(viewLifecycleOwner) { score ->
            loadingDialog?.dismiss()
            if (score != null) {
                // A. Update Text
                val formattedScore = String.format("%.0f", score)
                binding.tvScore.text = formattedScore

                // B. Update Progress Bar
                // Scale 0-1000 score to 0-100 percentage
                val progressPercent = ((score / 1000.0) * 100).toInt()

                // FIX: Changed .progress to .setPercentage()
                // If this is red, check SemiCircleProgressBar.kt for the correct function name
                try {
                    binding.progressBarScore.setPercentage(progressPercent)
                } catch (e: Exception) {
                    // Fallback in case the view does not support this method
                    // You might need to check your custom view implementation
                }

                // C. Feedback
                Snackbar.make(binding.root, "Skor diperbarui!", Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.btnRefreshScore)
                    .setBackgroundTint(requireContext().getColor(android.R.color.holo_green_dark))
                    .show()
            } else {
                Snackbar.make(binding.root, "Gagal mengambil skor", Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                loadingDialog?.dismiss()
                if (result.isSuccess) {
                    Snackbar.make(binding.root, "Pengajuan berhasil dikirim!", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(requireContext().getColor(android.R.color.holo_green_dark))
                        .show()
                    findNavController().popBackStack()
                } else {
                    Snackbar.make(binding.root, "Gagal: ${result.exceptionOrNull()?.message}", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(requireContext().getColor(android.R.color.holo_red_dark))
                        .show()
                }
                viewModel.resetResult()
            }
        }
    }

    private fun setupCalculations() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateSummary()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.edtNominal.addTextChangedListener(textWatcher)
    }

    private fun calculateSummary() {
        val nominalStr = binding.edtNominal.text.toString()
        val nominal = nominalStr.toDoubleOrNull() ?: 0.0

        val serviceFee = nominal * 0.05
        val totalReturn = nominal + serviceFee
        val received = nominal

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0

        binding.txtJasa.text = "Biaya Admin (5%): ${formatter.format(serviceFee)}"
        binding.txtTotalPengembalian.text = "Total Pengembalian: ${formatter.format(totalReturn)}"
        binding.txtDanaDiterima.text = "Dana Diterima: ${formatter.format(received)}"
    }

    private fun validateAndSubmit() {
        val jenis = binding.edtJenisPinjaman.text.toString()
        val peruntukan = binding.edtPeruntukan.text.toString()
        val nominalStr = binding.edtNominal.text.toString()
        val tenorStr = binding.edtLamaPinjaman.text.toString()

        if (jenis.isEmpty() || peruntukan.isEmpty() || nominalStr.isEmpty() || tenorStr.isEmpty()) {
            Snackbar.make(binding.root, "Mohon lengkapi semua data", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(requireContext().getColor(android.R.color.holo_red_dark))
                .show()
            return
        }

        if (selectedKtpUri == null) {
            Snackbar.make(binding.root, "Wajib unggah foto KTP", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(requireContext().getColor(android.R.color.holo_red_dark))
                .show()
            return
        }

        loadingDialog?.show()

        val amount = nominalStr.toDouble()
        val tenor = tenorStr.toInt()
        val satuan = binding.spinnerSatuan.selectedItem.toString()

        viewModel.submitLoan(jenis, peruntukan, amount, tenor, satuan, selectedKtpUri!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}