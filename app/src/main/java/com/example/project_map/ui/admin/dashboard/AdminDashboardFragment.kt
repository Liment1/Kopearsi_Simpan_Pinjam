package com.example.project_map.ui.admin.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.data.model.MonthlyFinancialReport
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
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
    private lateinit var chartRevenue: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        // 1. Observe Totals
        viewModel.totalSavings.observe(viewLifecycleOwner) { amount ->
            tvTotalSimpanan.text = formatCurrency(amount)
        }
        viewModel.totalActiveLoans.observe(viewLifecycleOwner) { amount ->
            tvTotalPinjaman.text = formatCurrency(amount)
        }
        viewModel.totalRevenue.observe(viewLifecycleOwner) { amount ->
            tvSaldoKas.text = formatCurrency(amount)
        }
        viewModel.totalProfit.observe(viewLifecycleOwner) { amount ->
            tvLabaBersih.text = formatCurrency(amount)
        }

        // 2. Observe Chart Data
        viewModel.chartData.observe(viewLifecycleOwner) { reports ->
            setupLineChart(reports)
        }
    }

    private fun setupLineChart(reports: List<MonthlyFinancialReport>) {
        if (reports.isEmpty()) return

        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        // Sort reports by month/year logic if needed
        reports.forEachIndexed { index, report ->
            // Use netProfit for the line chart value
            entries.add(Entry(index.toFloat(), report.netProfit.toFloat()))
            labels.add(report.month.take(3)) // "Jan", "Feb"
        }

        val dataSet = LineDataSet(entries, "Laba Bersih (Net Profit)")

        // --- Line Chart Styling (Green Theme) ---
        dataSet.color = Color.parseColor("#4CAF50")
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setCircleColor(Color.parseColor("#4CAF50"))
        dataSet.valueTextSize = 10f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curve
        dataSet.setDrawFilled(true) // Fill area under line
        dataSet.fillColor = Color.parseColor("#4CAF50")
        dataSet.fillAlpha = 50

        val lineData = LineData(dataSet)
        chartRevenue.data = lineData

        // Axis Styling
        chartRevenue.description.isEnabled = false
        chartRevenue.legend.isEnabled = true
        chartRevenue.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chartRevenue.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartRevenue.xAxis.setDrawGridLines(false)
        chartRevenue.axisRight.isEnabled = false
        chartRevenue.animateX(1000)

        chartRevenue.invalidate()
    }

    private fun setupNavigation(view: View) {
        val navMap = mapOf(
            R.id.cardDataAnggota to R.id.nav_admin_data_anggota,
            R.id.cardPengajuanPinjaman to R.id.nav_admin_pengajuan_pinjaman,
            R.id.cardAngsuranBerjalan to R.id.nav_admin_angsuran_berjalan,
            R.id.cardPermintaanPenarikan to R.id.nav_admin_permintaan_penarikan,
            R.id.cardTransaksiSimpanan to R.id.nav_admin_riwayat_simpanan,
            R.id.cardLaporanKeuangan to R.id.nav_admin_laporan_keuangan,
            R.id.cardKirimNotifikasi to R.id.nav_admin_notifikasi,
            R.id.cardPengaturan to R.id.nav_admin_pengaturan
        )

        navMap.forEach { (viewId, destinationId) ->
            view.findViewById<CardView>(viewId).setOnClickListener {
                findNavController().navigate(destinationId)
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