package com.example.project_map.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.data.KoperasiDatabase
import com.example.project_map.data.TipeCatatan
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis

class AdminDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation(view)
        displayFinancialSummary(view)
        setupChart(view)
    }

    private fun setupNavigation(view: View) {
        // Set click listeners for each card to navigate to the corresponding fragment
        view.findViewById<CardView>(R.id.cardDataAnggota).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminDataAnggotaFragment)
        }
        view.findViewById<CardView>(R.id.cardPengajuanPinjaman).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_fragmentPinjamanAdmin)
        }
        view.findViewById<CardView>(R.id.cardTransaksiSimpanan).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_fragmentTransaksiSimpanan)
        }
        view.findViewById<CardView>(R.id.cardLaporanKeuangan).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminLaporanKeuanganFragment)
        }
        view.findViewById<CardView>(R.id.cardKirimNotifikasi).setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminNotifikasiFragment)
        }
    }

    private fun displayFinancialSummary(view: View) {
        val allTransactions = KoperasiDatabase.allTransactions

        // 1. Rekap total simpanan dan pinjaman
        val totalSimpanan = allTransactions.filter { it.type == TipeCatatan.SIMPANAN }.sumOf { it.amount }
        val totalPinjaman = allTransactions.filter { it.type == TipeCatatan.PINJAMAN }.sumOf { it.amount }

        // 2. Saldo kas dan laba bersih
        val totalPemasukan = allTransactions.filter { it.type.isPemasukan() }.sumOf { it.amount }
        val totalPengeluaran = allTransactions.filter { !it.type.isPemasukan() }.sumOf { it.amount }
        val saldoKas = totalPemasukan - totalPengeluaran
        val labaBersih = totalPemasukan - totalPengeluaran // Simplified for this prototype

        view.findViewById<TextView>(R.id.tvTotalSimpananValue).text = formatCurrency(totalSimpanan)
        view.findViewById<TextView>(R.id.tvTotalPinjamanValue).text = formatCurrency(totalPinjaman)
        view.findViewById<TextView>(R.id.tvSaldoKasValue).text = formatCurrency(saldoKas)
        view.findViewById<TextView>(R.id.tvLabaBersihValue).text = formatCurrency(labaBersih)
    }

    private fun setupChart(view: View) {
        val barChart = view.findViewById<BarChart>(R.id.barChartDashboard)
        val allTransactions = KoperasiDatabase.allTransactions

        // Group transactions by month for the chart
        val monthlySummary = allTransactions.groupBy {
            val cal = Calendar.getInstance()
            cal.time = it.date
            cal.get(Calendar.MONTH)
        }.mapValues { entry ->
            val simpanan = entry.value.filter { it.type == TipeCatatan.SIMPANAN }.sumOf { it.amount }
            val pinjaman = entry.value.filter { it.type == TipeCatatan.PINJAMAN }.sumOf { it.amount }
            Pair(simpanan, pinjaman)
        }

        val simpananEntries = ArrayList<BarEntry>()
        val pinjamanEntries = ArrayList<BarEntry>()
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")

        for (i in 0..11) {
            val summary = monthlySummary[i]
            simpananEntries.add(BarEntry(i.toFloat(), summary?.first?.toFloat() ?: 0f))
            pinjamanEntries.add(BarEntry(i.toFloat(), summary?.second?.toFloat() ?: 0f))
        }

        val simpananDataSet = BarDataSet(simpananEntries, "Simpanan").apply {
            color = ContextCompat.getColor(requireContext(), R.color.green_primary)
        }
        val pinjamanDataSet = BarDataSet(pinjamanEntries, "Pinjaman").apply {
            color = ContextCompat.getColor(requireContext(), R.color.red)
        }

        val barData = BarData(simpananDataSet, pinjamanDataSet)
        barChart.data = barData

        // Chart styling
        barChart.description.isEnabled = false
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < months.size) {
                    months[index]
                } else {
                    ""
                }
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(true)
        xAxis.setDrawGridLines(false)

        barChart.axisRight.isEnabled = false
        barChart.groupBars(0f, 0.08f, 0.02f) // (fromX, groupSpace, barSpace)
        barChart.invalidate()
        barChart.animateY(1000)
    }


    private fun formatCurrency(value: Double): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        format.maximumFractionDigits = 0
        return format.format(value)
    }
}

