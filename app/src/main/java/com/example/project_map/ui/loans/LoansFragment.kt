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
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.InputStream

class LoansFragment : Fragment() {

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

    // Bunga sebagai konstanta (5% saat ini)
    private val RATE = 0.05

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loans, container, false)

        // --- bind views ---
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

        // --- back button (navigasi) ---
        btnBack.setOnClickListener {
            // kembali ke fragment sebelumnya
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // --- register activity result launchers ---
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

        // --- upload KTP (pilih atau ambil foto) ---
        btnUploadKtp.setOnClickListener { showUploadOptions() }

        // --- spinner: jika belum diset di XML dengan entries, kita set adapter guard ---
        if (spinnerSatuan.adapter == null) {
            val arr = resources.getStringArray(R.array.satuan_tenor)
            spinnerSatuan.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arr).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        // --- hitung otomatis saat mengetik nominal ---
        edtNominal.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateHitungan()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- tombol ajukan menyimpan ke LoanStorage dan navigasi ke LoanHistoryFragment ---
        btnAjukan.setOnClickListener { kirimPengajuan() }

        // initial display
        updateHitungan()

        return view
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
            val bitmap = BitmapFactory.decodeStream(inputStream)
            ktpBitmap = bitmap
            imgKtp.setImageBitmap(bitmap)
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
        } else {
            txtJasa.text = "Jasa Pinjaman (${(RATE*100).toInt()}%): Rp 0"
            txtTotalPengembalian.text = "Total Pengembalian: Rp 0"
            txtDanaDiterima.text = "Total Dana Diterima: Rp 0"
        }
    }

    private fun kirimPengajuan() {
        val jenis = edtJenisPinjaman.text.toString().trim()
        val peruntukan = edtPeruntukan.text.toString().trim()
        val lama = edtLamaPinjaman.text.toString().trim()
        val satuan = spinnerSatuan.selectedItem?.toString() ?: ""
        val nominalText = edtNominal.text.toString().replace("[^0-9]".toRegex(), "")

        if (jenis.isEmpty() || peruntukan.isEmpty() || lama.isEmpty() || nominalText.isEmpty() || ktpBitmap == null) {
            Toast.makeText(requireContext(), "Lengkapi semua data (termasuk foto KTP)!", Toast.LENGTH_SHORT).show()
            return
        }

        val nominal = nominalText.toDouble()
        val bunga = nominal * RATE
        val totalPengembalian = nominal + bunga

        // Simpan ke lokal (LoanStorage) sebagai JSONObject
        val loan = JSONObject().apply {
            put("status", "Proses")
            put("nominal", nominal)
            put("tenor", "$lama $satuan")
            put("tujuan", peruntukan)
            put("bunga", RATE)
            put("sisaAngsuran", totalPengembalian)
            put("hasKtp", true)
            put("isPaid", false) // baru, untuk menandai pinjaman belum dibayar
        }


        LoanStorage.saveLoan(requireContext(), loan)
        Toast.makeText(requireContext(), "Pengajuan berhasil disimpan.", Toast.LENGTH_SHORT).show()

        // Navigasi ke LoanHistoryFragment menggunakan Navigation Component (lebih aman)
        // Pastikan di nav_graph ada fragment dengan id "loanHistoryFragment"
        try {
            findNavController().navigate(R.id.loanHistoryFragment)
        } catch (e: Exception) {
            // fallback: jika nav graph/action belum diset, gunakan parentFragmentManager replace (container id 'nav_host')
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host, LoanHistoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
