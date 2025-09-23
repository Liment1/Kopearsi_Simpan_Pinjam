package com.example.project_map

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
            val savedEmail = sharedPreferences.getString("EMAIL", "")
            val savedPassword = sharedPreferences.getString("PASSWORD", "")

            if (email == savedEmail && password == savedPassword) {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("EMAIL", email)
                intent.putExtra("NAME", sharedPreferences.getString("NAME", ""))
                intent.putExtra("PHONE", sharedPreferences.getString("PHONE", ""))
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }


        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
