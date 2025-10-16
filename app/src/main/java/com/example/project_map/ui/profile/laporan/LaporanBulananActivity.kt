package com.example.project_map.ui.profile.laporan

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.CatatanKeuangan
import com.example.project_map.data.TipeCatatan
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class LaporanBulananActivity : AppCompatActivity() {

    private lateinit var tvCurrentMonth: TextView
    private lateinit var tvTotalSimpanan: TextView
    private lateinit var tvTotalPinjaman: TextView
    private lateinit var tvTotalAngsuran: TextView
    private lateinit var tvSummaryTitle: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var cardSummary: MaterialCardView

    private lateinit var riwayatAdapter: RiwayatAdapter
    private val allTransactions = mutableListOf<CatatanKeuangan>()
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_bulanan)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
        tvTotalSimpanan = findViewById(R.id.tvTotalSimpanan)
        tvTotalPinjaman = findViewById(R.id.tvTotalPinjaman)
        tvTotalAngsuran = findViewById(R.id.tvTotalAngsuran)
        tvSummaryTitle = findViewById(R.id.tvSummaryTitle)
        rvTransactions = findViewById(R.id.rvTransactions)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        cardSummary = findViewById(R.id.cardSummary)

        val btnPreviousMonth = findViewById<ImageButton>(R.id.btnPreviousMonth)
        val btnNextMonth = findViewById<ImageButton>(R.id.btnNextMonth)

        riwayatAdapter = RiwayatAdapter(emptyList())
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = riwayatAdapter

        // ### THIS IS THE CHANGE ###
        // Get data from the new data source instead of creating it here.
        allTransactions.addAll(com.example.project_map.data.LaporanDataSource.getDummyTransactions())

        btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateUI()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateUI()
        }

        updateUI()
    }

    // REMOVED: createDummyTransactions() and getDate() are now in LaporanDataSource.kt

    private fun updateUI() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("in", "ID"))
        val currentMonthStr = monthFormat.format(calendar.time)
        tvCurrentMonth.text = currentMonthStr
        tvSummaryTitle.text = "Ringkasan Bulan $currentMonthStr"

        val filteredTransactions = allTransactions.filter {
            val transactionCalendar = Calendar.getInstance()
            transactionCalendar.time = it.date
            transactionCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    transactionCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
        }

        if (filteredTransactions.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvTransactions.visibility = View.GONE
            cardSummary.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvTransactions.visibility = View.VISIBLE
            cardSummary.visibility = View.VISIBLE

            val totalSimpanan = filteredTransactions.filter { it.type == TipeCatatan.SIMPANAN }.sumOf { it.amount }
            val totalPinjaman = filteredTransactions.filter { it.type == TipeCatatan.PINJAMAN }.sumOf { it.amount }
            val totalAngsuran = filteredTransactions.filter { it.type == TipeCatatan.ANGSURAN }.sumOf { it.amount }

            val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0

            tvTotalSimpanan.text = format.format(totalSimpanan)
            tvTotalPinjaman.text = format.format(totalPinjaman)
            tvTotalAngsuran.text = format.format(totalAngsuran)

            riwayatAdapter.updateData(filteredTransactions.sortedBy { it.date })
        }
    }
}