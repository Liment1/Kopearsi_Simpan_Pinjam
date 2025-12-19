package com.example.project_map.ui.user.loans

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.data.model.Installment
import com.example.project_map.databinding.FragmentAngsuranBinding
import java.text.NumberFormat
import java.util.Locale

class UserAngsuranFragment : Fragment() {

    private var _binding: FragmentAngsuranBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserLoanViewModel by activityViewModels()

    private var currentInstallment: Installment? = null
    private var proofUri: Uri? = null
    private var paymentMethod = "TRANSFER" // Default

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            proofUri = result.data?.data
            binding.tvUploadStatus.text = "Bukti dipilih."
            binding.tvUploadStatus.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAngsuranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // 1. Recover Data
        val loanId = arguments?.getString("loanId")
        val instId = arguments?.getString("installmentId")
        val amount = arguments?.getDouble("amount") ?: 0.0

        currentInstallment = viewModel.installments.value?.find { it.id == instId }
            ?: Installment(id = instId ?: "", loanId = loanId ?: "", jumlahBayar = amount, status = "Belum Bayar")

        // 2. Setup UI
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 }
        binding.tvAmountDue.text = fmt.format(amount)

        // 3. Radio Button Logic (Toggle)
        binding.rgMetodePembayaran.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbPotongSaldo) {
                paymentMethod = "SALDO"
                binding.btnUploadBukti.visibility = View.GONE
                binding.tvUploadStatus.visibility = View.GONE
                binding.tvInstructions.text = "Saldo simpanan Anda akan dipotong otomatis." // Make sure you have this TextView or reuse status
            } else {
                paymentMethod = "TRANSFER"
                binding.btnUploadBukti.visibility = View.VISIBLE
                binding.tvInstructions.text = "Silakan transfer dan upload bukti."
            }
        }

        binding.btnUploadBukti.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        // 4. Handle Pay Button
        binding.btnBayar.setOnClickListener {
            if (paymentMethod == "TRANSFER" && proofUri == null) {
                Toast.makeText(context, "Mohon upload bukti transfer", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.payInstallment(currentInstallment!!, paymentMethod, proofUri)
            }
        }

        // 5. Observe Result
        viewModel.loanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UserLoanViewModel.State.Idle -> {
                    // Do nothing, waiting for user input
                    binding.btnBayar.text = "Konfirmasi Pembayaran"
                    binding.btnBayar.isEnabled = true
                }
                is UserLoanViewModel.State.Loading -> {
                    binding.btnBayar.text = "Memproses..."
                    binding.btnBayar.isEnabled = false
                }
                is UserLoanViewModel.State.Success -> {
                    Toast.makeText(context, "Pembayaran Berhasil!", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
                is UserLoanViewModel.State.Error -> {
                    binding.btnBayar.text = "Konfirmasi Pembayaran"
                    binding.btnBayar.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}