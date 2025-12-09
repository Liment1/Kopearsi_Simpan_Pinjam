package com.example.project_map.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.project_map.R
import com.example.project_map.data.KoperasiDatabase
import com.example.project_map.data.TipeCatatan
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButtonToggleGroup
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdminLaporanKeuanganFragment : Fragment() {

    // Deklarasi View
    private lateinit var tvCurrentPeriod: TextView
    private lateinit var tvSummaryTitle: TextView
    private lateinit var tvTotalPemasukan: TextView
    private lateinit var tvTotalPengeluaran: TextView
    private lateinit var tvLabaRugi: TextView
    private lateinit var tvRincianSimpanan: TextView
    private lateinit var tvRincianAngsuranMasuk: TextView
    private lateinit var tvRincianPinjamanKeluar: TextView
    private lateinit var tvRincianOperasional: TextView
    private lateinit var barChart: BarChart

    private val calendar = Calendar.getInstance()
    private var isMonthlyView = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_laporan_keuangan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
//        setupToolbar(view)
        setupListeners()
        // Inisialisasi tampilan awal
        updateUI()
    }

    // Fungsi untuk inisialisasi semua View
    private fun initViews(view: View) {
        tvCurrentPeriod = view.findViewById(R.id.tvCurrentPeriod)
        tvSummaryTitle = view.findViewById(R.id.tvSummaryTitle)
        tvTotalPemasukan = view.findViewById(R.id.tvTotalPemasukan)
        tvTotalPengeluaran = view.findViewById(R.id.tvTotalPengeluaran)
        tvLabaRugi = view.findViewById(R.id.tvLabaRugi)
        tvRincianSimpanan = view.findViewById(R.id.tvRincianSimpanan)
        tvRincianAngsuranMasuk = view.findViewById(R.id.tvRincianAngsuranMasuk)
        tvRincianPinjamanKeluar = view.findViewById(R.id.tvRincianPinjamanKeluar)
        tvRincianOperasional = view.findViewById(R.id.tvRincianOperasional)
        barChart = view.findViewById(R.id.barChart)
    }

    // Fungsi untuk setup semua listener
    private fun setupListeners() {
        view?.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupFilter)?.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                isMonthlyView = checkedId == R.id.btnFilterBulanan
                updateUI()
            }
        }
        view?.findViewById<ImageButton>(R.id.btnPreviousPeriod)?.setOnClickListener {
            if (isMonthlyView) calendar.add(Calendar.MONTH, -1) else calendar.add(Calendar.YEAR, -1)
            updateUI()
        }
        view?.findViewById<ImageButton>(R.id.btnNextPeriod)?.setOnClickListener {
            if (isMonthlyView) calendar.add(Calendar.MONTH, 1) else calendar.add(Calendar.YEAR, 1)
            updateUI()
        }
    }

    // Fungsi utama untuk update seluruh UI
    private fun updateUI() {
        val dateFormat = if (isMonthlyView) SimpleDateFormat("MMMM yyyy", Locale("in", "ID")) else SimpleDateFormat("yyyy", Locale("in", "ID"))
        val periodStr = dateFormat.format(calendar.time)
        tvCurrentPeriod.text = periodStr
        tvSummaryTitle.text = "Ringkasan $periodStr"

        // Filter data transaksi sesuai periode
        val filteredTransactions = KoperasiDatabase.allTransactions.filter {
            val transactionCal = Calendar.getInstance().apply { time = it.date }
            if (isMonthlyView) {
                transactionCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && transactionCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            } else {
                transactionCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            }
        }

        // Kalkulasi Pemasukan
        val totalSimpanan = filteredTransactions.filter { it.type == TipeCatatan.SIMPANAN }.sumOf { it.amount }
        val totalAngsuranMasuk = filteredTransactions.filter { it.type == TipeCatatan.ANGSURAN && !it.description.contains("Biaya", true) }.sumOf { it.amount }
        val totalPemasukan = totalSimpanan + totalAngsuranMasuk

        // Kalkulasi Pengeluaran
        val totalPinjamanKeluar = filteredTransactions.filter { it.type == TipeCatatan.PINJAMAN }.sumOf { it.amount }
        val totalOperasional = filteredTransactions.filter { it.description.contains("Biaya", true) }.sumOf { it.amount }
        val totalPengeluaran = totalPinjamanKeluar + totalOperasional

        val labaRugi = totalPemasukan - totalPengeluaran

        // Update Tampilan Teks
        tvTotalPemasukan.text = formatCurrency(totalPemasukan)
        tvTotalPengeluaran.text = formatCurrency(totalPengeluaran)
        tvLabaRugi.text = formatCurrency(labaRugi)
        tvLabaRugi.setTextColor(if (labaRugi >= 0) ContextCompat.getColor(requireContext(), R.color.green) else ContextCompat.getColor(requireContext(), R.color.red))

        tvRincianSimpanan.text = formatCurrency(totalSimpanan)
        tvRincianAngsuranMasuk.text = formatCurrency(totalAngsuranMasuk)
        tvRincianPinjamanKeluar.text = formatCurrency(totalPinjamanKeluar)
        tvRincianOperasional.text = formatCurrency(totalOperasional)

        // Update Tampilan Grafik
        setupBarChart(totalPemasukan, totalPengeluaran)
    }

    // Fungsi untuk setup dan menggambar grafik
    private fun setupBarChart(pemasukan: Double, pengeluaran: Double) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, pemasukan.toFloat()))
        entries.add(BarEntry(1f, pengeluaran.toFloat()))

        val dataSet = BarDataSet(entries, "Data Keuangan")
        dataSet.colors = listOf(ContextCompat.getColor(requireContext(), R.color.green), ContextCompat.getColor(requireContext(), R.color.red))
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return formatCurrency(value.toDouble()).replace("Rp", "").trim()
            }
        }

        val barData = BarData(dataSet)
        barChart.data = barData

        // Styling Grafik
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.animateY(1000)

        // Styling Sumbu X
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            private val labels = arrayOf("Pemasukan", "Pengeluaran")
            override fun getFormattedValue(value: Float): String {
                return labels[value.toInt()]
            }
        }

        // Styling Sumbu Y
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f

        barChart.invalidate() // Refresh chart
    }

    // Helper untuk format mata uang
    private fun formatCurrency(value: Double): String {
        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(value)
    }
}