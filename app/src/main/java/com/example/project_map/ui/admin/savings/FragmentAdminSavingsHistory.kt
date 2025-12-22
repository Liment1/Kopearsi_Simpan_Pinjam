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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Savings
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class FragmentAdminSavingsHistory : Fragment() {

    private val viewModel: SavingsHistoryViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpananAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_admin_savings_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewSimpanan)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupJenis)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SimpananAdapter(emptyList()) { savings ->
            val bundle = Bundle().apply {
                putString("transactionId", savings.id)
                putString("userId", savings.userId)
            }
            try {
                findNavController().navigate(R.id.action_adminSavingsHistoryFragment_to_adminSavingsDetailFragment, bundle)
            } catch (e: Exception) {
                Snackbar.make(view, "Navigasi belum diatur", Snackbar.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter

        viewModel.filteredSavingss.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/Hide progress bar if you have one
        }

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

    inner class SimpananAdapter(
        private var data: List<Savings>,
        private val onItemClick: (Savings) -> Unit
    ) : RecyclerView.Adapter<SimpananAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvKeterangan) // Using Top Text for Name
            val tvDetails: TextView = itemView.findViewById(R.id.tvTanggal) // Using Bottom Text for Type/Date
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

            // 1. Show Name
            holder.tvName.text = if (item.userName.isNotEmpty()) item.userName else "Member #${item.userId.take(5)}"

            // 2. Show Type & Date
            val sdf = SimpleDateFormat("dd MMM", Locale("in", "ID"))
            val dateStr = if(item.date != null) sdf.format(item.date) else "-"
            holder.tvDetails.text = "${item.type} • $dateStr"

            holder.tvJumlah.text = "+ ${format.format(item.amount)}"

            // 3. Icon visibility
            holder.ivProof.visibility = if (item.type == "Simpanan Sukarela" || item.proofUrl.isNotEmpty()) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener { onItemClick(item) }
        }

        override fun getItemCount() = data.size

        fun updateList(newData: List<Savings>) {
            data = newData
            notifyDataSetChanged()
        }
    }
}