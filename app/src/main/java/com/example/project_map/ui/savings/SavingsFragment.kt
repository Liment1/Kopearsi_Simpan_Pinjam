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
import com.example.project_map.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
// IMPORT THE DATA PACKAGE
import com.example.project_map.data.Transaction

class SavingsFragment : Fragment(R.layout.fragment_savings), TarikBottomSheet.OnNominalEntered {

    // Define new views
    private lateinit var tvTotalPokok: TextView
    private lateinit var tvTotalWajib: TextView
    private lateinit var tvTotalSukarela: TextView

    // Firebase
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Data
    private var totalSimpanan = 0.0
    private var totalPokok = 0.0
    private var totalWajib = 0.0
    private var totalSukarela = 0.0

    private var allTransactions: List<Transaction> = emptyList()
    private lateinit var adapter: TransactionAdapter

    // UI
    private lateinit var tvTotal: TextView
    private lateinit var spinnerFilter: Spinner
    private lateinit var recycler: RecyclerView

    // Upload Logic
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

        tvTotalPokok = view.findViewById(R.id.tvTotalPokok)
        tvTotalWajib = view.findViewById(R.id.tvTotalWajib)
        tvTotalSukarela = view.findViewById(R.id.tvTotalSukarela)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvTotal = view.findViewById(R.id.tvTotalSimpanan)
        spinnerFilter = view.findViewById(R.id.spinnerFilter)
        recycler = view.findViewById(R.id.recyclerView)
        val btnSimpan: Button = view.findViewById(R.id.btnTambahSimpanan)

        // --- FIX HERE: Explicitly specify the type <Transaction> ---
        adapter = TransactionAdapter(ArrayList<Transaction>(), ::showTransactionDetail)

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

                if (document != null && document.exists()) {
                    totalSimpanan = document.getDouble("totalSimpanan") ?: 0.0
                    totalPokok = document.getDouble("simpananPokok") ?: 0.0
                    totalWajib = document.getDouble("simpananWajib") ?: 0.0
                    totalSukarela = document.getDouble("simpananSukarela") ?: 0.0

                    // UPDATE ALL TEXT VIEWS SEPARATELY
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

                if (snapshots != null) {
                    allTransactions = snapshots.toObjects(Transaction::class.java)
                    val currentFilter = spinnerFilter.selectedItem?.toString() ?: "Semua"
                    filterTransactions(currentFilter)
                }
            }
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
                saveToFirestore(currentNominal.toDouble(), "Simpanan Sukarela", imageUri.toString())
            }
            .setNegativeButton("Ganti", null)
            .show()
    }

    private fun saveToFirestore(amount: Double, type: String, imageUriString: String?) {
        val userId = auth.currentUser?.uid ?: return
        val batch = db.batch()

        val userRef = db.collection("users").document(userId)
        val newTransRef = userRef.collection("savings").document()

        val transaction = Transaction(
            id = newTransRef.id,
            date = Date(),
            type = type,
            amount = amount,
            description = "Setoran $type",
            imageUri = imageUriString
        )

        batch.set(newTransRef, transaction)
        batch.update(userRef, "totalSimpanan", FieldValue.increment(amount))

        val fieldToUpdate = when (type) {
            "Simpanan Pokok" -> "simpananPokok"
            "Simpanan Wajib" -> "simpananWajib"
            "Simpanan Sukarela" -> "simpananSukarela"
            else -> "simpananSukarela"
        }
        batch.update(userRef, fieldToUpdate, FieldValue.increment(amount))

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(context, "Berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterTransactions(filter: String) {
        val filtered = if (filter == "Semua") allTransactions else allTransactions.filter {
            it.type == filter
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

    private fun showTransactionDetail(transaction: Transaction) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_detail, null)
        val ivBukti = dialogView.findViewById<ImageView>(R.id.ivProof)
        val tvType = dialogView.findViewById<TextView>(R.id.tvType)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
        val dateStr = if (transaction.date != null) dateFormat.format(transaction.date) else "-"

        tvType.text = transaction.type
        tvDate.text = dateStr

        if (transaction.type == "Simpanan Sukarela" && !transaction.imageUri.isNullOrEmpty()) {
            ivBukti.visibility = View.VISIBLE
            ivBukti.setImageURI(Uri.parse(transaction.imageUri))
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