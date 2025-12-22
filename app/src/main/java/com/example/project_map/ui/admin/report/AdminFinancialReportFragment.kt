package com.example.project_map.ui.admin.report

import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.project_map.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButtonToggleGroup
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

class AdminFinancialReportFragment : Fragment() {

    private val viewModel: AdminFinancialReportViewModel by viewModels()

    // UI Components matching XML
    private lateinit var tvCurrentPeriod: TextView
    private lateinit var tvTotalPemasukan: TextView
    private lateinit var tvTotalPengeluaran: TextView
    private lateinit var tvLabaRugi: TextView
    private lateinit var barChart: BarChart

    // Rincian Pemasukan
    private lateinit var tvRincianSimpanan: TextView
    private lateinit var tvRincianAngsuran: TextView
    private lateinit var tvRincianDenda: TextView

    // Rincian Pengeluaran
    private lateinit var tvRincianPinjamanKeluar: TextView
    private lateinit var tvRincianOperasional: TextView

    // Navigation & Filter
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_financial_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupListeners()
        setupObservers()

        view.findViewById<View>(R.id.fabExportPdf).setOnClickListener {
            exportToPdf()
        }
    }

    private fun initializeViews(view: View) {
        // Navigation
        tvCurrentPeriod = view.findViewById(R.id.tvCurrentPeriod)
        btnPrev = view.findViewById(R.id.btnPrev)
        btnNext = view.findViewById(R.id.btnNext)
        toggleGroup = view.findViewById(R.id.toggleGroupFilter)

        // Totals
        tvTotalPemasukan = view.findViewById(R.id.tvTotalPemasukan)
        tvTotalPengeluaran = view.findViewById(R.id.tvTotalPengeluaran)
        tvLabaRugi = view.findViewById(R.id.tvLabaRugi)

        // Chart
        barChart = view.findViewById(R.id.chartPemasukanPengeluaran)

        // Rincian Pemasukan
        tvRincianSimpanan = view.findViewById(R.id.tvRincianSimpanan)
        tvRincianAngsuran = view.findViewById(R.id.tvRincianAngsuran)
        tvRincianDenda = view.findViewById(R.id.tvRincianDenda)

        // Rincian Pengeluaran
        tvRincianPinjamanKeluar = view.findViewById(R.id.tvRincianPinjamanKeluar)
        tvRincianOperasional = view.findViewById(R.id.tvRincianOperasional)
    }

    private fun setupListeners() {
        btnPrev.setOnClickListener { viewModel.prevPeriod() }
        btnNext.setOnClickListener { viewModel.nextPeriod() }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnFilterBulanan -> viewModel.setReportType(true)
                    R.id.btnFilterTahunan -> viewModel.setReportType(false)
                }
            }
        }
    }

    private fun setupObservers() {
        // 1. Label Period
        viewModel.currentPeriodLabel.observe(viewLifecycleOwner) { label ->
            tvCurrentPeriod.text = label
        }

        // 2. Data & Chart
        viewModel.reportState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AdminFinancialReportViewModel.ReportState.Loading -> {
                    // Could show progress bar here
                }
                is AdminFinancialReportViewModel.ReportState.Success -> {
                    // Update Totals
                    tvTotalPemasukan.text = formatCurrency(state.income)
                    tvTotalPengeluaran.text = formatCurrency(state.expense)
                    tvLabaRugi.text = formatCurrency(state.netProfit)

                    // Update Breakdown (Rincian)
                    tvRincianSimpanan.text = formatCurrency(state.incSimpanan)
                    tvRincianAngsuran.text = formatCurrency(state.incAngsuran)
                    tvRincianDenda.text = formatCurrency(state.incDenda)

                    tvRincianPinjamanKeluar.text = formatCurrency(state.expPinjaman)
                    tvRincianOperasional.text = formatCurrency(state.expOperasional)

                    // Color logic
                    if (state.netProfit >= 0) {
                        tvLabaRugi.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    } else {
                        tvLabaRugi.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    }

                    setupChart(state.income, state.expense)
                }
            }
        }
    }

    private fun setupChart(income: Double, expense: Double) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, income.toFloat()))
        entries.add(BarEntry(1f, expense.toFloat()))

        val dataSet = BarDataSet(entries, "Laporan Keuangan")
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.green),
            ContextCompat.getColor(requireContext(), R.color.red)
        )

        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value > 0) formatCurrency(value.toDouble()).replace("Rp", "").trim() else ""
            }
        }

        val barData = BarData(dataSet)
        barChart.data = barData

        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setTouchEnabled(false)
        barChart.animateY(1000)

        // Axis
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                private val labels = arrayOf("Masuk", "Keluar")
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt()
                    return if (idx in labels.indices) labels[idx] else ""
                }
            }
        }

        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f

        barChart.invalidate()
    }

    private fun formatCurrency(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(value)
    }

    private fun exportToPdf() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(1080, 1920, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Draw the view onto the PDF canvas
        // We capture the whole ScrollView or the main layout
        val content = view?.findViewById<View>(R.id.contentLayout) // Ensure your XML root or content has this ID
        content?.draw(canvas)

        pdfDocument.finishPage(page)

        // Save to Downloads folder
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Laporan_Keuangan.pdf"
        )

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF Disimpan di Downloads", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}