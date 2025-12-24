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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminFinancialReportFragment : Fragment() {

    private val viewModel: AdminFinancialReportViewModel by viewModels()

    private lateinit var tvCurrentPeriod: TextView
    private lateinit var tvTotalPemasukan: TextView
    private lateinit var tvTotalPengeluaran: TextView
    private lateinit var tvLabaRugi: TextView
    private lateinit var barChart: BarChart
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
        // Ukuran A4 (72 DPI) = 595 x 842 unit
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()
        val titlePaint = android.graphics.Paint()

        // 1. Header Laporan
        titlePaint.apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }
        canvas.drawText("LAPORAN KEUANGAN KOPERASI", 40f, 50f, titlePaint)

        paint.textSize = 12f
        canvas.drawText("Periode: ${tvCurrentPeriod.text}", 40f, 75f, paint)
        canvas.drawLine(40f, 90f, 555f, 90f, paint)

        // 2. Data Ringkasan (Mengambil data dari ViewModel/UI)
        paint.textSize = 14f
        canvas.drawText("Ringkasan Umum", 40f, 120f, titlePaint)

        paint.textSize = 12f
        canvas.drawText("Total Pemasukan: ${tvTotalPemasukan.text}", 40f, 150f, paint)
        canvas.drawText("Total Pengeluaran: ${tvTotalPengeluaran.text}", 40f, 175f, paint)

        // Warna untuk Laba/Rugi
        val netProfitText = tvLabaRugi.text.toString()
        paint.color = if (!netProfitText.contains("-")) Color.GREEN else Color.RED
        canvas.drawText("Laba/Rugi Bersih: $netProfitText", 40f, 200f, paint)
        paint.color = Color.BLACK // Reset warna

        // 3. Rincian Transaksi
        canvas.drawText("Rincian Pemasukan", 40f, 240f, titlePaint)
        canvas.drawText("- Simpanan: ${tvRincianSimpanan.text}", 50f, 265f, paint)
        canvas.drawText("- Angsuran: ${tvRincianAngsuran.text}", 50f, 285f, paint)

        canvas.drawText("Rincian Pengeluaran", 40f, 320f, titlePaint)
        canvas.drawText("- Pinjaman Keluar: ${tvRincianPinjamanKeluar.text}", 50f, 345f, paint)
        canvas.drawText("- Operasional: ${tvRincianOperasional.text}", 50f, 365f, paint)

        // 4. Footer
        paint.textSize = 10f
        paint.color = Color.GRAY
        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Dicetak pada: $timestamp", 40f, 800f, paint)

        pdfDocument.finishPage(page)
        val fileName = "Laporan_Keuangan_${System.currentTimeMillis()}.pdf"
        val resolver = requireContext().contentResolver

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            try {
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    Toast.makeText(context, "PDF tersimpan di folder Downloads", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal simpan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // LOGIKA UNTUK ANDROID 9 KE BAWAH (API < 29)
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            try {
                pdfDocument.writeTo(FileOutputStream(file))
                Toast.makeText(context, "PDF tersimpan di folder Downloads", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal simpan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}