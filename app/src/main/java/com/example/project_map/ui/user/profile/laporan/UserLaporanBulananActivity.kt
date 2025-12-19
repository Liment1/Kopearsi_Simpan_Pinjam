package com.example.project_map.ui.user.profile.laporan

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView


class UserLaporanBulananActivity : AppCompatActivity() {

    // MVVM: Inject ViewModel
    private val viewModel: UserLaporanViewModel by viewModels()

    private lateinit var userRiwayatAdapter: UserRiwayatAdapter

    // UI Refs
    private lateinit var tvCurrentMonth: TextView
    private lateinit var tvSummaryTitle: TextView
    private lateinit var tvTotalSimpanan: TextView
    private lateinit var tvTotalPinjaman: TextView
    private lateinit var tvTotalAngsuran: TextView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var cardSummary: MaterialCardView
    private lateinit var rvTransactions: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_bulanan)

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
        tvSummaryTitle = findViewById(R.id.tvSummaryTitle)
        tvTotalSimpanan = findViewById(R.id.tvTotalSimpanan)
        tvTotalPinjaman = findViewById(R.id.tvTotalPinjaman)
        tvTotalAngsuran = findViewById(R.id.tvTotalAngsuran)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        cardSummary = findViewById(R.id.cardSummary)
        rvTransactions = findViewById(R.id.rvTransactions)

        val btnPreviousMonth = findViewById<ImageButton>(R.id.btnPreviousMonth)
        val btnNextMonth = findViewById<ImageButton>(R.id.btnNextMonth)

        // Init Adapter
        userRiwayatAdapter = UserRiwayatAdapter(emptyList())
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = userRiwayatAdapter

        // Actions
        btnPreviousMonth.setOnClickListener { viewModel.changeMonth(-1) }
        btnNextMonth.setOnClickListener { viewModel.changeMonth(1) }
    }

    private fun setupObservers() {
        // 1. Observe Month Title
        viewModel.monthTitle.observe(this) { title ->
            tvCurrentMonth.text = title
            tvSummaryTitle.text = "Ringkasan Bulan $title"
        }

        // 2. Observe Financial Summary (Totals)
        viewModel.financialSummary.observe(this) { summary ->
            tvTotalSimpanan.text = summary.totalSimpanan
            tvTotalPinjaman.text = summary.totalPinjaman
            tvTotalAngsuran.text = summary.totalAngsuran
        }

        // 3. Observe List Data
        viewModel.transactionList.observe(this) { list ->
            userRiwayatAdapter.updateData(list)
        }

        // 4. Observe Empty State
        viewModel.isEmpty.observe(this) { isEmpty ->
            if (isEmpty) {
                layoutEmpty.visibility = View.VISIBLE
                rvTransactions.visibility = View.GONE
                cardSummary.visibility = View.GONE
            } else {
                layoutEmpty.visibility = View.GONE
                rvTransactions.visibility = View.VISIBLE
                cardSummary.visibility = View.VISIBLE
            }
        }
    }
}