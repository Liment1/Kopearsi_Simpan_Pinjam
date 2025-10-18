package com.example.project_map.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import com.example.project_map.R

class FragmentPinjamanAdmin : Fragment() {

    data class PengajuanPinjaman(
        val nama: String,
        val jumlahPokok: Int,
        var status: String
    )

    private lateinit var etBunga: EditText
    private lateinit var etDenda: EditText
    private lateinit var btnSimpan: Button
    private lateinit var tvDaftar: TextView
    private lateinit var tvHistory: TextView
    private lateinit var underlineToggle: View
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var txtPage: TextView
    private lateinit var containerCards: LinearLayout

    private val daftarPengajuan = mutableListOf<PengajuanPinjaman>()
    private val daftarHistory = mutableListOf<PengajuanPinjaman>()
    private var showingHistory = false
    private val maxItems = 5
    private var currentPage = 0
    private var bungaPersen = 0
    private var dendaPersen = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pinjaman_admin, container, false)

        etBunga = view.findViewById(R.id.etBunga)
        etDenda = view.findViewById(R.id.etDenda)
        btnSimpan = view.findViewById(R.id.btnSimpan)
        tvDaftar = view.findViewById(R.id.tvDaftar)
        tvHistory = view.findViewById(R.id.tvHistory)
        underlineToggle = view.findViewById(R.id.underlineToggle)
        btnPrev = view.findViewById(R.id.btnPrev)
        btnNext = view.findViewById(R.id.btnNext)
        txtPage = view.findViewById(R.id.txtPage)
        containerCards = view.findViewById(R.id.containerCards)

        // Dummy data
        for (i in 1..13) {
            daftarPengajuan.add(PengajuanPinjaman("Orang $i", i * 1000000, "Menunggu"))
        }

        updatePage()
        updateToggleUI()

        tvDaftar.setOnClickListener {
            showingHistory = false
            currentPage = 0
            updatePage()
            updateToggleUI()
        }

        tvHistory.setOnClickListener {
            showingHistory = true
            currentPage = 0
            updatePage()
            updateToggleUI()
        }

        btnPrev.setOnClickListener {
            if (currentPage > 0) currentPage--
            updatePage()
        }

        btnNext.setOnClickListener {
            val list = if (showingHistory) daftarHistory else daftarPengajuan
            if ((currentPage + 1) * maxItems < list.size) currentPage++
            updatePage()
        }

        btnSimpan.setOnClickListener {
            val b = etBunga.text.toString().toIntOrNull() ?: 0
            val d = etDenda.text.toString().toIntOrNull() ?: 0
            bungaPersen = b
            dendaPersen = d
            Toast.makeText(requireContext(), "Bunga: $bungaPersen% | Denda: $dendaPersen%", Toast.LENGTH_SHORT).show()
            updatePage() // Perbarui tampilan dengan total otomatis
        }

        return view
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
        val sublist = list.subList(start, end)

        containerCards.removeAllViews()

        for (item in sublist) {
            val card = layoutInflater.inflate(R.layout.item_pinjaman_admin, containerCards, false)

            val txtNama = card.findViewById<TextView>(R.id.txtNamaPinjaman)
            val txtJumlah = card.findViewById<TextView>(R.id.txtJumlahPinjaman)
            val txtStatus = card.findViewById<TextView>(R.id.txtStatusPinjaman)
            val btnSetuju = card.findViewById<Button>(R.id.btnSetuju)
            val btnTolak = card.findViewById<Button>(R.id.btnTolak)

            val total = item.jumlahPokok + (item.jumlahPokok * bungaPersen / 100) + (item.jumlahPokok * dendaPersen / 100)

            txtNama.text = item.nama
            txtJumlah.text = "Rp %,d".format(total)
            txtStatus.text = item.status

            // Set warna card sesuai status
            when (item.status) {
                "Menunggu" -> {
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
                else -> {
                    card.setBackgroundColor(Color.WHITE)
                    txtStatus.setTextColor(Color.BLACK)
                }
            }

            if (showingHistory) {
                btnSetuju.visibility = View.GONE
                btnTolak.visibility = View.GONE
            } else {
                btnSetuju.visibility = View.VISIBLE
                btnTolak.visibility = View.VISIBLE

                btnSetuju.setOnClickListener {
                    item.status = "Disetujui"
                    daftarPengajuan.remove(item)
                    daftarHistory.add(item)
                    updatePage()
                }

                btnTolak.setOnClickListener {
                    item.status = "Ditolak"
                    daftarPengajuan.remove(item)
                    daftarHistory.add(item)
                    updatePage()
                }
            }

            // Klik card untuk detail
            card.setOnClickListener {
                val total = item.jumlahPokok + (item.jumlahPokok * bungaPersen / 100) + (item.jumlahPokok * dendaPersen / 100)
                val dialogView = layoutInflater.inflate(R.layout.dialog_pinjaman_admin_detail, null)
                val dialogRoot = dialogView.findViewById<LinearLayout>(R.id.dialogRoot)

                // Set warna background sesuai status
                when (item.status) {
                    "Menunggu" -> dialogRoot.setBackgroundColor(Color.parseColor("#FFF3CD"))
                    "Disetujui" -> dialogRoot.setBackgroundColor(Color.parseColor("#D4EDDA"))
                    "Ditolak" -> dialogRoot.setBackgroundColor(Color.parseColor("#F8D7DA"))
                    else -> dialogRoot.setBackgroundColor(Color.WHITE)
                }

                // Set isi teks
                dialogView.findViewById<TextView>(R.id.txtDetailNama).text = "Nama: ${item.nama}"
                dialogView.findViewById<TextView>(R.id.txtDetailJumlah).text = "Jumlah Pokok: Rp %,d".format(item.jumlahPokok)
                dialogView.findViewById<TextView>(R.id.txtDetailStatus).text = "Status: ${item.status}"
                dialogView.findViewById<TextView>(R.id.txtDetailBunga).text = "Bunga: $bungaPersen%"
                dialogView.findViewById<TextView>(R.id.txtDetailDenda).text = "Denda: $dendaPersen%"
                dialogView.findViewById<TextView>(R.id.txtDetailTotal).text = "Total: Rp %,d".format(total)

                // Buat AlertDialog
                val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton("OK", null)
                    .create()

                dialog.show()

                // Kustom tombol OK agar warnanya sesuai status
                val btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                when (item.status) {
                    "Menunggu" -> btnOk.setBackgroundColor(Color.parseColor("#FFF3CD"))
                    "Disetujui" -> btnOk.setBackgroundColor(Color.parseColor("#D4EDDA"))
                    "Ditolak" -> btnOk.setBackgroundColor(Color.parseColor("#F8D7DA"))
                    else -> btnOk.setBackgroundColor(Color.WHITE)
                }
                btnOk.setTextColor(Color.BLACK)
            }



            containerCards.addView(card)
        }

        txtPage.text = "Page ${currentPage + 1} / ${((list.size - 1) / maxItems) + 1}"
    }
}
