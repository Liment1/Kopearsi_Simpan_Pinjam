package com.example.project_map.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdminAngsuranFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AdminInstallmentAdapter

    // Simple data class for Admin View
    data class InstallmentItem(
        val amount: Double,
        val date: Date?,
        val type: String,
        val isPaid: Boolean,
        val number: Int
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Ensure you created fragment_admin_angsuran.xml as discussed previously
        return inflater.inflate(R.layout.fragment_admin_angsuran, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        recycler = view.findViewById(R.id.recyclerViewAngsuran)
        recycler.layoutManager = LinearLayoutManager(context)

        fetchAllInstallments()
    }

    private fun fetchAllInstallments() {
        // Fetch ALL installments from ANY user where isPaid == true
        db.collectionGroup("installments")
            .whereEqualTo("isPaid", true)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<InstallmentItem>()
                for (doc in result) {
                    val amount = doc.getDouble("amount") ?: 0.0
                    val type = doc.getString("type") ?: ""
                    val number = doc.getLong("number")?.toInt() ?: 0
                    val date = doc.getDate("date")
                    val isPaid = doc.getBoolean("isPaid") ?: false

                    list.add(InstallmentItem(amount, date, type, isPaid, number))
                }
                adapter = AdminInstallmentAdapter(list)
                recycler.adapter = adapter
            }
            .addOnFailureListener {
                // CHECK LOGCAT FOR INDEX LINK HERE
                Toast.makeText(context, "Error: ${it.message}. Check Logcat for Index link.", Toast.LENGTH_LONG).show()
            }
    }

    // Inner Adapter Class
    inner class AdminInstallmentAdapter(private val items: List<InstallmentItem>) :
        RecyclerView.Adapter<AdminInstallmentAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvType: TextView = v.findViewById(R.id.tvKeterangan) // reusing item_simpanan layout IDs
            val tvDate: TextView = v.findViewById(R.id.tvTanggal)
            val tvAmount: TextView = v.findViewById(R.id.tvJumlah)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // Reusing 'item_simpanan.xml' for simplicity, or use 'item_admin_angsuran.xml'
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_simpanan, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))

            holder.tvType.text = "${item.type} #${item.number}"
            holder.tvAmount.text = format.format(item.amount)
            holder.tvDate.text = if (item.date != null) dateFormat.format(item.date) else "-"
        }

        override fun getItemCount() = items.size
    }
}