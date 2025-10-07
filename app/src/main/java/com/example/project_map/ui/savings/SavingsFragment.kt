package com.example.project_map.ui.savings

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SavingsFragment : Fragment(R.layout.fragment_savings), TarikBottomSheet.OnNominalEntered {

    private var totalSimpanan = 10000000
    private lateinit var transaksi: MutableList<Transaction>
    private lateinit var adapter: TransactionAdapter
    private var selectedImageUri: Uri? = null
    private var currentNominal = 0

    // Contract untuk mengambil gambar dari galeri
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                // Tampilkan dialog konfirmasi dengan preview gambar
                showImageConfirmationDialog(uri)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTotal: TextView = view.findViewById(R.id.tvTotalSimpanan)
        val recycler: RecyclerView = view.findViewById(R.id.recyclerView)
        val btnSimpan: Button = view.findViewById(R.id.btnTarik)

        tvTotal.text = formatRupiah(totalSimpanan)
        btnSimpan.text = "Simpan Uang"

        // Inisialisasi data transaksi
        transaksi = mutableListOf(
            Transaction("2025-09-01", "Simpanan Pokok", "+ Rp 5.000.000", TransactionType.DEPOSIT),
            Transaction("2025-09-10", "Simpanan Wajib", "+ Rp 1.000.000", TransactionType.DEPOSIT),
            Transaction("2025-09-15", "Penarikan", "- Rp 500.000", TransactionType.WITHDRAWAL),
            Transaction("2025-09-20", "Simpanan Wajib", "+ Rp 1.500.000", TransactionType.DEPOSIT),
            Transaction("2025-09-25", "Simpanan Sukarela", "+ Rp 3.000.000", TransactionType.DEPOSIT)
        )

        adapter = TransactionAdapter(transaksi)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        btnSimpan.setOnClickListener {
            TarikBottomSheet(this).show(parentFragmentManager, "SimpanBottomSheet")
        }

        // Jadwalkan simpanan wajib setiap 3 detik untuk demo
        scheduleMonthlySavingsForDemo()
    }

    override fun onNominalEntered(nominal: Int) {
        currentNominal = nominal
        // Tampilkan dialog untuk meminta bukti transfer
        showBuktiTransferDialog(nominal)
    }

    private fun showBuktiTransferDialog(nominal: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Bukti Transfer")
            .setMessage("Silakan upload bukti transfer untuk penyetoran sebesar ${formatRupiah(nominal)}")
            .setPositiveButton("Upload Bukti") { dialog, _ ->
                dialog.dismiss()
                openImagePicker()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun showImageConfirmationDialog(imageUri: Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Bukti Transfer")
            .setMessage("Bukti transfer telah dipilih. Lanjutkan penyimpanan?")
            .setPositiveButton("Lanjutkan") { dialog, _ ->
                dialog.dismiss()
                processSavingsAfterImageUpload(currentNominal)
            }
            .setNegativeButton("Ganti Gambar") { dialog, _ ->
                dialog.dismiss()
                openImagePicker()
            }
            .show()
    }

    private fun processSavingsAfterImageUpload(nominal: Int) {
        // Update total simpanan
        totalSimpanan += nominal
        view?.findViewById<TextView>(R.id.tvTotalSimpanan)?.text = formatRupiah(totalSimpanan)

        // Tambahkan transaksi baru
        val newTransaction = Transaction(
            tanggal = getCurrentDate(),
            keterangan = "Simpanan Sukarela",
            jumlah = "+ ${formatRupiah(nominal)}",
            type = TransactionType.DEPOSIT
        )
        adapter.addTransaction(newTransaction)

        // Tampilkan pesan sukses
        AlertDialog.Builder(requireContext())
            .setTitle("Berhasil")
            .setMessage("Penyimpanan uang berhasil dilakukan")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun scheduleMonthlySavingsForDemo() {
        val executor = Executors.newSingleThreadScheduledExecutor()

        // Jadwalkan simpanan wajib setiap 30 detik untuk demo
        executor.scheduleAtFixedRate({
            requireActivity().runOnUiThread {
                addMonthlySavings()
            }
        }, 30, 30, TimeUnit.SECONDS)
    }

    private fun addMonthlySavings() {
        val monthlyAmount = 1500000
        totalSimpanan += monthlyAmount

        view?.findViewById<TextView>(R.id.tvTotalSimpanan)?.text = formatRupiah(totalSimpanan)

        val monthlyTransaction = Transaction(
            tanggal = getCurrentDate(),
            keterangan = "Simpanan Wajib",
            jumlah = "+ ${formatRupiah(monthlyAmount)}",
            type = TransactionType.DEPOSIT
        )
        adapter.addTransaction(monthlyTransaction)

        // Tampilkan notifikasi toast atau snackbar untuk demo
        android.widget.Toast.makeText(
            requireContext(),
            "Simpanan wajib Rp 1.500.000 telah ditambahkan",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun formatRupiah(amount: Int): String {
        return "Rp %,d".format(amount).replace(',', '.')
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}

enum class TransactionType {
    DEPOSIT, WITHDRAWAL
}