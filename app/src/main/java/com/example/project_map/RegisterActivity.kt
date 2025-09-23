package com.example.project_map

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // [BARU] Deklarasi view baru
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val tvLoginRedirect = findViewById<TextView>(R.id.tvLoginRedirect)

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val phone = etPhone.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // [BARU] Validasi checkbox
            if (!cbTerms.isChecked) {
                Toast.makeText(this, "You must accept the Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan data ke SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("NAME", name)
            editor.putString("PHONE", phone)
            editor.putString("EMAIL", email)
            editor.putString("PASSWORD", password)
            editor.apply()

            Toast.makeText(this, "Register successful!", Toast.LENGTH_SHORT).show()

            // Kembali ke Login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // [BARU] Listener untuk teks "Kembali ke Login"
        tvLoginRedirect.setOnClickListener {
            // Sama seperti di atas, kembali ke Login Activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Tutup halaman register
        }
    }
}