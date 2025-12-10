package com.example.project_map.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.data.Transaction
import com.example.project_map.databinding.FragmentHomeBinding
import com.example.project_map.data.Loan
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Listener reference to avoid memory leaks
    private var userListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        binding.rvHistori.layoutManager = LinearLayoutManager(requireContext())

        if (currentUser != null) {
            setupRealtimeUserListener(currentUser.uid)
            fetchLoanSummary(currentUser.uid)
            fetchCombinedRecentActivity(currentUser.uid)
            setupNavigationActions()
        } else {
            binding.tvUserName.text = "Tamu"
            binding.tvSaldoValue.text = "Rp 0"
        }
    }

    private fun fetchCombinedRecentActivity(userId: String) {
        val mixedList = mutableListOf<DashboardItem>()
        val sdf = SimpleDateFormat("dd MMM", Locale("in", "ID"))

        // 1. Query Savings
        val savingsTask = db.collection("users").document(userId).collection("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .get()

        // 2. Query Loans
        val loansTask = db.collection("users").document(userId).collection("loans")
            .orderBy("tanggalPengajuan", Query.Direction.DESCENDING)
            .limit(5)
            .get()

        // Execute queries
        Tasks.whenAllSuccess<Any>(savingsTask, loansTask)
            .addOnSuccessListener { results ->
                if (_binding == null) return@addOnSuccessListener

                // Process Savings
                val savingsSnapshot = results[0] as com.google.firebase.firestore.QuerySnapshot
                for (doc in savingsSnapshot) {
                    val item = doc.toObject(Transaction::class.java)
                    val isExp = item.type.contains("Penarikan")
                    val prefix = if (isExp) "-" else "+"

                    mixedList.add(DashboardItem(
                        title = item.type,
                        date = item.date,
                        amountString = "$prefix ${formatCurrency(item.amount)}",
                        isExpense = isExp
                    ))
                }

                // Process Loans
                val loansSnapshot = results[1] as com.google.firebase.firestore.QuerySnapshot
                for (doc in loansSnapshot) {
                    val item = doc.toObject(Loan::class.java)
                    mixedList.add(DashboardItem(
                        title = "Pinjaman: ${item.tujuan}",
                        date = item.tanggalPengajuan,
                        amountString = "+ ${formatCurrency(item.nominal)}",
                        isExpense = false
                    ))
                }

                // Sort & Map
                val sortedList = mixedList.sortedByDescending { it.date }.take(5)
                val adapterList = sortedList.map {
                    RecentActivity(
                        title = it.title,
                        date = if (it.date != null) sdf.format(it.date) else "-",
                        amount = it.amountString
                    )
                }

                binding.rvHistori.adapter = RecentActivityAdapter(adapterList)
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error fetching combined activity", e)
            }
    }

    private fun setupRealtimeUserListener(userId: String) {
        userListener = db.collection("users").document(userId)
            .addSnapshotListener { document, e ->
                if (e != null) return@addSnapshotListener
                if (_binding == null) return@addSnapshotListener

                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "Anggota"
                    binding.tvUserName.text = name

                    val totalSimpanan = document.getDouble("totalSimpanan") ?: 0.0
                    binding.tvSaldoValue.text = formatCurrency(totalSimpanan)
                }
            }
    }

    private fun fetchLoanSummary(userId: String) {
        db.collection("users").document(userId).collection("loans")
            .whereIn("status", listOf("Disetujui", "Pinjaman Berjalan", "Proses"))
            .get()
            .addOnSuccessListener { result ->
                if (_binding == null) return@addOnSuccessListener
                var totalSisaPinjaman = 0.0
                for (doc in result) {
                    val loan = doc.toObject(Loan::class.java)
                    totalSisaPinjaman += loan.sisaAngsuran
                }
                binding.tvPinjamanValue.text = formatCurrency(totalSisaPinjaman)
            }
            .addOnFailureListener {
                binding.tvPinjamanValue.text = "Rp 0"
            }
    }

    private fun setupNavigationActions() {
        binding.menuSimpanan.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_savingsFragment)
        }
        binding.menuPinjaman.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_pinjamanFragment)
        }
        binding.menuAngsuran.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_angsuranFragment)
        }
        binding.menuProfil.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
        binding.btnNotification.setOnClickListener {
            Toast.makeText(context, "Tidak ada notifikasi baru", Toast.LENGTH_SHORT).show()
        }
//        binding.fabDeposit.setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_savingsFragment)
//        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userListener?.remove()
        _binding = null
    }

    // --- INTERNAL DATA CLASS ---
    data class DashboardItem(
        val title: String,
        val date: Date?,
        val amountString: String,
        val isExpense: Boolean
    )
}
