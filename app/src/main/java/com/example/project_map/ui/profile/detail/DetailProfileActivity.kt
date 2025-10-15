package com.example.project_map.ui.profile.detail

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_map.R
import com.example.project_map.data.UserDatabase // <-- Pastikan import ini ada
import com.example.project_map.data.UserData
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class DetailProfileActivity : AppCompatActivity() {

    // ▼▼▼ BAGIAN INI DIHAPUS KARENA SUDAH PINDAH KE UserDatabase.kt ▼▼▼
    /*
    private val allUsers = listOf(
        UserData("0825012", "admin@gmail.com", "admin", "Administrator Utama", "081234567890", true),
        UserData("1025045", "user@gmail.com", "user", "Budi Santoso", "087654321098"),
        UserData("1125077", "siti@gmail.com", "siti123", "Siti Aminah", "089988776655")
    )
    */
    // ▲▲▲ -------------------------------------------------------- ▲▲▲

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_profile)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("USER_ID", "-")
        val name = sharedPreferences.getString("USER_NAME", "-")
        val email = sharedPreferences.getString("USER_EMAIL", "-")
        val phone = sharedPreferences.getString("USER_PHONE", "-")

        val tvNama = findViewById<TextView>(R.id.tvNama)
        val tvKodePegawai = findViewById<TextView>(R.id.tvKodePegawai)
        val tvEmailDetail = findViewById<TextView>(R.id.tvEmailDetail)
        val tvPhoneDetail = findViewById<TextView>(R.id.tvPhoneDetail)
        val etPasswordLama = findViewById<TextInputEditText>(R.id.etPasswordLama)
        val etPasswordBaru = findViewById<TextInputEditText>(R.id.etPasswordBaru)
        val etKonfirmasiPasswordBaru = findViewById<TextInputEditText>(R.id.etKonfirmasiPasswordBaru)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)

        tvNama.text = name
        tvKodePegawai.text = id
        tvEmailDetail.text = email
        tvPhoneDetail.text = phone

        btnSimpan.setOnClickListener {
            val passBaru = etPasswordBaru.text.toString()
            val konfirmasiPassBaru = etKonfirmasiPasswordBaru.text.toString()
            val passLama = etPasswordLama.text.toString()

            // ▼▼▼ DIUBAH AGAR MENGAMBIL DARI UserDatabase.allUsers ▼▼▼
            val userFromList = UserDatabase.allUsers.firstOrNull { it.id == id }
            // ▲▲▲ ----------------------------------------------- ▲▲▲

            val credentialsPrefs = getSharedPreferences("UserCredentials", Context.MODE_PRIVATE)
            val correctOldPassword = credentialsPrefs.getString(userFromList?.id, userFromList?.pass)

            when {
                passBaru.isBlank() || konfirmasiPassBaru.isBlank() || passLama.isBlank() -> {
                    Toast.makeText(this, "Semua kolom password harus diisi!", Toast.LENGTH_SHORT).show()
                }
                passBaru != konfirmasiPassBaru -> {
                    Toast.makeText(this, "Password baru dan konfirmasi tidak cocok!", Toast.LENGTH_SHORT).show()
                }
                passLama != correctOldPassword -> {
                    Toast.makeText(this, "Password lama salah!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val editor = credentialsPrefs.edit()
                    editor.putString(id, passBaru)
                    editor.apply()

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