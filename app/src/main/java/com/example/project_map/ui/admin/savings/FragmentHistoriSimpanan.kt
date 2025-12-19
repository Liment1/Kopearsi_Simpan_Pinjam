package com.example.project_map.ui.admin.savings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Savings
import com.google.android.material.chip.ChipGroup
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class FragmentHistoriSimpanan : Fragment() {

    // Use delegation to get the ViewModel
    private val viewModel: SavingsHistoryViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpananAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_riwayat_simpanan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize Views
        recyclerView = view.findViewById(R.id.recyclerViewSimpanan)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupJenis)

        // 2. Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SimpananAdapter(emptyList())
        recyclerView.adapter = adapter

        // 3. Observe ViewModel
        viewModel.filteredSavingss.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/Hide progress bar if you have one in your layout
            // e.g., progressBar.visibility = if(isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Setup Chip Filters
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

                val filterType = when(checkedIds[0]) {
                    R.id.chipPokok -> "Simpanan Pokok"
                    R.id.chipWajib -> "Simpanan Wajib"
                    R.id.chipSukarela -> "Simpanan Sukarela"
                    else -> "Semua"
                }
                viewModel.applyFilter(filterType)
            }
        }
    }

    // --- Adapter (Kept Inner for Simplicity, or move to separate file) ---
    inner class SimpananAdapter(private var data: List<Savings>) :
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
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0

            holder.tvKeterangan.text = item.type
            holder.tvJumlah.text = "+ ${format.format(item.amount)}"

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
            holder.tvTanggal.text = if(item.date != null) sdf.format(item.date) else "-"

            if (item.type == "Simpanan Sukarela") {
                holder.ivProof.visibility = View.VISIBLE
                // Use Glide or Picasso here to load item.imageUri if it exists
            } else {
                holder.ivProof.visibility = View.GONE
            }
        }

        override fun getItemCount() = data.size

        fun updateList(newData: List<Savings>) {
            data = newData
            notifyDataSetChanged()
        }
    }
}