package com.example.project_map.ui.savings

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.Transaction
import com.example.project_map.data.repository.SavingsRepository
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import coil.load

class SavingsFragment : Fragment(R.layout.fragment_savings), TarikBottomSheet.OnNominalEntered {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val repository = SavingsRepository()

    // UI Components
    private lateinit var tvTotal: TextView
    private lateinit var tvTotalPokok: TextView
    private lateinit var tvTotalWajib: TextView
    private lateinit var tvTotalSukarela: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var recycler: RecyclerView
    private lateinit var btnSimpan: Button

    private lateinit var progressDialog: ProgressDialog

    // Data
    private var totalSimpanan = 0.0
    private var totalPokok = 0.0
    private var totalWajib = 0.0
    private var totalSukarela = 0.0

    private var allTransactions: List<Transaction> = emptyList()
    private lateinit var adapter: TransactionAdapter

    private var selectedImageUri: Uri? = null
    private var currentNominal = 0

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                showImageConfirmationDialog(uri)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openImagePicker()
        else Toast.makeText(requireContext(), "Izin akses galeri ditolak", Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Sedang mengupload bukti transfer...")
            setCancelable(false)
        }

        tvTotal = view.findViewById(R.id.tvTotalSimpanan)
        tvTotalPokok = view.findViewById(R.id.tvTotalPokok)
        tvTotalWajib = view.findViewById(R.id.tvTotalWajib)
        tvTotalSukarela = view.findViewById(R.id.tvTotalSukarela)
        chipGroup = view.findViewById(R.id.chipGroupFilter)
        recycler = view.findViewById(R.id.recyclerView)
        btnSimpan = view.findViewById(R.id.btnTambahSimpanan)

        adapter = TransactionAdapter(ArrayList<Transaction>()) { transaction ->
            showTransactionDetail(transaction)
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val checkedId = checkedIds[0]
            val filterType = when (checkedId) {
                R.id.chipWajib -> "Simpanan Wajib"
                R.id.chipPokok -> "Simpanan Pokok"
                R.id.chipSukarela -> "Simpanan Sukarela"
                else -> "Semua"
            }
            filterTransactions(filterType)
        }

        btnSimpan.setOnClickListener {
            TarikBottomSheet(this).show(parentFragmentManager, "SimpanBottomSheet")
        }

        fetchUserDataAndTotals()
        fetchTransactionHistory()
    }

    private fun fetchUserDataAndTotals() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .addSnapshotListener { document, e ->
                if (e != null) return@addSnapshotListener
                if (!isAdded) return@addSnapshotListener

                if (document != null && document.exists()) {
                    totalSimpanan = document.getDouble("totalSimpanan") ?: 0.0
                    totalPokok = document.getDouble("simpananPokok") ?: 0.0
                    totalWajib = document.getDouble("simpananWajib") ?: 0.0
                    totalSukarela = document.getDouble("simpananSukarela") ?: 0.0

                    tvTotal.text = formatRupiah(totalSimpanan)
                    tvTotalPokok.text = formatRupiah(totalPokok)
                    tvTotalWajib.text = formatRupiah(totalWajib)
                    tvTotalSukarela.text = formatRupiah(totalSukarela)
                }
            }
    }

    private fun fetchTransactionHistory() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (!isAdded) return@addSnapshotListener

                if (snapshots != null) {
                    val rawList = snapshots.toObjects(Transaction::class.java)

                    // --- FIX 1: DATA SANITIZER ---
                    // This prevents the app from crashing when it sees old "content://" links
                    allTransactions = rawList.map { t ->
                        if (t.imageUri != null && t.imageUri!!.startsWith("content://")) {
                            // If it's a bad link, we copy the object but set imageUri to null
                            // Assuming Transaction is a Data Class. If it's not, just remove this map block.
                            try {
                                t.copy(imageUri = null)
                            } catch (e: Exception) {
                                // Fallback if copy() doesn't exist (e.g. if it's not a data class)
                                t.apply {
                                    // Try to set it via reflection or direct access if var
                                    // If you can't edit it, we just return t and let the adapter handle errors
                                    // (but usually data classes have copy)
                                }
                                t
                            }
                        } else {
                            t
                        }
                    }

                    val checkedId = if (chipGroup.checkedChipIds.isNotEmpty()) chipGroup.checkedChipIds[0] else R.id.chipAll
                    val filterType = when (checkedId) {
                        R.id.chipWajib -> "Simpanan Wajib"
                        R.id.chipPokok -> "Simpanan Pokok"
                        R.id.chipSukarela -> "Simpanan Sukarela"
                        else -> "Semua"
                    }
                    filterTransactions(filterType)
                }
            }
    }

    private fun filterTransactions(filter: String) {
        val filtered = if (filter == "Semua") allTransactions else allTransactions.filter {
            it.type == filter
        }
        adapter.updateList(filtered)
    }

    override fun onNominalEntered(nominal: Int) {
        currentNominal = nominal
        showBuktiTransferDialog(nominal)
    }

    private fun showBuktiTransferDialog(nominal: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Upload Bukti Transfer")
            .setMessage("Upload bukti transfer untuk Simpanan Sukarela sebesar ${formatRupiah(nominal.toDouble())}")
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
            .setTitle("Konfirmasi")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                uploadAndSaveTransaction(currentNominal.toDouble(), "Simpanan Sukarela", imageUri)
            }
            .setNegativeButton("Ganti", null)
            .show()
    }

    // --- FIX 2: TEMP FILE HANDLING ---

    private fun imageUriToTempFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadAndSaveTransaction(amount: Double, type: String, originalUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        progressDialog.show()

        lifecycleScope.launch {
            try {
                // Step A: Convert unsafe URI to safe local file
                val localFile = imageUriToTempFile(requireContext(), originalUri)

                if (localFile != null) {
                    val localUri = Uri.fromFile(localFile)

                    // Step B: Upload using the SAFE local URI
                    val imageUrl = repository.uploadProofToCloudinary(userId, localUri)

                    // Step C: Save to Firestore
                    repository.saveTransaction(userId, amount, type, imageUrl)

                    Toast.makeText(context, "Berhasil disimpan!", Toast.LENGTH_SHORT).show()

                    localFile.delete()
                } else {
                    Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun showTransactionDetail(transaction: Transaction) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_detail, null)
        val ivBukti = dialogView.findViewById<ImageView>(R.id.ivProof)
        val tvType = dialogView.findViewById<TextView>(R.id.tvType)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
        val dateStr = if (transaction.date != null) dateFormat.format(transaction.date) else "-"

        tvType.text = transaction.type
        tvDate.text = dateStr

        // FIX 3: Check protocol before loading to avoid crash in Detail Dialog too
        if (transaction.type == "Simpanan Sukarela" &&
            !transaction.imageUri.isNullOrEmpty() &&
            transaction.imageUri!!.startsWith("http")) { // Only load http/https

            ivBukti.visibility = View.VISIBLE
            ivBukti.load(transaction.imageUri) {
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
            }
        } else {
            ivBukti.visibility = View.GONE
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}