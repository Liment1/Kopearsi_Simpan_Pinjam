package com.example.project_map.ui.savings

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.savings.TransactionType
import com.example.project_map.R
import java.text.SimpleDateFormat
import java.util.*

class SavingsFragment : Fragment(R.layout.fragment_savings), TarikBottomSheet.OnNominalEntered {

    private var totalSimpanan = 10_000_000
    private var totalPokok = 5_000_000
    private var totalWajib = 1_000_000
    private var totalSukarela = 3_000_000

    private lateinit var transaksi: MutableList<Transaction>
    private lateinit var adapter: TransactionAdapter
    private var selectedImageUri: Uri? = null
    private var currentNominal = 0

    private lateinit var tvTotal: TextView
    private lateinit var spinnerFilter: Spinner
    private lateinit var recycler: RecyclerView
    private lateinit var handler: Handler

    // Untuk memilih gambar
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                showImageConfirmationDialog(uri)
            }
        }
    }

    // Permission launcher (Android 13+ pakai READ_MEDIA_IMAGES)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openImagePicker()
        else Toast.makeText(requireContext(), "Izin akses galeri ditolak", Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTotal = view.findViewById(R.id.tvTotalSimpanan)
        spinnerFilter = view.findViewById(R.id.spinnerFilter)
        recycler = view.findViewById(R.id.recyclerView)
        val btnSimpan: Button = view.findViewById(R.id.btnTambahSimpanan)

        updateTotals()

        transaksi = mutableListOf(
            Transaction("2025-09-01 08:10:00", "Simpanan Pokok", "+ Rp 5.000.000", TransactionType.DEPOSIT, null),
            Transaction("2025-09-10 10:30:00", "Simpanan Wajib", "+ Rp 1.000.000", TransactionType.DEPOSIT, null),
            Transaction("2025-09-25 09:15:00", "Simpanan Sukarela", "+ Rp 3.000.000", TransactionType.DEPOSIT, null)
        )

        adapter = TransactionAdapter(transaksi) { transaction ->
            showTransactionDetail(transaction)
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val options = resources.getStringArray(R.array.filter_options)
        spinnerFilter.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, options)
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                filterTransactions(options[pos])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Tombol Simpan otomatis masuk ke Simpanan Sukarela
        btnSimpan.setOnClickListener {
            TarikBottomSheet(this).show(parentFragmentManager, "SimpanBottomSheet")
        }

        // Handler untuk simpanan wajib otomatis setiap 30 detik
        handler = Handler(Looper.getMainLooper())
        startAutoSimpananWajib()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    // Auto-simpanan wajib (simulasi tiap 30 detik)
    private fun startAutoSimpananWajib() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val nominalWajib = 100_000 // contoh nominal wajib per bulan
                totalWajib += nominalWajib
                totalSimpanan += nominalWajib

                val transaction = Transaction(
                    tanggal = getCurrentDate(),
                    keterangan = "Simpanan Wajib",
                    jumlah = "+ ${formatRupiah(nominalWajib)}",
                    type = TransactionType.DEPOSIT,
                    imageUri = null
                )

                transaksi.add(0, transaction)
                adapter.updateList(transaksi)
                updateTotals()

                Toast.makeText(requireContext(), "Simpanan wajib otomatis berhasil ditambahkan", Toast.LENGTH_SHORT).show()

                handler.postDelayed(this, 30_000) // setiap 30 detik
            }
        }, 30_000)
    }

    override fun onNominalEntered(nominal: Int) {
        currentNominal = nominal
        showBuktiTransferDialog(nominal)
    }

    private fun showBuktiTransferDialog(nominal: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Upload Bukti Transfer")
            .setMessage("Upload bukti transfer untuk Simpanan Sukarela sebesar ${formatRupiah(nominal)}")
            .setPositiveButton("Upload") { dialog, _ ->
                dialog.dismiss()
                checkGalleryPermission()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun checkGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun showImageConfirmationDialog(imageUri: Uri) {
        val view = layoutInflater.inflate(R.layout.dialog_image_preview, null)
        val ivPreview = view.findViewById<ImageView>(R.id.ivPreview)
        ivPreview.setImageURI(imageUri)

        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Bukti Transfer")
            .setView(view)
            .setPositiveButton("Lanjutkan") { _, _ ->
                processSavingsAfterUpload(currentNominal, imageUri)
            }
            .setNegativeButton("Ganti Gambar") { _, _ ->
                openImagePicker()
            }
            .show()
    }

    private fun processSavingsAfterUpload(nominal: Int, imageUri: Uri) {
        totalSukarela += nominal
        totalSimpanan += nominal
        updateTotals()

        val newTransaction = Transaction(
            tanggal = getCurrentDate(),
            keterangan = "Simpanan Sukarela",
            jumlah = "+ ${formatRupiah(nominal)}",
            type = TransactionType.DEPOSIT,
            imageUri = imageUri.toString()
        )

        transaksi.add(0, newTransaction)
        adapter.updateList(transaksi)

        AlertDialog.Builder(requireContext())
            .setTitle("Berhasil")
            .setMessage("Simpanan Sukarela sebesar ${formatRupiah(nominal)} berhasil disimpan.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showTransactionDetail(transaction: Transaction) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_detail, null)
        val ivBukti = dialogView.findViewById<ImageView>(R.id.ivProof)
        val tvType = dialogView.findViewById<TextView>(R.id.tvType)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)
        val tvTime = dialogView.findViewById<TextView>(R.id.tvTime)

        tvType.text = transaction.keterangan
        val (date, time) = transaction.tanggal.split(" ")
        tvDate.text = "Tanggal: $date"
        tvTime.text = "Jam: $time"

        // Cek apakah jenis simpanan adalah "Simpanan Sukarela"
        if (transaction.keterangan == "Simpanan Sukarela") {
            // Jika ya, tampilkan ImageView dan atur gambarnya
            ivBukti.visibility = View.VISIBLE
            if (transaction.imageUri.isNullOrEmpty()) {
                ivBukti.setImageResource(R.drawable.placeholder_image)
            } else {
                ivBukti.setImageURI(Uri.parse(transaction.imageUri))
            }
        } else {
            // Jika bukan, sembunyikan ImageView
            ivBukti.visibility = View.GONE
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun filterTransactions(filter: String) {
        val filtered = if (filter == "Semua") transaksi else transaksi.filter {
            it.keterangan == filter
        }
        adapter.updateList(filtered)
        updateTotalsByFilter(filter)
    }

    private fun updateTotalsByFilter(filter: String) {
        val totalFiltered = when (filter) {
            "Simpanan Pokok" -> totalPokok
            "Simpanan Wajib" -> totalWajib
            "Simpanan Sukarela" -> totalSukarela
            else -> totalSimpanan
        }
        tvTotal.text = formatRupiah(totalFiltered)
    }

    private fun updateTotals() {
        tvTotal.text = formatRupiah(totalSimpanan)
    }

    private fun formatRupiah(amount: Int): String = "Rp %,d".format(amount).replace(',', '.')
    private fun getCurrentDate(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
}