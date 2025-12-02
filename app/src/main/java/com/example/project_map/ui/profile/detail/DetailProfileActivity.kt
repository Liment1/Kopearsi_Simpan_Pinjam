package com.example.project_map.ui.profile.detail

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_map.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_profile)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser

        // Safety Check
        if (currentUser == null) {
            Toast.makeText(this, "Sesi habis, silakan login kembali.", Toast.LENGTH_SHORT).show()
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

        // --- FETCH DATA FROM FIRESTORE ---
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    tvNama.text = document.getString("name")
                    // Note: We use 'memberCode' (or whatever you named the field for AGT...)
                    // If you kept it as 'id' in the DB, change this string to "id"
                    tvKodePegawai.text = document.getString("memberCode") ?: document.getString("id") ?: "-"
                    tvEmailDetail.text = document.getString("email")
                    tvPhoneDetail.text = document.getString("phone")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data user.", Toast.LENGTH_SHORT).show()
            }

        // --- PASSWORD CHANGE LOGIC ---
        btnSimpan.setOnClickListener {
            val passLama = etPasswordLama.text.toString()
            val passBaru = etPasswordBaru.text.toString()
            val konfirmasiPass = etKonfirmasiPasswordBaru.text.toString()

            // 1. Basic Validation
            if (passLama.isEmpty() || passBaru.isEmpty() || konfirmasiPass.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passBaru.length < 6) {
                Toast.makeText(this, "Password baru minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passBaru != konfirmasiPass) {
                Toast.makeText(this, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Re-Authenticate User (Required by Firebase for sensitive changes)
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, passLama)

            currentUser.reauthenticate(credential)
                .addOnSuccessListener {
                    // 3. Update Password in Firebase Auth
                    currentUser.updatePassword(passBaru)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Password berhasil diubah!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal update password: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    // This usually means the 'Old Password' was wrong
                    Toast.makeText(this, "Password lama salah", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}