package com.example.project_map.ui.loans

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
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.data.CreditScoreManager // Import CreditScoreManager
import com.example.project_map.data.Loan
import com.example.project_map.ui.custom.SemiCircleProgressBar // Import custom view
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.Calendar
import java.util.Date

class LoansFragment : Fragment() {

    // --- Credit Score UI Components ---
    private lateinit var progressBarScore: SemiCircleProgressBar
    private lateinit var tvScore: TextView
    private lateinit var tvDecision: TextView
    private lateinit var btnRefreshScore: Button
    // ----------------------------------

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
    private lateinit var btnBack: View

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    private var ktpBitmap: Bitmap? = null
    private val RATE = 0.05

    // Firebase
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loans, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // --- Initialize Credit Score Views ---
        // Make sure these IDs exist in your updated fragment_loans.xml
        progressBarScore = view.findViewById(R.id.progressBarScore)
        tvScore = view.findViewById(R.id.tvScore)
        tvDecision = view.findViewById(R.id.tvDecision)
        btnRefreshScore = view.findViewById(R.id.btnRefreshScore)
        // -------------------------------------

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
        btnBack = view.findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setupImagePickers()
        setupSpinner()

        edtNominal.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateHitungan() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnAjukan.setOnClickListener { kirimPengajuan() }

        // Listener for manual refresh of score
        btnRefreshScore.setOnClickListener {
            fetchCreditScore()
        }

        updateHitungan()

        // Fetch score automatically when screen loads
        fetchCreditScore()

        return view
    }

    private fun fetchCreditScore() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            tvDecision.text = "Error: User not logged in"
            return
        }

        btnRefreshScore.isEnabled = false
        btnRefreshScore.text = "Memuat..."

        // Call the Manager to get the score from API
        CreditScoreManager.getScoreFromApi(userId) { score, decision ->
            // Switch back to Main Thread for UI updates
            activity?.runOnUiThread {
                btnRefreshScore.isEnabled = true
                btnRefreshScore.text = "Cek Skor Terbaru"

                if (score != null) {
                    // Update Progress Bar (0-100)
                    progressBarScore.progress = score.toFloat()

                    // Update Text
                    tvScore.text = String.format("%.2f", score)
                    tvDecision.text = "Keputusan Sistem: $decision"
                } else {
                    Toast.makeText(context, "Gagal mengambil skor kredit", Toast.LENGTH_SHORT).show()
                    tvDecision.text = "Keputusan: Gagal memuat"
                }
            }
        }
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

        btnUploadKtp.setOnClickListener { showUploadOptions() }
    }

    private fun setupSpinner() {
        if (spinnerSatuan.adapter == null) {
            val arr = resources.getStringArray(R.array.satuan_tenor)
            spinnerSatuan.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arr).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }
    }

    private fun showUploadOptions() {
        val options = arrayOf("Ambil Foto", "Pilih dari Galeri", "Batal")
        AlertDialog.Builder(requireContext())
            .setTitle("Unggah KTP")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> ambilFoto()
                    1 -> openGallery()
                    else -> dialog.dismiss()
                }
            }.show()
    }

    private fun ambilFoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
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

    private fun kirimPengajuan() {
        val userId = auth.currentUser?.uid
        if (userId == null) return

        val jenis = edtJenisPinjaman.text.toString().trim()
        val peruntukan = edtPeruntukan.text.toString().trim()
        val lamaStr = edtLamaPinjaman.text.toString().trim()
        val satuan = spinnerSatuan.selectedItem?.toString() ?: ""
        val nominalText = edtNominal.text.toString().replace("[^0-9]".toRegex(), "")

        if (jenis.isEmpty() || peruntukan.isEmpty() || lamaStr.isEmpty() || nominalText.isEmpty() || ktpBitmap == null) {
            Toast.makeText(requireContext(), "Lengkapi semua data (termasuk foto KTP)!", Toast.LENGTH_SHORT).show()
            return
        }

        val nominal = nominalText.toDouble()
        val lama = lamaStr.toInt()

        // 1. Calculate Financials
        val bunga = nominal * RATE
        val totalPengembalian = nominal + bunga

        // Simple calculation: Total / Duration
        val cicilanPerBulan = totalPengembalian / lama

        // 2. Prepare Batch Write
        val batch = db.batch()

        // A. Loan Document
        val newLoanRef = db.collection("users").document(userId).collection("loans").document()

        val loanData = Loan(
            id = newLoanRef.id,
            namaPeminjam = auth.currentUser?.displayName ?: "User",
            nominal = nominal,
            tenor = "$lama $satuan",
            tujuan = peruntukan,
            status = "Proses",
            bunga = RATE,
            sisaAngsuran = totalPengembalian,
            totalDibayar = 0.0,
            tanggalPengajuan = Date()
        )
        batch.set(newLoanRef, loanData)

        // B. Generate Installment Schedule
        val calendar = Calendar.getInstance()

        for (i in 1..lama) {
            calendar.add(Calendar.MONTH, 1) // Next month

            val instRef = newLoanRef.collection("installments").document()

            val installmentMap = hashMapOf(
                "id" to instRef.id,
                "loanId" to newLoanRef.id,
                "bulanKe" to i,
                "jumlahBayar" to cicilanPerBulan,
                "jatuhTempo" to calendar.time,
                "status" to "Belum Bayar",
                "tanggalBayar" to null,
                "buktiBayarUrl" to ""
            )

            batch.set(instRef, installmentMap)
        }

        // 3. Commit
        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Pengajuan & Jadwal berhasil dibuat.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_loansFragment_to_pinjamanFragment)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengirim pengajuan", Toast.LENGTH_SHORT).show()
            }
    }
}