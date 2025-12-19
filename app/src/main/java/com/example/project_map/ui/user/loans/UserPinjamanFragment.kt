package com.example.project_map.ui.user.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.data.model.Loan
import com.example.project_map.databinding.FragmentPinjamanBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class
UserPinjamanFragment : Fragment() {

    private var _binding: FragmentPinjamanBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPinjamanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.btnAjukanPinjaman.setOnClickListener {
            findNavController().navigate(R.id.action_pinjamanFragment_to_loansFragment)
        }

        fetchLoans()
    }

    private fun fetchLoans() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("loans")
            .orderBy("tanggalPengajuan", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val loanList = result.toObjects(Loan::class.java)

                // Setup Adapter
                val historyAdapter = UserLoanHistoryAdapter(loanList) { clickedLoan ->
                    val bundle = Bundle().apply {
                        // Pass String ID (Changed from putLong to putString)
                        putString("loanId", clickedLoan.id)
                    }
                    findNavController().navigate(R.id.action_pinjamanFragment_to_loanDetailFragment, bundle)
                }

                binding.recyclerLoanHistory.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerLoanHistory.adapter = historyAdapter
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}