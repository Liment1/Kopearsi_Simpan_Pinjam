package com.example.project_map.ui.admin.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.UserData
import com.example.project_map.data.repository.admin.MemberFinancials
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

class AdminUserFragment : Fragment() {

    private val viewModel: AdminUserViewModel by viewModels()
    private lateinit var adminUserAdapter: AdminUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvAnggota = view.findViewById<RecyclerView>(R.id.rvAnggota)
        val fabTambahAnggota = view.findViewById<FloatingActionButton>(R.id.fabTambahAnggota)

        // 1. Setup Adapter
        adminUserAdapter = AdminUserAdapter(emptyList()) { anggota ->
            // On Item Click: Open Dialog Logic
            showFinancialSummaryDialog(anggota)
        }
        rvAnggota.layoutManager = LinearLayoutManager(requireContext())
        rvAnggota.adapter = adminUserAdapter

        // 2. Observe Data
        viewModel.members.observe(viewLifecycleOwner) { list ->
            // Update adapter data (You might need to add a 'updateList' function to your Adapter,
            // or re-instantiate if you didn't add that method yet.
            // Better practice: Add `updateList` to Adapter).
            // For now, re-using constructor logic:
            rvAnggota.adapter = AdminUserAdapter(list) { showFinancialSummaryDialog(it) }
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
                viewModel.onMessageShown()
            }
        }

        fabTambahAnggota.setOnClickListener {
            Toast.makeText(context, "Fitur tambah anggota manual belum tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Dialog Logic ---

    private fun showFinancialSummaryDialog(anggota: UserData) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_financial_summary, null)
        val tvNama = dialogView.findViewById<TextView>(R.id.tvNamaAnggota)
        val tvSimpanan = dialogView.findViewById<TextView>(R.id.tvTotalSimpananValue)
        val tvPinjaman = dialogView.findViewById<TextView>(R.id.tvTotalPinjamanValue)
        val btnEdit = dialogView.findViewById<Button>(R.id.btnEditData)

        tvNama.text = anggota.name
        tvSimpanan.text = "Loading..."
        tvPinjaman.text = "Loading..."

        // Trigger Data Load
        viewModel.loadMemberFinancials(anggota.id)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Observe just once for this specific dialog session would be tricky directly.
        // Instead, we observe the LiveData globally in onViewCreated, OR we use a simple observer here that we remove later.
        // Simplest MVVM approach for Dialogs: Observe inside the dialog creation scope.

        val observer = androidx.lifecycle.Observer<MemberFinancials?> { financials ->
            if (financials != null) {
                tvSimpanan.text = formatCurrency(financials.totalSavings)
                tvPinjaman.text = formatCurrency(financials.outstandingLoan)
            }
        }
        viewModel.selectedMemberFinancials.observe(viewLifecycleOwner, observer)

        dialog.setOnDismissListener {
            viewModel.selectedMemberFinancials.removeObserver(observer)
        }

        btnEdit.setOnClickListener {
            dialog.dismiss()
            showEditAnggotaDialog(anggota)
        }
        dialog.show()
    }

    private fun showEditAnggotaDialog(anggota: UserData) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_form_anggota, null)
        val etNama = dialogView.findViewById<EditText>(R.id.etNamaForm)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhoneForm)
        val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerStatus)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmailForm)

        // Setup UI
        dialogView.findViewById<View>(R.id.layoutPasswordLama)?.visibility = View.GONE
        etNama.setText(anggota.name)
        etPhone.setText(anggota.phone)
        etEmail.setText(anggota.email)
        etEmail.isEnabled = false

        val statusList = listOf("Anggota Aktif", "Calon Anggota", "Diblokir", "Tidak Aktif")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter
        spinnerStatus.setSelection(statusList.indexOf(anggota.status).coerceAtLeast(0))

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Anggota: ${anggota.name}")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                viewModel.updateMember(
                    anggota.id,
                    etNama.text.toString(),
                    etPhone.text.toString(),
                    spinnerStatus.selectedItem.toString()
                )
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun formatCurrency(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(value)
    }
}