package com.example.project_map.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.Loan
import com.example.project_map.ui.admin.loans.AdminLoanAdapter
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FragmentPinjamanAdmin : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AdminLoanAdapter

    // UI Elements
    private lateinit var tvDaftar: TextView
    private lateinit var tvHistory: TextView
    private lateinit var indicatorDaftar: View
    private lateinit var indicatorHistory: View

    // Data
    private val pendingList = mutableListOf<Loan>()
    private val historyList = mutableListOf<Loan>()
    private var isShowingHistory = false

    // Map to store DocumentReferences (Key: LoanID, Value: Reference)
    // This allows us to update the specific document in users/{uid}/loans/
    private val loanRefs = mutableMapOf<String, DocumentReference>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ensure fragment_pinjaman_admin.xml uses a RecyclerView now
        return inflater.inflate(R.layout.fragment_pinjaman_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        // Init Views (Update IDs in your XML to match these)
        recycler = view.findViewById(R.id.recyclerViewLoans)
        tvDaftar = view.findViewById(R.id.tvDaftar)
        tvHistory = view.findViewById(R.id.tvHistory)
        indicatorDaftar = view.findViewById(R.id.indicatorDaftar)
        indicatorHistory = view.findViewById(R.id.indicatorHistory)

        setupRecyclerView()
        setupTabs()
        fetchLoans()
    }

    private fun setupRecyclerView() {
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = AdminLoanAdapter(emptyList()) { loan, action ->
            if (action == "terima") {
                showApprovalDialog(loan)
            } else if (action == "tolak") {
                showRejectionDialog(loan)
            }
        }
        recycler.adapter = adapter
    }

    private fun setupTabs() {
        tvDaftar.setOnClickListener {
            isShowingHistory = false
            updateTabUI()
            adapter.updateList(pendingList)
        }
        tvHistory.setOnClickListener {
            isShowingHistory = true
            updateTabUI()
            adapter.updateList(historyList)
        }
        updateTabUI() // Default state
    }

    private fun updateTabUI() {
        if (isShowingHistory) {
            tvDaftar.setTextColor(Color.GRAY)
            indicatorDaftar.visibility = View.INVISIBLE
            tvHistory.setTextColor(Color.parseColor("#4CAF50")) // Green
            indicatorHistory.visibility = View.VISIBLE
        } else {
            tvDaftar.setTextColor(Color.parseColor("#4CAF50"))
            indicatorDaftar.visibility = View.VISIBLE
            tvHistory.setTextColor(Color.GRAY)
            indicatorHistory.visibility = View.INVISIBLE
        }
    }

    private fun fetchLoans() {
        db.collectionGroup("loans")
            .orderBy("tanggalPengajuan", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                pendingList.clear()
                historyList.clear()
                loanRefs.clear()

                for (doc in result) {
                    val loan = doc.toObject(Loan::class.java)
                    // Store the actual document reference to update later
                    loanRefs[loan.id] = doc.reference

                    if (loan.status == "Proses") {
                        pendingList.add(loan)
                    } else {
                        historyList.add(loan)
                    }
                }

                // Refresh current view
                val listToShow = if (isShowingHistory) historyList else pendingList
                adapter.updateList(listToShow)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal memuat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showApprovalDialog(loan: Loan) {
        AlertDialog.Builder(requireContext())
            .setTitle("Setujui Pinjaman")
            .setMessage("Apakah Anda yakin menyetujui pinjaman Rp ${loan.nominal}?")
            .setPositiveButton("Ya, Setujui") { _, _ ->
                updateLoanStatus(loan.id, "Pinjaman Berjalan", "")
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showRejectionDialog(loan: Loan) {
        val input = EditText(requireContext())
        input.hint = "Alasan penolakan..."

        AlertDialog.Builder(requireContext())
            .setTitle("Tolak Pinjaman")
            .setView(input)
            .setPositiveButton("Tolak") { _, _ ->
                val reason = input.text.toString()
                updateLoanStatus(loan.id, "Ditolak", reason)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateLoanStatus(loanId: String, newStatus: String, reason: String) {
        val ref = loanRefs[loanId] ?: return

        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "alasanPenolakan" to reason
        )

        ref.update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Status berhasil diperbarui", Toast.LENGTH_SHORT).show()
                fetchLoans() // Refresh data
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal update status", Toast.LENGTH_SHORT).show()
            }
    }
}