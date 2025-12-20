package com.example.project_map.ui.user.savings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Locale

class UserSavingsFragment : Fragment() {

    private var _binding: FragmentUserSavingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserSavingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserSavingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // History Setup
        binding.rvHistory.layoutManager = LinearLayoutManager(context)

        setupObservers()
        setupActions()
    }

    private fun setupObservers() {
        viewModel.balances.observe(viewLifecycleOwner) { data ->
            binding.tvTotalBalance.text = data["totalFormatted"]
            binding.cardPokok.tvTitle.text = "Simpanan Pokok"; binding.cardPokok.tvAmount.text = data["pokokFormatted"]
            binding.cardWajib.tvTitle.text = "Simpanan Wajib"; binding.cardWajib.tvAmount.text = data["wajibFormatted"]
            binding.cardSukarela.tvTitle.text = "Simpanan Sukarela"; binding.cardSukarela.tvAmount.text = data["sukarelaFormatted"]
        }

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

        // History List
        viewModel.history.observe(viewLifecycleOwner) { list ->
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))

            val uiList = list.map { saving ->
                val type = if (savings.type.contains("Penarikan")) TransactionType.WITHDRAWAL else TransactionType.SAVINGS

                UserRecentItem(
                    title = transaction.type,
                    date = if (transaction.date != null) sdf.format(transaction.date) else "-",
                    amount = transaction.amount.toString(),
                    type = type
                )
            }
            binding.rvHistory.adapter = UserRecentAdapter(uiList)
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                if (result.isSuccess) Toast.makeText(context, result.getOrNull(), Toast.LENGTH_LONG).show()
                else Toast.makeText(context, "Gagal: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                viewModel.resetResult()
            }
        }
    }

    private fun setupActions() {
        binding.btnPayDirect.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Instruksi Pembayaran")
                .setMessage("Transfer Rp 200.000 ke BCA 12345678 a.n Koperasi.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showTransactionSheet(isDeposit: Boolean) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_transaction, null)
        dialog.setContentView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvSheetTitle)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etAmount)
        val layoutBank = view.findViewById<View>(R.id.layoutBank) // Find layout wrapper or Edit Text
        val etBank = view.findViewById<TextInputEditText>(R.id.etBank)
        val layoutAccount = view.findViewById<View>(R.id.layoutAccount)
        val etAccount = view.findViewById<TextInputEditText>(R.id.etAccount)
        val btnSubmit = view.findViewById<View>(R.id.btnSubmit)

        if (isDeposit) {
            tvTitle.text = "Isi Saldo (Deposit)"
            layoutBank.visibility = View.GONE
            layoutAccount.visibility = View.GONE
        } else {
            tvTitle.text = "Penarikan Dana"
            layoutBank.visibility = View.VISIBLE
            layoutAccount.visibility = View.VISIBLE
        }

        btnSubmit.setOnClickListener {
            val amountStr = etAmount.text.toString()
            if (amountStr.isEmpty()) return@setOnClickListener

            if (isDeposit) {
                viewModel.submitDeposit(amountStr.toDouble())
            } else {
                val bank = etBank.text.toString()
                val acc = etAccount.text.toString()

                if (bank.isNotEmpty() && acc.isNotEmpty()) {
                    viewModel.submitWithdrawal(amountStr.toDouble(), bank, acc)
                } else {
                    Toast.makeText(context, "Lengkapi data bank", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun showRestrictedToast() {
        Toast.makeText(context, "Selesaikan aktivasi keanggotaan terlebih dahulu.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}