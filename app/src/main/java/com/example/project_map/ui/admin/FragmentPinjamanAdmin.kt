package com.example.project_map.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import com.example.project_map.R
import com.example.project_map.data.Loan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.jvm.java

class FragmentPinjamanAdmin : Fragment() {

    private lateinit var btnSimpan: Button
    private lateinit var tvDaftar: TextView
    private lateinit var tvHistory: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var txtPage: TextView
    private lateinit var containerCards: LinearLayout
    private lateinit var db: FirebaseFirestore

    private val daftarPengajuan = mutableListOf<Loan>() // "Proses"
    private val daftarHistory = mutableListOf<Loan>() // "Disetujui" or "Ditolak"

    private var showingHistory = false
    private val maxItems = 5
    private var currentPage = 0
    // Note: Storing DocumentReference to update status later
    private val loanRefs = mutableMapOf<String, com.google.firebase.firestore.DocumentReference>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pinjaman_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        btnSimpan = view.findViewById(R.id.btnSimpan)
        tvDaftar = view.findViewById(R.id.tvDaftar)
        tvHistory = view.findViewById(R.id.tvHistory)
        btnPrev = view.findViewById(R.id.btnPrev)
        btnNext = view.findViewById(R.id.btnNext)
        txtPage = view.findViewById(R.id.txtPage)
        containerCards = view.findViewById(R.id.containerCards)

        fetchLoans()

        tvDaftar.setOnClickListener {
            showingHistory = false
            currentPage = 0
            updateToggleUI()
            updatePage()
        }

        tvHistory.setOnClickListener {
            showingHistory = true
            currentPage = 0
            updateToggleUI()
            updatePage()
        }

        btnPrev.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }

        btnNext.setOnClickListener {
            val list = if (showingHistory) daftarHistory else daftarPengajuan
            if ((currentPage + 1) * maxItems < list.size) {
                currentPage++
                updatePage()
            }
        }

        updateToggleUI()
    }

    private fun fetchLoans() {
        // Query ALL "loans" collections across all users
        db.collectionGroup("loans").orderBy("tanggalPengajuan", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { result ->
                daftarPengajuan.clear()
                daftarHistory.clear()
                loanRefs.clear()

                for (doc in result) {
                    val loan = doc.toObject(Loan::class.java)
                    // Save reference to update later
                    loanRefs[loan.id] = doc.reference

                    if (loan.status == "Proses") {
                        daftarPengajuan.add(loan)
                    } else {
                        daftarHistory.add(loan)
                    }
                }
                updatePage()
            }
            .addOnFailureListener {  e ->
                android.util.Log.e("FirestoreError", "Loans Query Failed", e)
            //                Toast.makeText(context, "Gagal memuat pinjaman: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateStatus(loanId: String, newStatus: String) {
        val ref = loanRefs[loanId]
        if (ref != null) {
            ref.update("status", newStatus)
                .addOnSuccessListener {
                    Toast.makeText(context, "Status diubah menjadi $newStatus", Toast.LENGTH_SHORT).show()
                    fetchLoans() // Refresh list
                }
        }
    }

    private fun updateToggleUI() {
        if (showingHistory) {
            tvHistory.setTextColor(Color.BLACK)
            tvDaftar.setTextColor(Color.DKGRAY)
        } else {
            tvDaftar.setTextColor(Color.BLACK)
            tvHistory.setTextColor(Color.DKGRAY)
        }
    }

    private fun updatePage() {
        val list = if (showingHistory) daftarHistory else daftarPengajuan
        val start = currentPage * maxItems
        val end = minOf(start + maxItems, list.size)

        containerCards.removeAllViews()

        if (list.isEmpty()) {
            txtPage.text = "No Data"
            return
        }

        val sublist = list.subList(start, end)

        for (item in sublist) {
            val card = layoutInflater.inflate(R.layout.item_pinjaman_admin, containerCards, false)

            val txtNama = card.findViewById<TextView>(R.id.txtNamaPinjaman)
            val txtJumlah = card.findViewById<TextView>(R.id.txtJumlahPinjaman)
            val txtStatus = card.findViewById<TextView>(R.id.txtStatusPinjaman)
            val btnSetuju = card.findViewById<Button>(R.id.btnSetuju)
            val btnTolak = card.findViewById<Button>(R.id.btnTolak)

            txtNama.text = item.namaPeminjam
            txtJumlah.text = "Rp %,d".format(item.nominal.toLong())
            txtStatus.text = item.status

            // Coloring logic...
            when (item.status) {
                "Proses" -> {
                    card.setBackgroundColor(Color.parseColor("#FFF3CD"))
                    txtStatus.setTextColor(Color.parseColor("#856404"))
                }
                "Disetujui" -> {
                    card.setBackgroundColor(Color.parseColor("#D4EDDA"))
                    txtStatus.setTextColor(Color.parseColor("#155724"))
                }
                "Ditolak" -> {
                    card.setBackgroundColor(Color.parseColor("#F8D7DA"))
                    txtStatus.setTextColor(Color.parseColor("#721C24"))
                }
            }

            if (showingHistory) {
                btnSetuju.visibility = View.GONE
                btnTolak.visibility = View.GONE
            } else {
                btnSetuju.visibility = View.VISIBLE
                btnTolak.visibility = View.VISIBLE

                btnSetuju.setOnClickListener { updateStatus(item.id, "Disetujui") }
                btnTolak.setOnClickListener { updateStatus(item.id, "Ditolak") }
            }

            containerCards.addView(card)
        }

        txtPage.text = "Page ${currentPage + 1} / ${((list.size - 1) / maxItems) + 1}"
    }
}