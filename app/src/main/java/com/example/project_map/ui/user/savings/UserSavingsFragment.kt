package com.example.project_map.ui.user.savings

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.project_map.R
import com.example.project_map.data.model.Savings
import com.google.android.material.chip.ChipGroup
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale

class UserSavingsFragment : Fragment(R.layout.fragment_user_savings), SimpanUang.OnNominalEntered {

    private val viewModel: UserSavingsViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter
    private lateinit var progressDialog: ProgressDialog

    // UI Refs
    private lateinit var tvTotal: TextView
    private lateinit var tvTotalPokok: TextView
    private lateinit var tvTotalWajib: TextView
    private lateinit var tvTotalSukarela: TextView
    private lateinit var chipGroup: ChipGroup

    private var currentNominal = 0

    // Image Picker Logic
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
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

        setupUI(view)
        setupObservers()
    }

    private fun setupUI(view: View) {
        tvTotal = view.findViewById(R.id.tvTotalSimpanan)
        tvTotalPokok = view.findViewById(R.id.tvTotalPokok)
        tvTotalWajib = view.findViewById(R.id.tvTotalWajib)
        tvTotalSukarela = view.findViewById(R.id.tvTotalSukarela)
        chipGroup = view.findViewById(R.id.chipGroupFilter)

        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Sedang memproses...")
            setCancelable(false)
        }

        // Setup RecyclerView
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = TransactionAdapter(emptyList()) { transaction ->
            showTransactionDetail(transaction)
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Setup Chip Filter
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val filterType = when (checkedIds[0]) {
                R.id.chipWajib -> "Simpanan Wajib"
                R.id.chipPokok -> "Simpanan Pokok"
                R.id.chipSukarela -> "Simpanan Sukarela"
                else -> "Semua"
            }
            viewModel.applyFilter(filterType)
        }

        // Setup Button
        view.findViewById<Button>(R.id.btnTambahSimpanan).setOnClickListener {
            SimpanUang(this).show(parentFragmentManager, "SimpanBottomSheet")
        }
    }

    private fun setupObservers() {
        // Observe Balances
        viewModel.balances.observe(viewLifecycleOwner) { data ->
            tvTotal.text = data["total"]
            tvTotalPokok.text = data["pokok"]
            tvTotalWajib.text = data["wajib"]
            tvTotalSukarela.text = data["sukarela"]
        }

        // Observe Transactions
        viewModel.savingsList.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
        }

        // Observe Loading State
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) progressDialog.show() else progressDialog.dismiss()
        }

        // Observe Toast Messages
        viewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // --- Interaction Logic (Passed from BottomSheet) ---

    override fun onNominalEntered(nominal: Int) {
        currentNominal = nominal
        showBuktiTransferDialog(nominal)
    }

    private fun showBuktiTransferDialog(nominal: Int) {
        // Just the dialog logic to trigger permission check
        AlertDialog.Builder(requireContext())
            .setTitle("Upload Bukti Transfer")
            .setMessage("Upload bukti transfer?")
            .setPositiveButton("Upload") { _, _ -> checkGalleryPermission() }
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
        view.findViewById<ImageView>(R.id.ivPreview).setImageURI(imageUri)

        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                // IMPORTANT: We need to convert URI to File here because ViewModel
                // shouldn't have access to Context/ContentResolver.
                val safeFile = imageUriToTempFile(requireContext(), imageUri)
                if (safeFile != null) {
                    val safeUri = Uri.fromFile(safeFile)
                    viewModel.uploadTransaction(currentNominal.toDouble(), "Simpanan Sukarela", safeUri)
                } else {
                    Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

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
            null
        }
    }

    private fun showTransactionDetail(savings: Savings) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_detail, null)
        val ivBukti = dialogView.findViewById<ImageView>(R.id.ivProof)
        val tvType = dialogView.findViewById<TextView>(R.id.tvType)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
        tvType.text = savings.type
        tvDate.text = if (savings.date != null) dateFormat.format(savings.date) else "-"

        if (savings.type == "Simpanan Sukarela" &&
            !savings.imageUri.isNullOrEmpty() &&
            savings.imageUri!!.startsWith("http")) {

            ivBukti.visibility = View.VISIBLE
            ivBukti.load(savings.imageUri) {
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
}