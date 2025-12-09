package com.example.project_map.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.Transaction
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class FragmentHistoriSimpanan : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpananAdapter
    // Note: If you have switched to Chips, you might want to remove this Spinner entirely later.
//    private lateinit var spinnerJenis: Spinner
    private lateinit var db: FirebaseFirestore

    private val fullList = mutableListOf<Transaction>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_riwayat_simpanan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize Firebase FIRST
        db = FirebaseFirestore.getInstance()

        // 2. Initialize Views
        recyclerView = view.findViewById(R.id.recyclerViewSimpanan)

        // --- FIX: UNCOMMENTED THIS LINE ---
        // This connects the variable to the XML. Without this, the app crashes.
//        spinnerJenis = view.findViewById(R.id.spinnerJenis)
        // ----------------------------------

        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupJenis)

        // 3. Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Initialize adapter with empty list first to avoid "No adapter attached" warning
        adapter = SimpananAdapter(emptyList())
        recyclerView.adapter = adapter

        // 4. Setup ChipGroup Logic
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

                val filter = when(checkedIds[0]) {
                    R.id.chipPokok -> "Simpanan Pokok"
                    R.id.chipWajib -> "Simpanan Wajib"
                    R.id.chipSukarela -> "Simpanan Sukarela"
                    else -> "Semua"
                }
                applyFilter(filter)
            }
        }

        // 5. Setup Spinner Logic


        // 6. Load Data
        fetchAllSavings()
    }

    private fun fetchAllSavings() {
        db.collectionGroup("savings")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                fullList.clear()
                for (doc in result) {
                    // Handle potential null/data mismatch safely
                    try {
                        val trans = doc.toObject(Transaction::class.java)
                        fullList.add(trans)
                    } catch (e: Exception) {
                        // Skip bad data
                    }
                }
                // Default filter
                applyFilter("Semua")
            }
            .addOnFailureListener { e->
                android.util.Log.e("FirestoreError", "Savings Query Failed", e)
            }
    }

    private fun applyFilter(jenis: String) {
        val filteredList = if (jenis == "Semua") {
            fullList
        } else {
            fullList.filter { it.type == jenis }
        }
        // Update the existing adapter instead of creating a new one every time (better performance)
        adapter = SimpananAdapter(filteredList)
        recyclerView.adapter = adapter
    }

    inner class SimpananAdapter(private val data: List<Transaction>) :
        RecyclerView.Adapter<SimpananAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvKeterangan: TextView = itemView.findViewById(R.id.tvKeterangan)
            val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
            val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
            val ivProof: ImageView = itemView.findViewById(R.id.ivProof)
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

            // Check for Simpanan Sukarela logic
            if (item.type == "Simpanan Sukarela") {
                holder.ivProof.visibility = View.VISIBLE
                // holder.ivProof.setImageResource(R.drawable.placeholder_image) // Ensure this drawable exists
            } else {
                holder.ivProof.visibility = View.GONE
            }
        }

        override fun getItemCount() = data.size
    }
}