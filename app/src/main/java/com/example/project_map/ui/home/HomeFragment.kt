package com.example.project_map.ui.home

import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.jvm.java
import java.util.Date
import java.util.Calendar

class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Listener references to avoid memory leaks
    private var userListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        s: Bundle?
    ): View {
        _b = FragmentHomeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Navigation
        b.btnPengajuanPenarikan.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_pinjamanFragment)
        }

        // Setup RecyclerView
        b.recyclerRecent.layoutManager = LinearLayoutManager(requireContext())

        // Load Data
        val userId = auth.currentUser?.uid
        if (userId != null) {
            setupRealtimeUserListener(userId)
            fetchLoanStats(userId)
            fetchRecentActivity(userId)
        } else {
            // Fallback for guest/not logged in
            b.tvWelcomeName.text = "Tamu"
            b.tvAngNumber.text = "-"
        }
    }

    private fun setupRealtimeUserListener(userId: String) {
        userListener = db.collection("users").document(userId)
            .addSnapshotListener { document, e ->
                if (e != null) return@addSnapshotListener
                if (_b == null) return@addSnapshotListener // Crash prevention

                if (document != null && document.exists()) {
                    // 1. Profile Data
                    val name = document.getString("name") ?: "Anggota"
                    val memberCode = document.getString("memberCode") ?: "ANG-..."

                    b.tvWelcomeName.text = name
                    b.tvAngNumber.text = memberCode

                    // 2. Financial Totals
                    val totalSimpanan = document.getDouble("totalSimpanan") ?: 0.0
                    val simpananWajib = document.getDouble("simpananWajib") ?: 0.0

                    b.tvSaldo.text = formatCurrency(totalSimpanan)
                    b.tvSimpananWajib.text = "Simpanan Wajib ${formatCurrency(simpananWajib)}"
                }
            }
    }

    private fun fetchLoanStats(userId: String) {
        db.collection("users").document(userId).collection("loans")
            .get()
            .addOnSuccessListener { result ->
                if (_b == null) return@addOnSuccessListener

                var activeCount = 0
                var paidCount = 0
                var usedAmount = 0.0

                for (doc in result) {
                    val loan = doc.toObject(Loan::class.java)

                    // Logic to determine active vs paid
                    if (loan.status == "Lunas") {
                        paidCount++
                    } else if (loan.status == "Disetujui" || loan.status == "Pinjaman Berjalan" || loan.status == "Proses") {
                        activeCount++
                        // Add to "Used Amount" only if it's currently active/running
                        usedAmount += loan.nominal
                    }
                }

                // Update UI
                b.tvPinjamanAktifValue.text = activeCount.toString()
                b.tvPinjamanLunasValue.text = paidCount.toString()
                b.tvPinjamanTerpakaiValue.text = formatCurrency(usedAmount)

                // Static Limit (Or fetch from user profile if you have a 'limit' field)
                b.tvLimitPinjamanValue.text = formatCurrency(100_000_000.0)
            }
            .addOnFailureListener {
                // Handle error silently or show toast
            }
    }

    private fun fetchRecentActivity(userId: String) {
        // Fetching from 'savings' collection as the primary activity log
        // (You could also fetch 'installments' and merge them if you want a mixed list)

        db.collection("users").document(userId).collection("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { result ->
                if (_b == null) return@addOnSuccessListener

                val activityList = mutableListOf<RecentActivity>()
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))

                for (doc in result) {
                    val trans = doc.toObject(Transaction::class.java)

                    val dateStr = if (trans.date != null) sdf.format(trans.date!!) else "-"
                    val amountStr = if (trans.type.contains("Penarikan"))
                        "-${formatCurrency(trans.amount)}"
                    else
                        "+${formatCurrency(trans.amount)}"

                    activityList.add(
                        RecentActivity(
                            title = trans.type, // e.g. "Simpanan Sukarela"
                            date = dateStr,
                            amount = amountStr
                        )
                    )
                }

                // If empty, you might want to show a dummy item or "Belum ada aktivitas"
                if (activityList.isEmpty()) {
                    // Optional: Handle empty state
                }

                b.recyclerRecent.adapter = RecentActivityAdapter(activityList)
            }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Stop listening to realtime updates to save battery/data
        userListener?.remove()
        _b = null
    }
}