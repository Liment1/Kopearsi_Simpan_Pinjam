package com.example.project_map.ui.user.loans

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.databinding.FragmentLoanFormBinding // Matches fragment_loan_form.xml
import java.text.NumberFormat
import java.util.Locale

class UserLoanForm : Fragment() {

    private var _binding: FragmentLoanFormBinding? = null
    private val binding get() = _binding!!

    // Share ViewModel with Dashboard to keep data in sync
    private val viewModel: UserLoanViewModel by activityViewModels()

    private var selectedKtpUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedKtpUri = uri
            binding.imgKtpPreview.setImageURI(uri)
            binding.btnUploadKtp.text = "Ganti Foto KTP"
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
        setupUI()
        setupCalculations()
        setupObservers()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        // Spinner Setup
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("Bulan", "Minggu") // Hardcoded defaults
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSatuan.adapter = adapter

        binding.btnRefreshScore.setOnClickListener { viewModel.fetchScore() }
        binding.btnUploadKtp.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnAjukan.setOnClickListener { validateAndSubmit() }
    }

    private fun setupCalculations() {
        binding.edtNominal.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = calculateLoanDetails(s.toString())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun calculateLoanDetails(nominalStr: String) {
        // Remove non-numeric characters before parsing
        val cleanString = nominalStr.replace("[^0-9]".toRegex(), "")
        if (cleanString.isEmpty()) {
            binding.txtJasa.text = "Jasa Pinjaman (5%): Rp 0"
            binding.txtTotalPengembalian.text = "Total Pengembalian: Rp 0"
            binding.txtDanaDiterima.text = "Total Dana Diterima: Rp 0"
            return
        }

        val nominal = cleanString.toDoubleOrNull() ?: 0.0
        val interest = nominal * 0.05
        val total = nominal + interest

        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0

        binding.txtJasa.text = "Jasa Pinjaman (5%): ${format.format(interest)}"
        binding.txtTotalPengembalian.text = "Total Pengembalian: ${format.format(total)}"
        binding.txtDanaDiterima.text = "Total Dana Diterima: ${format.format(nominal)}"
    }

    private fun setupObservers() {
        viewModel.creditScore.observe(viewLifecycleOwner) { score ->
            if (score != null) {
                binding.tvScore.text = score.toString()

                // Ensure SemiCircleProgressBar.kt exists for this to work
                val percent = ((score / 850) * 100).toInt()
                binding.progressBarScore.setPercent(percent)

                if (score >= 650) {
                    binding.tvDecision.text = "Keputusan: Disarankan"
                    binding.tvDecision.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                } else {
                    binding.tvDecision.text = "Keputusan: Perlu Peninjauan"
                    binding.tvDecision.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                }
            }
        }

        viewModel.loanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UserLoanViewModel.State.Loading -> {
                    binding.btnAjukan.isEnabled = false
                    binding.btnAjukan.text = "Memproses..."
                }
                is UserLoanViewModel.State.Success -> {
                    binding.btnAjukan.isEnabled = true
                    binding.btnAjukan.text = "Kirim Pengajuan Pinjaman"
                    Toast.makeText(context, "Pengajuan Berhasil!", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                    viewModel.resetState()
                }
                is UserLoanViewModel.State.Error -> {
                    binding.btnAjukan.isEnabled = true
                    binding.btnAjukan.text = "Kirim Pengajuan Pinjaman"
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
                else -> {}
            }
        }
    }

    private fun validateAndSubmit() {
        val jenis = binding.edtJenisPinjaman.text.toString().trim()
        val peruntukan = binding.edtPeruntukan.text.toString().trim()
        val lamaStr = binding.edtLamaPinjaman.text.toString().trim()
        val nominalStr = binding.edtNominal.text.toString().replace("[^0-9]".toRegex(), "")

        if (jenis.isEmpty() || peruntukan.isEmpty() || lamaStr.isEmpty() || nominalStr.isEmpty()) {
            Toast.makeText(context, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedKtpUri == null) {
            Toast.makeText(context, "Upload Foto KTP", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.submitLoan(
            jenis,
            peruntukan,
            lamaStr.toInt(),
            binding.spinnerSatuan.selectedItem.toString(),
            nominalStr.toDouble(),
            selectedKtpUri
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}