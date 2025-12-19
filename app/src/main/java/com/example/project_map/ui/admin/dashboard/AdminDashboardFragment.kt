package com.example.project_map.ui.admin.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.data.model.MonthlyFinancialReport
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.NumberFormat
import java.util.Locale

class AdminDashboardFragment : Fragment() {

    private val viewModel: AdminDashboardViewModel by viewModels()

    // Views
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

        initializeViews(view)
        setupNavigation(view)
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        tvTotalSimpanan = view.findViewById(R.id.tvTotalSimpananValue)
        tvTotalPinjaman = view.findViewById(R.id.tvTotalPinjamanValue)
        tvSaldoKas = view.findViewById(R.id.tvSaldoKasValue)
        tvLabaBersih = view.findViewById(R.id.tvLabaBersihValue)
        chartRevenue = view.findViewById(R.id.chartRevenue)
    }

    private fun observeViewModel() {
        // 1. Total Savings
        viewModel.totalSavings.observe(viewLifecycleOwner) { amount ->
            tvTotalSimpanan.text = formatCurrency(amount)
        }

        // 2. Total Active Loans
        viewModel.totalActiveLoans.observe(viewLifecycleOwner) { amount ->
            tvTotalPinjaman.text = formatCurrency(amount)
        }

        // 3. Financial Summary (Kas & Profit)
        viewModel.totalRevenue.observe(viewLifecycleOwner) { amount ->
            tvSaldoKas.text = formatCurrency(amount)
        }
        viewModel.totalProfit.observe(viewLifecycleOwner) { amount ->
            tvLabaBersih.text = formatCurrency(amount)
        }

        // 4. Chart Data
        viewModel.chartData.observe(viewLifecycleOwner) { reports ->
            setupChart(reports)
        }

        // 5. Error Handling
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                // If specific index error for queries
                if (msg.contains("failed to execute")) {
                    tvTotalPinjaman.text = "Error Index"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupChart(reports: List<MonthlyFinancialReport>) {
        if (reports.isEmpty()) return

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        reports.forEachIndexed { index, report ->
            entries.add(BarEntry(index.toFloat(), report.totalRevenue.toFloat()))
            labels.add(report.month.take(3)) // e.g., "November" -> "Nov"
        }

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

        chartRevenue.invalidate()
    }

    private fun setupNavigation(view: View) {
        val navMap = mapOf(
            R.id.cardDataAnggota to R.id.action_adminDashboardFragment_to_adminDataAnggotaFragment,
            R.id.cardPengajuanPinjaman to R.id.action_adminDashboardFragment_to_fragmentPinjamanAdmin,
            R.id.cardAngsuranBerjalan to R.id.action_adminDashboardFragment_to_adminAngsuranFragment,
            R.id.cardTransaksiSimpanan to R.id.action_adminDashboardFragment_to_fragmentTransaksiSimpanan,
            R.id.cardLaporanKeuangan to R.id.action_adminDashboardFragment_to_adminLaporanKeuanganFragment,
            R.id.cardKirimNotifikasi to R.id.action_adminDashboardFragment_to_adminNotifikasiFragment,
            R.id.cardPengaturan to R.id.action_adminDashboardFragment_to_adminPengaturanFragment
        )

        navMap.forEach { (viewId, actionId) ->
            view.findViewById<CardView>(viewId).setOnClickListener {
                findNavController().navigate(actionId)
            }
        }
    }

    private fun formatCurrency(value: Double): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        format.maximumFractionDigits = 0
        return format.format(value)
    }
}