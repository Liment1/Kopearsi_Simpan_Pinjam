package com.example.project_map.ui.adminhexsel

import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.*
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FragmentTransaksiSimpanan : Fragment() {

    data class TransaksiSimpanan(
        val nama: String,
        val jenis: String,
        val jumlah: Int,
        val tanggal: String // yyyy-MM-dd HH:mm:ss
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpananAdapter
    private lateinit var spinnerJenis: Spinner
    private lateinit var spinnerRange: Spinner
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnExport: Button
    private lateinit var txtPage: TextView

    private val fullList = mutableListOf<TransaksiSimpanan>()
    private var filteredList = listOf<TransaksiSimpanan>()
    private val maxItems = 5
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_transaksi_simpanan, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewSimpanan)
        spinnerJenis = view.findViewById(R.id.spinnerFilterJenis)
        spinnerRange = view.findViewById(R.id.spinnerFilterRange)
        btnPrev = view.findViewById(R.id.btnPrev)
        btnNext = view.findViewById(R.id.btnNext)
        txtPage = view.findViewById(R.id.txtPage)
        btnExport = view.findViewById(R.id.btnExportPDF)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Dummy data
        fullList.addAll(listOf(
            TransaksiSimpanan("Andi", "Pokok", 500000, "2025-10-01 08:10:00"),
            TransaksiSimpanan("Budi", "Wajib", 100000, "2025-10-02 10:30:00"),
            TransaksiSimpanan("Citra", "Sukarela", 250000, "2025-10-03 09:15:00"),
            TransaksiSimpanan("Dewi", "Pokok", 300000, "2025-09-20 14:00:00"),
            TransaksiSimpanan("Eka", "Wajib", 150000, "2025-09-15 16:20:00"),
            TransaksiSimpanan("Fajar", "Sukarela", 200000, "2025-08-10 11:05:00"),
            TransaksiSimpanan("Gita", "Pokok", 400000, "2025-08-01 12:30:00"),
            TransaksiSimpanan("Hadi", "Wajib", 120000, "2025-07-15 09:00:00"),
            TransaksiSimpanan("Indra", "Sukarela", 350000, "2025-06-18 10:10:00")
        ))

        // Setup spinners
        val jenisOptions = listOf("Semua", "Pokok", "Wajib", "Sukarela")
        val rangeOptions = listOf("Semua", "Hari ini", "Minggu ini", "Bulan ini", "Tahun ini")

        spinnerJenis.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, jenisOptions)
        spinnerRange.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, rangeOptions)

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilter(
                    jenisOptions[spinnerJenis.selectedItemPosition],
                    rangeOptions[spinnerRange.selectedItemPosition]
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerJenis.onItemSelectedListener = filterListener
        spinnerRange.onItemSelectedListener = filterListener

        btnPrev.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }

        btnNext.setOnClickListener {
            val totalPage = (filteredList.size - 1) / maxItems
            if (currentPage < totalPage) {
                currentPage++
                updatePage()
            }
        }

        btnExport.setOnClickListener {
            exportPdf()
        }

        applyFilter("Semua", "Semua")
        return view
    }

    private fun applyFilter(jenis: String, range: String) {
        val now = Calendar.getInstance()
        filteredList = fullList.filter { item ->
            val matchesJenis = jenis == "Semua" || item.jenis == jenis
            val matchesRange = when (range) {
                "Semua" -> true
                "Hari ini" -> item.tanggal.startsWith(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time))
                "Minggu ini" -> {
                    val calItem = Calendar.getInstance().apply { time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.tanggal)!! }
                    calItem.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)
                            && calItem.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                }
                "Bulan ini" -> {
                    val calItem = Calendar.getInstance().apply { time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.tanggal)!! }
                    calItem.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                            && calItem.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                }
                "Tahun ini" -> {
                    val calItem = Calendar.getInstance().apply { time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.tanggal)!! }
                    calItem.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                }
                else -> true
            }
            matchesJenis && matchesRange
        }
        currentPage = 0
        updatePage()
    }

    private fun updatePage() {
        val start = currentPage * maxItems
        val end = minOf(start + maxItems, filteredList.size)
        val pageList = filteredList.subList(start, end)
        adapter = SimpananAdapter(pageList)
        recyclerView.adapter = adapter
        val totalPage = if (filteredList.isEmpty()) 1 else ((filteredList.size - 1) / maxItems + 1)
        txtPage.text = "Page ${currentPage + 1} / $totalPage"
    }

    inner class SimpananAdapter(private val data: List<TransaksiSimpanan>) :
        RecyclerView.Adapter<SimpananAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val card: CardView = itemView.findViewById(R.id.cardRoot)
            val ivProof: ImageView = itemView.findViewById(R.id.ivProof)
            val tvNama: TextView = itemView.findViewById(R.id.tvNama)
            val tvKeterangan: TextView = itemView.findViewById(R.id.tvKeterangan)
            val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
            val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simpanan, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            holder.tvNama.text = item.nama
            holder.tvKeterangan.text = item.jenis
            holder.tvTanggal.text = item.tanggal.split(" ")[0]
            holder.tvJumlah.text = "+ Rp %,d".format(item.jumlah).replace(',', '.')

            // --- PENYESUAIAN DIMULAI DI SINI ---
            if (item.jenis == "Sukarela") {
                // Jika sukarela, tampilkan ImageView
                holder.ivProof.visibility = View.VISIBLE
                holder.ivProof.setImageResource(R.drawable.placeholder_image)
            } else {
                // Jika pokok atau wajib, sembunyikan ImageView
                holder.ivProof.visibility = View.GONE
            }
            // --- PENYESUAIAN SELESAI ---


            holder.card.setOnClickListener {
                val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_detail, null)
                val ivProofDialog = dialogView.findViewById<ImageView>(R.id.ivProof)
                val tvType = dialogView.findViewById<TextView>(R.id.tvType)
                val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)
                val tvTime = dialogView.findViewById<TextView>(R.id.tvTime)

                tvType.text = "${item.nama} - ${item.jenis}"
                val (date, time) = item.tanggal.split(" ")
                tvDate.text = "Tanggal: $date"
                tvTime.text = "Jam: $time"

                // --- PENYESUAIAN DI SINI JUGA UNTUK DIALOG ---
                if (item.jenis == "Sukarela") {
                    // Tampilkan gambar di dialog
                    ivProofDialog.visibility = View.VISIBLE
                    ivProofDialog.setImageResource(R.drawable.placeholder_image)
                } else {
                    // Sembunyikan gambar di dialog
                    ivProofDialog.visibility = View.GONE
                }
                // --- PENYESUAIAN SELESAI ---

                AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton("Tutup", null)
                    .show()
            }
        }

        override fun getItemCount() = data.size
    }

    private fun exportPdf() {
        val pdf = PdfDocument()
        val paint = Paint()
        var y = 50f

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        paint.textSize = 16f
        canvas.drawText("Laporan Transaksi Simpanan", 50f, y, paint)
        y += 30f

        filteredList.forEach { item ->
            val line = "${item.tanggal.split(" ")[0]} - ${item.nama} - ${item.jenis} - Rp %,d".format(item.jumlah).replace(',', '.')
            canvas.drawText(line, 50f, y, paint)
            y += 25f
        }

        pdf.finishPage(page)

        try {
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloads, "Export_Simpanan.pdf")
            pdf.writeTo(FileOutputStream(file))
            Toast.makeText(requireContext(), "PDF berhasil disimpan di Download", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
        pdf.close()
    }
}