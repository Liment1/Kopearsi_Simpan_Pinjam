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
import com.example.project_map.data.CatatanKeuangan // <-- Diubah
import com.example.project_map.data.TipeCatatan // <-- Diubah
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class LaporanBulananActivity : AppCompatActivity() {

    // ... (deklarasi view tetap sama) ...
    private lateinit var tvCurrentMonth: TextView
    private lateinit var tvTotalSimpanan: TextView
    private lateinit var tvTotalPinjaman: TextView
    private lateinit var tvTotalAngsuran: TextView
    private lateinit var tvSummaryTitle: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var cardSummary: MaterialCardView


    private lateinit var riwayatAdapter: RiwayatAdapter // <-- Diubah
    private val allTransactions = mutableListOf<CatatanKeuangan>() // <-- Diubah
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_bulanan)

        // ... (inisialisasi view dan toolbar tetap sama) ...
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

        // Setup RecyclerView dengan adapter baru
        riwayatAdapter = RiwayatAdapter(emptyList()) // <-- Diubah
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = riwayatAdapter // <-- Diubah

        // Buat data dummy
        createDummyTransactions()

        // ... (listener tombol tetap sama) ...
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

    private fun createDummyTransactions() {
        // Menggunakan CatatanKeuangan dan TipeCatatan
        allTransactions.add(CatatanKeuangan(getDate(-2, 5), "Simpanan Wajib", 100000.0, TipeCatatan.SIMPANAN)) // <-- Diubah
        allTransactions.add(CatatanKeuangan(getDate(-2, 15), "Bayar Angsuran #1", 250000.0, TipeCatatan.ANGSURAN)) // <-- Diubah
        allTransactions.add(CatatanKeuangan(getDate(-1, 5), "Simpanan Wajib", 100000.0, TipeCatatan.SIMPANAN)) // <-- Diubah
        allTransactions.add(CatatanKeuangan(getDate(-1, 10), "Pinjaman Renovasi", 2000000.0, TipeCatatan.PINJAMAN)) // <-- Diubah
        allTransactions.add(CatatanKeuangan(getDate(-1, 15), "Bayar Angsuran #2", 250000.0, TipeCatatan.ANGSURAN)) // <-- Diubah
        allTransactions.add(CatatanKeuangan(getDate(-1, 20), "Simpanan Sukarela", 50000.0, TipeCatatan.SIMPANAN)) // <-- Diubah
        allTransactions.add(CatatanKeuangan(getDate(0, 5), "Simpanan Wajib", 100000.0, TipeCatatan.SIMPANAN)) // <-- Diubah
        allTransactions.add(CatatanKeuangan(getDate(0, 10), "Simpanan Sukarela", 150000.0, TipeCatatan.SIMPANAN)) // <-- Diubah
    }

    private fun getDate(monthOffset: Int, day: Int): Date {
        // ... (fungsi ini tetap sama) ...
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, monthOffset)
        cal.set(Calendar.DAY_OF_MONTH, day)
        return cal.time
    }

    private fun updateUI() {
        // ... (logika filter dan update UI tetap sama) ...
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

            val totalSimpanan = filteredTransactions.filter { it.type == TipeCatatan.SIMPANAN }.sumOf { it.amount } // <-- Diubah
            val totalPinjaman = filteredTransactions.filter { it.type == TipeCatatan.PINJAMAN }.sumOf { it.amount } // <-- Diubah
            val totalAngsuran = filteredTransactions.filter { it.type == TipeCatatan.ANGSURAN }.sumOf { it.amount } // <-- Diubah

            val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0

            tvTotalSimpanan.text = format.format(totalSimpanan)
            tvTotalPinjaman.text = format.format(totalPinjaman)
            tvTotalAngsuran.text = format.format(totalAngsuran)

            riwayatAdapter.updateData(filteredTransactions.sortedBy { it.date }) // <-- Diubah
        }
    }
}