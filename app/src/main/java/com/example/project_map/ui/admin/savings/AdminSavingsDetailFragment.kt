package com.example.project_map.ui.admin.savings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.project_map.R
import com.example.project_map.databinding.FragmentAdminSavingsDetailBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class AdminSavingsDetailFragment : Fragment() {

    private var _binding: FragmentAdminSavingsDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminSavingsDetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminSavingsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transId = arguments?.getString("transactionId") ?: return
        val userId = arguments?.getString("userId") ?: return

        // Load Data
        viewModel.loadData(transId, userId)

        // Observe Data
        viewModel.savings.observe(viewLifecycleOwner) { savings ->
            if (savings != null) {
                val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                format.maximumFractionDigits = 0
                val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))

                // 1. Set Texts
                binding.tvAmount.text = format.format(savings.amount)

                val dateStr = if (savings.date != null) sdf.format(savings.date) else "-"
                binding.tvDetails.text = "Tipe: ${savings.type}\nStatus: ${savings.status}\nTanggal: $dateStr"

                // 2. Proof Logic
                if (!savings.proofUrl.isNullOrEmpty()) {
                    binding.tvProofLabel.visibility = View.VISIBLE
                    binding.ivProof.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(savings.proofUrl)
                        .placeholder(R.drawable.ic_menu_gallery)
                        .into(binding.ivProof)
                } else {
                    binding.tvProofLabel.visibility = View.GONE
                    binding.ivProof.visibility = View.GONE
                }
            }
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = user.name
                binding.tvUserEmail.text = user.email
                if (user.avatarUrl.isNotEmpty()) {
                    Glide.with(this).load(user.avatarUrl).circleCrop().into(binding.ivUserAvatar)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}