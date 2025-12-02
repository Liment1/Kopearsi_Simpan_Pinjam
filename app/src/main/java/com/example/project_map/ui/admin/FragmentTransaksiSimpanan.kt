package com.example.project_map.ui.admin

import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class FragmentTransaksiSimpanan : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpananAdapter
    private lateinit var spinnerJenis: Spinner
    private lateinit var db: FirebaseFirestore

    private val fullList = mutableListOf<Transaction>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_transaksi_simpanan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.recyclerViewSimpanan)
        spinnerJenis = view.findViewById(R.id.spinnerFilterJenis)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val jenisOptions = listOf("Semua", "Simpanan Pokok", "Simpanan Wajib", "Simpanan Sukarela")
        spinnerJenis.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, jenisOptions)

        spinnerJenis.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                applyFilter(jenisOptions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        fetchAllSavings()
    }

    private fun fetchAllSavings() {
        // FETCH ALL SAVINGS FROM ALL USERS
        db.collectionGroup("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                fullList.clear()
                for (doc in result) {
                    val trans = doc.toObject(Transaction::class.java)
                    fullList.add(trans)
                }
                applyFilter("Semua")
            }
            .addOnFailureListener { e->
                // CHECK LOGCAT FOR INDEX LINK
                android.util.Log.e("FirestoreError", "Savings Query Failed", e)
//                Toast.makeText(context, "Error: ${it.message}. Index needed?", Toast.LENGTH_LONG).show()
            }
    }

    private fun applyFilter(jenis: String) {
        val filteredList = if (jenis == "Semua") {
            fullList
        } else {
            fullList.filter { it.type == jenis }
        }
        adapter = SimpananAdapter(filteredList)
        recyclerView.adapter = adapter
    }

    inner class SimpananAdapter(private val data: List<Transaction>) :
        RecyclerView.Adapter<SimpananAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvKeterangan: TextView = itemView.findViewById(R.id.tvKeterangan)
            val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
            val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
            val ivProof: ImageView = itemView.findViewById(R.id.ivProof) // Ensure this exists in item_simpanan
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simpanan, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            val format = java.text.NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0

            holder.tvKeterangan.text = item.type
            holder.tvJumlah.text = "+ ${format.format(item.amount)}"

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
            holder.tvTanggal.text = if(item.date != null) sdf.format(item.date) else "-"

            // Show icon if it's Sukarela (assuming layout supports it)
            if (item.type == "Simpanan Sukarela") {
                holder.ivProof.visibility = View.VISIBLE
                holder.ivProof.setImageResource(R.drawable.placeholder_image)
            } else {
                holder.ivProof.visibility = View.GONE
            }
        }

        override fun getItemCount() = data.size
    }
}