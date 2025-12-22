package com.example.project_map.ui.user.savings

import androidx.appcompat.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.databinding.FragmentUserSavingsBinding
import com.example.project_map.ui.user.home.TransactionType
import com.example.project_map.ui.user.home.UserRecentAdapter
import com.example.project_map.ui.user.home.UserRecentItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Locale

class UserSavingsFragment : Fragment() {

    private var _binding: FragmentUserSavingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserSavingsViewModel by viewModels()

    // --- UI Components ---
    private var loadingDialog: AlertDialog? = null

    // --- Image Picker Logic ---
    private var selectedImageUri: Uri? = null
    private var ivPreviewRef: ImageView? = null
    private var btnUploadRef: Button? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Update the UI inside the BottomSheet
            ivPreviewRef?.setImageURI(uri)
            ivPreviewRef?.visibility = View.VISIBLE
            btnUploadRef?.text = "Ganti Foto"
            btnUploadRef?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserSavingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup UI elements
        setupLoadingDialog()
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.rvHistory.layoutManager = LinearLayoutManager(context)

        // 2. Initialize
        setupObservers()
        setupActions()
    }

    private fun setupLoadingDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_loading, null)
        builder.setView(view)
        builder.setCancelable(false)
        loadingDialog = builder.create()
    }

    private fun setupObservers() {
        viewModel.balances.observe(viewLifecycleOwner) { data ->
            binding.tvTotalBalance.text = data["totalFormatted"]
            binding.cardPokok.tvTitle.text = "Simpanan Pokok"; binding.cardPokok.tvAmount.text = data["pokokFormatted"]
            binding.cardWajib.tvTitle.text = "Simpanan Wajib"; binding.cardWajib.tvAmount.text = data["wajibFormatted"]
            binding.cardSukarela.tvTitle.text = "Simpanan Sukarela"; binding.cardSukarela.tvAmount.text = data["sukarelaFormatted"]
        }

        // User Status (Lock/Unlock features)
        viewModel.userStatus.observe(viewLifecycleOwner) { status ->
            if (status == "Calon Anggota") {
                binding.cardActivation.visibility = View.VISIBLE
                binding.layoutActions.alpha = 0.5f
                binding.btnDeposit.setOnClickListener { showRestrictedToast() }
                binding.btnWithdraw.setOnClickListener { showRestrictedToast() }
            } else {
                binding.cardActivation.visibility = View.GONE
                binding.layoutActions.alpha = 1.0f
                binding.btnDeposit.setOnClickListener { showTransactionSheet(isDeposit = true) }
                binding.btnWithdraw.setOnClickListener { showTransactionSheet(isDeposit = false) }
            }
        }

        // Loading State
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) loadingDialog?.show() else loadingDialog?.dismiss()
        }

        // Action Results (Success/Failure)
        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                if (result.isSuccess) {
                    Snackbar.make(binding.root, result.getOrNull() ?: "Berhasil", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(requireContext().getColor(android.R.color.holo_green_dark))
                        .setTextColor(requireContext().getColor(android.R.color.white))
                        .show()
                } else {
                    Snackbar.make(binding.root, "Gagal: ${result.exceptionOrNull()?.message}", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(requireContext().getColor(android.R.color.holo_red_dark))
                        .setTextColor(requireContext().getColor(android.R.color.white))
                        .show()
                }
                viewModel.resetResult()
            }
        }

        // History List
        viewModel.history.observe(viewLifecycleOwner) { list ->
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
            val uiList = list.map { saving ->
                val type = if (saving.type.contains("Penarikan")) TransactionType.WITHDRAWAL else TransactionType.SAVINGS
                UserRecentItem(
                    title = saving.type,
                    date = if (saving.date != null) sdf.format(saving.date) else "-",
                    amount = saving.amount.toString(),
                    type = type
                )
            }
            binding.rvHistory.adapter = UserRecentAdapter(uiList)
        }
    }

    private fun setupActions() {
        binding.btnPayDirect.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Instruksi Pembayaran")
                .setMessage("Transfer ke BCA 12345678 a.n Koperasi.")
                .setPositiveButton("Tutup", null)
                .show()
        }
    }

    private fun showTransactionSheet(isDeposit: Boolean) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_transaction, null)
        dialog.setContentView(view)

        // Reset variables for new sheet instance
        selectedImageUri = null
        ivPreviewRef = null
        btnUploadRef = null

        // --- Common Views ---
        val tvTitle = view.findViewById<TextView>(R.id.tvSheetTitle)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etAmount)
        val layoutAmount = view.findViewById<TextInputLayout>(R.id.layoutAmountInput)

        // --- Deposit Views ---
        val layoutDeposit = view.findViewById<View>(R.id.layoutDeposit)
        val btnUpload = view.findViewById<Button>(R.id.btnUploadProof)
        val ivPreview = view.findViewById<ImageView>(R.id.ivProofPreview)

        // --- Withdrawal Views ---
        val layoutWithdrawal = view.findViewById<View>(R.id.layoutWithdrawal)
        val spinnerBank = view.findViewById<Spinner>(R.id.spinnerBank)
        val layoutManualBank = view.findViewById<TextInputLayout>(R.id.layoutManualBank)
        val etManualBank = view.findViewById<TextInputEditText>(R.id.etManualBank)
        val layoutAccount = view.findViewById<TextInputLayout>(R.id.layoutAccountInput)
        val etAccount = view.findViewById<TextInputEditText>(R.id.etAccount)

        if (isDeposit) {
            // Setup Deposit UI
            tvTitle.text = "Isi Simpanan Sukarela"
            layoutDeposit.visibility = View.VISIBLE
            layoutWithdrawal.visibility = View.GONE
            btnSubmit.text = "Kirim Bukti Pembayaran"

            // Connect references for Image Picker
            ivPreviewRef = ivPreview
            btnUploadRef = btnUpload

            btnUpload.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }

        } else {
            // Setup Withdrawal UI
            tvTitle.text = "Penarikan Dana"
            layoutDeposit.visibility = View.GONE
            layoutWithdrawal.visibility = View.VISIBLE
            btnSubmit.text = "Ajukan Penarikan"

            // Configure Spinner
            val banks = arrayOf("Pilih Bank", "BCA", "CIMB Niaga", "Mandiri", "BRI", "Lainnya")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, banks)
            spinnerBank.adapter = adapter

            spinnerBank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (banks[position] == "Lainnya") {
                        layoutManualBank.visibility = View.VISIBLE
                    } else {
                        layoutManualBank.visibility = View.GONE
                        layoutManualBank.error = null // Clear error if switching away
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        btnSubmit.setOnClickListener {
            // --- 1. VALIDATE AMOUNT ---
            val amountStr = etAmount.text.toString()
            if (amountStr.isEmpty()) {
                layoutAmount.error = "Wajib diisi"
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount < 10000) {
                layoutAmount.error = "Minimal Rp 10.000"
                return@setOnClickListener
            }
            layoutAmount.error = null // Clear error

            if (isDeposit) {
                // --- 2. VALIDATE DEPOSIT ---
                if (selectedImageUri == null) {
                    Snackbar.make(binding.root, "Harap unggah bukti transfer", Snackbar.LENGTH_SHORT)
                        .setAnchorView(binding.btnDeposit) // Show above bottom nav if present
                        .show()
                    Toast.makeText(context, "Unggah bukti transfer!", Toast.LENGTH_SHORT).show() // Backup feedback
                    return@setOnClickListener
                }

                // Submit
                viewModel.submitDeposit(amount, selectedImageUri!!)
                dialog.dismiss()

            } else {
                // --- 3. VALIDATE WITHDRAWAL ---
                val selectedBank = spinnerBank.selectedItem.toString()
                val finalBankName = if (selectedBank == "Lainnya") etManualBank.text.toString() else selectedBank
                val accNumber = etAccount.text.toString()

                if (selectedBank == "Pilih Bank") {
                    Toast.makeText(context, "Silakan pilih bank tujuan", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (selectedBank == "Lainnya" && finalBankName.isEmpty()) {
                    layoutManualBank.error = "Nama bank wajib diisi"
                    return@setOnClickListener
                }

                if (accNumber.isEmpty()) {
                    layoutAccount.error = "Nomor rekening wajib diisi"
                    return@setOnClickListener
                }
                // Validate Account: Must be digits and reasonably long (10-16)
                if (!accNumber.matches(Regex("^[0-9]{10,16}$"))) {
                    layoutAccount.error = "Nomor rekening tidak valid (10-16 angka)"
                    return@setOnClickListener
                }
                layoutAccount.error = null

                // Submit
                viewModel.submitWithdrawal(amount, finalBankName, accNumber)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showRestrictedToast() {
        Snackbar.make(binding.root, "Selesaikan aktivasi keanggotaan terlebih dahulu.", Snackbar.LENGTH_SHORT)
            .setBackgroundTint(requireContext().getColor(android.R.color.holo_orange_dark))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}