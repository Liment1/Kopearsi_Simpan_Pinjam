package com.example.project_map.ui.admin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.UserData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

class AdminDataAnggotaFragment : Fragment() {

    private lateinit var rvAnggota: RecyclerView
    private lateinit var fabTambahAnggota: FloatingActionButton
    private lateinit var anggotaAdapter: AnggotaAdapter
    private lateinit var db: FirebaseFirestore

    private var allMembers = mutableListOf<UserData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_data_anggota, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        rvAnggota = view.findViewById(R.id.rvAnggota)
        fabTambahAnggota = view.findViewById(R.id.fabTambahAnggota)

        setupRecyclerView()
        fetchMembers()

        fabTambahAnggota.setOnClickListener {
            // Adding a user usually requires Auth creation which is complex here.
            // For now, we'll show a toast or implement basic Firestore add if needed.
            Toast.makeText(context, "Fitur tambah anggota manual belum tersedia (Perlu Auth)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        anggotaAdapter = AnggotaAdapter(allMembers) { anggota ->
            showFinancialSummaryDialog(anggota)
        }
        rvAnggota.layoutManager = LinearLayoutManager(requireContext())
        rvAnggota.adapter = anggotaAdapter
    }

    private fun fetchMembers() {
        // Fetch all users
        db.collection("users").get()
            .addOnSuccessListener { result ->
                allMembers.clear()
                for (document in result) {
                    // Manual mapping to handle potential missing fields
                    val user = UserData(
                        id = document.id, // Using Doc ID as ID
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        phone = document.getString("phone") ?: "",
                        status = document.getString("status") ?: "Aktif",
                        memberCode = document.getString("memberCode") ?: ""
                    )
                    allMembers.add(user)
                }
                anggotaAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal memuat data anggota", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showFinancialSummaryDialog(anggota: UserData) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_financial_summary, null)
        val tvNamaAnggota = dialogView.findViewById<TextView>(R.id.tvNamaAnggota)
        val tvTotalSimpanan = dialogView.findViewById<TextView>(R.id.tvTotalSimpananValue)
        val tvTotalPinjaman = dialogView.findViewById<TextView>(R.id.tvTotalPinjamanValue)
        val btnEditData = dialogView.findViewById<Button>(R.id.btnEditData)

        tvNamaAnggota.text = anggota.name

        // Fetch Financials from Firestore for this specific user
        val userRef = db.collection("users").document(anggota.id)

        userRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val totalSimpanan = doc.getDouble("totalSimpanan") ?: 0.0
                // For Total Pinjaman (Outstanding), we might need to sum 'sisaAngsuran' of active loans
                // Or if you store a running total 'totalPinjaman' in user doc, use that.
                // Here we fetch active loans count for simplicity or calculate:

                tvTotalSimpanan.text = formatCurrency(totalSimpanan)
                // Placeholder for loan calc or fetch separate collection
                tvTotalPinjaman.text = "Loading..."

                // Calculate Total Outstanding Loan
                userRef.collection("loans").whereNotEqualTo("status", "Lunas").get()
                    .addOnSuccessListener { loans ->
                        val outstanding = loans.documents.sumOf { it.getDouble("sisaAngsuran") ?: 0.0 }
                        tvTotalPinjaman.text = formatCurrency(outstanding)
                    }
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnEditData.setOnClickListener {
            dialog.dismiss()
            showEditAnggotaDialog(anggota) // Call the new function
        }
        dialog.show()
    }

    private fun formatCurrency(value: Double): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        format.maximumFractionDigits = 0
        return format.format(value)
    }

    private fun showEditAnggotaDialog(anggota: UserData) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_form_anggota, null)

        val etNama = dialogView.findViewById<EditText>(R.id.etNamaForm)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmailForm) // Read-only usually
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhoneForm)
        val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerStatus)

        // Hide Password fields for Edit mode if not needed
        dialogView.findViewById<View>(R.id.layoutPasswordLama)?.visibility = View.GONE

        // Pre-fill Data
        etNama.setText(anggota.name)
        etEmail.setText(anggota.email)
        etEmail.isEnabled = false // Email is usually unique/ID based, so disable edit
        etPhone.setText(anggota.phone)

        // Setup Status Spinner
        val statusList = listOf("Anggota Aktif", "Calon Anggota", "Diblokir", "Tidak Aktif")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter

        // Set current selection
        val statusIndex = statusList.indexOf(anggota.status)
        if (statusIndex >= 0) spinnerStatus.setSelection(statusIndex)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Anggota: ${anggota.name}")
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, _ ->
                val newName = etNama.text.toString()
                val newPhone = etPhone.text.toString()
                val newStatus = spinnerStatus.selectedItem.toString()

                updateMemberInFirestore(anggota.id, newName, newPhone, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateMemberInFirestore(uid: String, name: String, phone: String, status: String) {
        val updates = mapOf(
            "name" to name,
            "phone" to phone,
            "status" to status
        )

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                fetchMembers() // Refresh list
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal update: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}