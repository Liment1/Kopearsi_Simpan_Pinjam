package com.example.project_map.ui.profile.laporan

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.CatatanKeuangan
import com.example.project_map.data.TipeCatatan
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class LaporanBulananActivity : AppCompatActivity() {

    // UI Components
    private lateinit var tvCurrentMonth: TextView
    private lateinit var tvTotalSimpanan: TextView
    private lateinit var tvTotalPinjaman: TextView
    private lateinit var tvTotalAngsuran: TextView
    private lateinit var tvSummaryTitle: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var cardSummary: MaterialCardView
    private lateinit var loadingProgressBar: android.widget.ProgressBar // Add a ProgressBar in XML if possible, or ignore if not

    // Logic & Data
    private lateinit var riwayatAdapter: RiwayatAdapter
    private val allTransactions = mutableListOf<CatatanKeuangan>()
    private val calendar = Calendar.getInstance()

    // Firebase
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
//
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_bulanan)

        // Init Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupViews()

        fetchTransactions()
    }

    private fun setupViews() {
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

        btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateUI()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateUI()
        }
    }

    private fun fetchTransactions() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch from: users -> [uid] -> transactions
        db.collection("users").document(userId).collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING) // Sort by date from DB
            .get()
            .addOnSuccessListener { result ->
                allTransactions.clear()
                for (document in result) {
                    try {
                        // Manual Mapping to ensure Enum and Date safety
                        val timestamp = document.getTimestamp("date")
                        val description = document.getString("description") ?: ""
                        val amount = document.getDouble("amount") ?: 0.0
                        val typeString = document.getString("type") ?: "SIMPANAN"

                        // Convert String back to Enum
                        val typeEnum = try {
                            TipeCatatan.valueOf(typeString)
                        } catch (e: Exception) {
                            TipeCatatan.SIMPANAN // Fallback
                        }

                        if (timestamp != null) {
                            val transaction = CatatanKeuangan(
                                date = timestamp.toDate(),
                                description = description,
                                amount = amount,
                                type = typeEnum
                            )
                            allTransactions.add(transaction)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                updateUI()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI() {
        // Update Month Label
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("in", "ID"))
        val currentMonthStr = monthFormat.format(calendar.time)
        tvCurrentMonth.text = currentMonthStr
        tvSummaryTitle.text = "Ringkasan Bulan $currentMonthStr"

        // Filter data locally for the selected month
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

            // Reset totals to 0
            tvTotalSimpanan.text = "Rp0"
            tvTotalPinjaman.text = "Rp0"
            tvTotalAngsuran.text = "Rp0"
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

            riwayatAdapter.updateData(filteredTransactions)
        }
    }

    private fun getDate(year: Int, month: Int, day: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(year, month, day)
        return cal.time
    }
}