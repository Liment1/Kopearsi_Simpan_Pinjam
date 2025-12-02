package com.example.project_map.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*
import android.widget.TextView

class AdminDashboardFragment : Fragment() {

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        setupNavigation(view)
        loadDashboardData(view)
    }

    private fun setupNavigation(view: View) {
        // ... (Keep existing navigation code) ...
        view.findViewById<CardView>(R.id.cardDataAnggota).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminDataAnggotaFragment)
        }
        view.findViewById<CardView>(R.id.cardPengajuanPinjaman).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_fragmentPinjamanAdmin)
        }
        // ... etc ...
    }

    private fun loadDashboardData(view: View) {
        val tvTotalSimpanan = view.findViewById<TextView>(R.id.tvTotalSimpananValue)
        val tvTotalPinjaman = view.findViewById<TextView>(R.id.tvTotalPinjamanValue)

        // 1. Calculate Total Savings (Sum all users' totalSimpanan)
        db.collection("users").get().addOnSuccessListener { result ->
            val totalSimpanan = result.documents.sumOf { it.getDouble("totalSimpanan") ?: 0.0 }
            tvTotalSimpanan.text = formatCurrency(totalSimpanan)
        }

        // 2. Calculate Total Active Loans (Sum all loans where status != Lunas)
        db.collectionGroup("loans").whereNotEqualTo("status", "Lunas").get()
            .addOnSuccessListener { result ->
                val totalPinjaman = result.documents.sumOf { it.getDouble("sisaAngsuran") ?: 0.0 }
                tvTotalPinjaman.text = formatCurrency(totalPinjaman)
            }
            .addOnFailureListener {
                tvTotalPinjaman.text = "Error"
            }

        // Note: Chart implementation requires grouping data by month locally
        // after fetching collectionGroup("savings") and collectionGroup("loans").
    }

    private fun formatCurrency(value: Double): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        format.maximumFractionDigits = 0
        return format.format(value)
    }
}