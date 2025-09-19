package com.example.project_map

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class SimpanFragment : Fragment(R.layout.fragment_simpan), TarikBottomSheet.OnNominalEntered {

    private var totalSimpanan = 10000000
    private lateinit var transaksi: MutableList<Transaction>
    private lateinit var adapter: TransactionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTotal: TextView = view.findViewById(R.id.tvTotalSimpanan)
        val recycler: RecyclerView = view.findViewById(R.id.recyclerView)
        val btnSimpan: Button = view.findViewById(R.id.btnTarik)

        tvTotal.text = "Rp %,d".format(totalSimpanan)
        btnSimpan.text = "Simpan Uang"

        transaksi = mutableListOf(
            Transaction("2025-09-01", "Setoran Awal", "+ Rp 5.000.000"),
            Transaction("2025-09-10", "Setoran Bulanan", "+ Rp 1.000.000"),
            Transaction("2025-09-15", "Penarikan", "- Rp 500.000"),
            Transaction("2025-09-20", "Setoran Bulanan", "+ Rp 1.500.000"),
            Transaction("2025-09-25", "Setoran Bonus", "+ Rp 3.000.000")
        )

        adapter = TransactionAdapter(transaksi)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        btnSimpan.setOnClickListener {
            TarikBottomSheet(this).show(parentFragmentManager, "SimpanBottomSheet")
        }
    }

    override fun onNominalEntered(nominal: Int) {
        totalSimpanan += nominal
        view?.findViewById<TextView>(R.id.tvTotalSimpanan)?.text = "Rp %,d".format(totalSimpanan)

        val newTransaction = Transaction(
            tanggal = getCurrentDate(),
            keterangan = "Setoran Sukarela",
            jumlah = "+ Rp %,d".format(nominal)
        )
        adapter.addTransaction(newTransaction)
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
