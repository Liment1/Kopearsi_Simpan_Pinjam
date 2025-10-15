package com.example.project_map.ui.admin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.UserDatabase
import com.example.project_map.data.UserData
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import java.util.concurrent.ThreadLocalRandom

class AdminDataAnggotaFragment : Fragment() {

    private lateinit var rvAnggota: RecyclerView
    private lateinit var fabTambahAnggota: FloatingActionButton
    private lateinit var anggotaAdapter: AnggotaAdapter
    private lateinit var toolbar: MaterialToolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_data_anggota, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.toolbarAdmin)
        rvAnggota = view.findViewById(R.id.rvAnggota)
        fabTambahAnggota = view.findViewById(R.id.fabTambahAnggota)

        // Setup Toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        setupRecyclerView()

        fabTambahAnggota.setOnClickListener {
            showAnggotaDialog(null)
        }
    }

    private fun setupRecyclerView() {
        anggotaAdapter = AnggotaAdapter(UserDatabase.allUsers) { anggota ->
            showAnggotaDialog(anggota)
        }
        rvAnggota.layoutManager = LinearLayoutManager(requireContext())
        rvAnggota.adapter = anggotaAdapter
    }

    private fun showAnggotaDialog(anggota: UserData?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_form_anggota, null)
        // Inisialisasi semua view dari dialog
        val etNama = dialogView.findViewById<EditText>(R.id.etNamaForm)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmailForm)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhoneForm)
        val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerStatus)
        val layoutPasswordLama = dialogView.findViewById<TextInputLayout>(R.id.layoutPasswordLama)
        val etPasswordLama = dialogView.findViewById<EditText>(R.id.etPasswordLamaForm)
        val etPasswordBaru = dialogView.findViewById<EditText>(R.id.etPasswordBaruForm)
        val etKonfirmasiPasswordBaru = dialogView.findViewById<EditText>(R.id.etKonfirmasiPasswordBaruForm)


        val statusAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.status_array, android.R.layout.simple_spinner_item
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        val dialogTitle = if (anggota == null) "Tambah Anggota Baru" else "Edit Data Anggota"

        anggota?.let {
            etNama.setText(it.name)
            etEmail.setText(it.email)
            etPhone.setText(it.phone)
            spinnerStatus.setSelection(statusAdapter.getPosition(it.status))
            // Tampilkan kolom password lama HANYA saat mode edit
            layoutPasswordLama.visibility = View.VISIBLE
        }

        AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, _ ->
                val nama = etNama.text.toString()
                val email = etEmail.text.toString()
                val phone = etPhone.text.toString()
                val status = spinnerStatus.selectedItem.toString()
                val passBaru = etPasswordBaru.text.toString()
                val konfirmasiPassBaru = etKonfirmasiPasswordBaru.text.toString()
                val passLama = etPasswordLama.text.toString()

                if (nama.isBlank() || email.isBlank()) {
                    Toast.makeText(requireContext(), "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Logika untuk Mode TAMBAH
                if (anggota == null) {
                    if (passBaru.isBlank() || konfirmasiPassBaru.isBlank()) {
                        Toast.makeText(requireContext(), "Password baru tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (passBaru != konfirmasiPassBaru) {
                        Toast.makeText(requireContext(), "Password baru dan konfirmasi tidak cocok", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val newId = "AGT" + String.format("%04d", (UserDatabase.allUsers.size + 1))
                    val newAnggota = UserData(newId, email, passBaru, nama, phone, false, status)
                    UserDatabase.allUsers.add(newAnggota)
                    anggotaAdapter.notifyItemInserted(UserDatabase.allUsers.size - 1)
                    Toast.makeText(requireContext(), "Anggota baru berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
                // Logika untuk Mode EDIT
                else {
                    anggota.name = nama
                    anggota.email = email
                    anggota.phone = phone
                    anggota.status = status

                    // Jika admin ingin mengubah password (salah satu kolom password diisi)
                    if (passBaru.isNotBlank() || konfirmasiPassBaru.isNotBlank() || passLama.isNotBlank()) {
                        val credentialsPrefs = requireContext().getSharedPreferences("UserCredentials", Context.MODE_PRIVATE)
                        val correctOldPassword = credentialsPrefs.getString(anggota.id, anggota.pass)

                        if (passLama != correctOldPassword) {
                            Toast.makeText(requireContext(), "Password lama salah!", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        if (passBaru != konfirmasiPassBaru) {
                            Toast.makeText(requireContext(), "Password baru dan konfirmasi tidak cocok", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        // Update password di database dan SharedPreferences
                        anggota.pass = passBaru
                        credentialsPrefs.edit().putString(anggota.id, passBaru).apply()
                    }
                    anggotaAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}