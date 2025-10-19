package com.example.project_map.ui.profile.detail

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_map.R
import com.example.project_map.data.HashingUtils
import com.example.project_map.data.UserDatabase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class DetailProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_profile)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("USER_ID", null)

        if (id == null) {
            Toast.makeText(this, "Sesi tidak valid, silakan login kembali.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val currentUser = UserDatabase.allUsers.find { it.id == id }
        if (currentUser == null) {
            Toast.makeText(this, "Data pengguna tidak ditemukan.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val tvNama = findViewById<TextView>(R.id.tvNama)
        val tvKodePegawai = findViewById<TextView>(R.id.tvKodePegawai)
        val tvEmailDetail = findViewById<TextView>(R.id.tvEmailDetail)
        val tvPhoneDetail = findViewById<TextView>(R.id.tvPhoneDetail)
        val etPasswordLama = findViewById<TextInputEditText>(R.id.etPasswordLama)
        val etPasswordBaru = findViewById<TextInputEditText>(R.id.etPasswordBaru)
        val etKonfirmasiPasswordBaru = findViewById<TextInputEditText>(R.id.etKonfirmasiPasswordBaru)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)

        tvNama.text = currentUser.name
        tvKodePegawai.text = currentUser.id
        tvEmailDetail.text = currentUser.email
        tvPhoneDetail.text = currentUser.phone

        // Pre-fill the old password field for the default user for easy testing
        if (currentUser.name == "Santi Sanjaya") {
            etPasswordLama.setText("User123_")
        }

        btnSimpan.setOnClickListener {
            val passLamaInput = etPasswordLama.text.toString()
            val passBaruInput = etPasswordBaru.text.toString()
            val konfirmasiPassBaruInput = etKonfirmasiPasswordBaru.text.toString()

            val hashedOldPasswordInput = HashingUtils.hashPassword(passLamaInput)

            when {
                passLamaInput.isBlank() || passBaruInput.isBlank() || konfirmasiPassBaruInput.isBlank() -> {
                    Toast.makeText(this, "Semua kolom password harus diisi!", Toast.LENGTH_SHORT).show()
                }
                hashedOldPasswordInput != currentUser.pass -> {
                    Toast.makeText(this, "Password lama salah!", Toast.LENGTH_SHORT).show()
                }
                passBaruInput != konfirmasiPassBaruInput -> {
                    Toast.makeText(this, "Password baru dan konfirmasi tidak cocok!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val hashedNewPassword = HashingUtils.hashPassword(passBaruInput)
                    currentUser.pass = hashedNewPassword

                    Toast.makeText(this, "Password berhasil diubah!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
