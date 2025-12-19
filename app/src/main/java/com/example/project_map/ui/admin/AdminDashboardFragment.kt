package com.example.project_map.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class AdminDashboardFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tvTotalSimpanan: TextView
    private lateinit var tvTotalPinjaman: TextView
    private lateinit var tvSaldoKas: TextView
    private lateinit var tvLabaBersih: TextView
    private lateinit var chartRevenue: BarChart

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        // Init Views
        tvTotalSimpanan = view.findViewById(R.id.tvTotalSimpananValue)
        tvTotalPinjaman = view.findViewById(R.id.tvTotalPinjamanValue)
        tvSaldoKas = view.findViewById(R.id.tvSaldoKasValue)
        tvLabaBersih = view.findViewById(R.id.tvLabaBersihValue)
        chartRevenue = view.findViewById(R.id.chartRevenue)

        loadUserFinancials()
        loadFinancialReports()

        setupNavigation(view)
//        loadDashboardData(view)
    }

    private fun setupNavigation(view: View) {
        // 1. Data Anggota
        view.findViewById<CardView>(R.id.cardDataAnggota).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminDataAnggotaFragment)
        }

        // 2. Pengajuan Pinjaman
        view.findViewById<CardView>(R.id.cardPengajuanPinjaman).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_fragmentPinjamanAdmin)
        }

        // 3. Angsuran Berjalan
        view.findViewById<CardView>(R.id.cardAngsuranBerjalan).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminAngsuranFragment)
        }

        // 4. History Simpanan (Transaksi Simpanan)
        view.findViewById<CardView>(R.id.cardTransaksiSimpanan).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_fragmentTransaksiSimpanan)
        }

        // 5. Laporan Keuangan
        view.findViewById<CardView>(R.id.cardLaporanKeuangan).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminLaporanKeuanganFragment)
        }

        // 6. Kirim Notifikasi
        view.findViewById<CardView>(R.id.cardKirimNotifikasi).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminNotifikasiFragment)
        }

        // 7. Pengaturan Admin
        view.findViewById<CardView>(R.id.cardPengaturan).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminPengaturanFragment)
        }
    }

    private fun loadUserFinancials() {
        // A. Total Simpanan (Sum of all users)
        db.collection("users").get().addOnSuccessListener { result ->
            val totalSimpanan = result.documents.sumOf { it.getDouble("totalSimpanan") ?: 0.0 }
            tvTotalSimpanan.text = formatCurrency(totalSimpanan)
        }

        // B. Total Active Loans (Sum of all active loans)
// ... di dalam fun loadUserFinancials()

// B. Total Active Loans (Sum of all active loans)
        db.collectionGroup("loans").whereNotEqualTo("status", "Lunas").get()
            .addOnSuccessListener { result ->
                val totalPinjaman = result.documents.sumOf { it.getDouble("sisaAngsuran") ?: 0.0 }
                tvTotalPinjaman.text = formatCurrency(totalPinjaman)
            }
            // PERUBAHAN DI SINI
            .addOnFailureListener { exception ->
                // Mencetak error ke Logcat
                Log.e("AdminDash", "Error loading active loans: ", exception)

                // Menampilkan pesan error yang lebih jelas di UI
                tvTotalPinjaman.text = "Error: Check Firestore Index"

                // Cek Logcat! Biasanya ada link untuk membuat index.
            }
    }

    // 2. Fetch Financial Reports (Revenue & Profit) & Setup Chart
    private fun loadFinancialReports() {
        db.collection("financial_reports").get()
            .addOnSuccessListener { result ->
                var totalRevenue = 0.0
                var totalProfit = 0.0

                val entriesRevenue = ArrayList<BarEntry>()
                val labels = ArrayList<String>()
                var index = 0f

                for (doc in result) {
                    val revenue = doc.getDouble("totalRevenue") ?: 0.0
                    val profit = doc.getDouble("netProfit") ?: 0.0
                    val monthName = doc.getString("month") ?: "?"

                    // Sum for dashboard cards
                    totalRevenue += revenue
                    totalProfit += profit

                    // Prepare Chart Entry
                    entriesRevenue.add(BarEntry(index, revenue.toFloat()))
                    labels.add(monthName.take(3)) // "Nov", "Dec"
                    index++
                }

                // Update Cards
                tvSaldoKas.text = formatCurrency(totalRevenue)
                tvLabaBersih.text = formatCurrency(totalProfit)

                // Setup Chart
                setupChart(entriesRevenue, labels)
            }
    }

    private fun setupChart(entries: ArrayList<BarEntry>, labels: ArrayList<String>) {
        if (entries.isEmpty()) return

        val dataSet = BarDataSet(entries, "Pemasukan (Revenue)")
        dataSet.color = Color.parseColor("#4CAF50") // Green
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        chartRevenue.data = barData

        // Styling
        chartRevenue.description.isEnabled = false
        chartRevenue.legend.isEnabled = false
        chartRevenue.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chartRevenue.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartRevenue.xAxis.setDrawGridLines(false)
        chartRevenue.axisRight.isEnabled = false
        chartRevenue.animateY(1000)

        chartRevenue.invalidate() // Refresh
    }

    private fun formatCurrency(value: Double): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        format.maximumFractionDigits = 0
        return format.format(value)
    }
}