package com.example.project_map.ui.user.loans

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.ui.other.custom.SemiCircleProgressBar
import java.io.FileNotFoundException
import java.io.InputStream

class UserLoansFragment : Fragment() {

    // Inject ViewModel (Using activityViewModels to share if needed, or viewModels)
    private val viewModel: UserLoanViewModel by activityViewModels()

    // --- Views ---
    // Credit Score
    private lateinit var progressBarScore: SemiCircleProgressBar
    private lateinit var tvScore: TextView
    private lateinit var tvDecision: TextView
    private lateinit var btnRefreshScore: Button

    // Form
    private lateinit var btnBack: ImageView
    private lateinit var imgKtp: ImageView
    private lateinit var btnUploadKtp: Button
    private lateinit var btnAjukan: Button
    private lateinit var edtNominal: EditText
    private lateinit var txtJasa: TextView
    private lateinit var txtTotalPengembalian: TextView
    private lateinit var txtDanaDiterima: TextView
    private lateinit var edtJenisPinjaman: EditText
    private lateinit var edtPeruntukan: EditText
    private lateinit var edtLamaPinjaman: EditText
    private lateinit var spinnerSatuan: Spinner

    // --- Helpers ---
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var ktpBitmap: Bitmap? = null
    private val RATE = 0.05

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loans, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize ALL Views
        initViews(view)

        // 2. Setup Listeners (Back, Upload, Submit)
        setupListeners()

        // 3. Setup Logic (Spinner, Image Pickers, Observers)
        setupSpinner()
        setupImagePickers()
        observeViewModel()

        // 4. Initial Data Load
        viewModel.fetchScore()
    }

    private fun initViews(view: View) {
        // Credit Score Section
        progressBarScore = view.findViewById(R.id.progressBarScore)
        tvScore = view.findViewById(R.id.tvScore)
        tvDecision = view.findViewById(R.id.tvDecision)
        btnRefreshScore = view.findViewById(R.id.btnRefreshScore)

        // Form Section
        btnBack = view.findViewById(R.id.btnBack)
        imgKtp = view.findViewById(R.id.imgKtpPreview)
        btnUploadKtp = view.findViewById(R.id.btnUploadKtp)
        btnAjukan = view.findViewById(R.id.btnAjukan)
        edtNominal = view.findViewById(R.id.edtNominal)
        txtJasa = view.findViewById(R.id.txtJasa)
        txtTotalPengembalian = view.findViewById(R.id.txtTotalPengembalian)
        txtDanaDiterima = view.findViewById(R.id.txtDanaDiterima)
        edtJenisPinjaman = view.findViewById(R.id.edtJenisPinjaman)
        edtPeruntukan = view.findViewById(R.id.edtPeruntukan)
        edtLamaPinjaman = view.findViewById(R.id.edtLamaPinjaman)
        spinnerSatuan = view.findViewById(R.id.spinnerSatuan)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnUploadKtp.setOnClickListener {
            showUploadOptions()
        }

        // Refresh Score
        btnRefreshScore.setOnClickListener {
            btnRefreshScore.text = "Loading..."
            viewModel.fetchScore()
        }

        // Submit Loan
        btnAjukan.setOnClickListener {
            validateAndSubmit()
        }

        // Nominal Text Watcher (For Calculations)
        edtNominal.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateHitungan() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSpinner() {
        val units = listOf("Bulan")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSatuan.adapter = adapter
    }

    private fun setupImagePickers() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
                val uri = result.data!!.data
                if (uri != null) loadImageFromUri(uri)
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
                val photo = result.data!!.extras?.get("data") as? Bitmap
                if (photo != null) {
                    ktpBitmap = photo
                    imgKtp.setImageBitmap(photo)
                }
            }
        }
    }

    private fun showUploadOptions() {
        val options = arrayOf("Ambil Foto", "Pilih dari Galeri", "Batal")
        AlertDialog.Builder(requireContext())
            .setTitle("Unggah KTP")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraLauncher.launch(intent)
                    }
                    1 -> {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        intent.type = "image/*"
                        galleryLauncher.launch(intent)
                    }
                    else -> dialog.dismiss()
                }
            }.show()
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            ktpBitmap = BitmapFactory.decodeStream(inputStream)
            imgKtp.setImageBitmap(ktpBitmap)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal memuat foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHitungan() {
        val nominalText = edtNominal.text.toString().replace("[^0-9]".toRegex(), "")
        if (nominalText.isNotEmpty()) {
            val nominal = try { nominalText.toDouble() } catch (e: Exception) { 0.0 }
            val bunga = nominal * RATE
            val totalPengembalian = nominal + bunga

            txtJasa.text = "Jasa Pinjaman (${(RATE*100).toInt()}%): Rp ${bunga.toLong()}"
            txtTotalPengembalian.text = "Total Pengembalian: Rp ${totalPengembalian.toLong()}"
            txtDanaDiterima.text = "Total Dana Diterima: Rp ${nominal.toLong()}"
        }
    }

    private fun observeViewModel() {
        // 1. Credit Score Observer
        viewModel.creditScore.observe(viewLifecycleOwner) { score ->
            btnRefreshScore.isEnabled = true
            btnRefreshScore.text = "Cek Skor Terbaru"
            if (score != null) {
                progressBarScore.progress = score.toFloat()
                tvScore.text = String.format("%.2f", score)
                tvDecision.text = if (score > 70) "Sangat Baik" else "Cukup"
            }
        }

        // 2. Submission State Observer
        viewModel.loanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UserLoanViewModel.State.Loading -> {
                    btnAjukan.isEnabled = false
                    btnAjukan.text = "Memproses..."
                }
                is UserLoanViewModel.State.Success -> {
                    Toast.makeText(context, "Pengajuan Berhasil!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loansFragment_to_pinjamanFragment)
                    // Reset state so it doesn't navigate again if back pressed
                    viewModel.resetState()
                }
                is UserLoanViewModel.State.Error -> {
                    btnAjukan.isEnabled = true
                    btnAjukan.text = "Kirim Pengajuan Pinjaman"
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> { /* Idle */ }
            }
        }
    }

    private fun validateAndSubmit() {
        val jenis = edtJenisPinjaman.text.toString().trim()
        val peruntukan = edtPeruntukan.text.toString().trim()
        val lamaStr = edtLamaPinjaman.text.toString().trim()
        val nominalText = edtNominal.text.toString().replace("[^0-9]".toRegex(), "")

        if (jenis.isEmpty() || peruntukan.isEmpty() || lamaStr.isEmpty() || nominalText.isEmpty()) {
            Toast.makeText(context, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        if (ktpBitmap == null) {
            Toast.makeText(context, "Foto KTP wajib diunggah", Toast.LENGTH_SHORT).show()
            return
        }

        // Send to ViewModel
        viewModel.submitLoan(
            jenis = jenis,
            peruntukan = peruntukan,
            lama = lamaStr.toInt(),
            satuan = "Bulan", // Hardcoded to Bulan as requested
            nominal = nominalText.toDouble()
        )
    }
}