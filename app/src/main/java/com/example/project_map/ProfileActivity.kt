package com.example.project_map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val name = intent.getStringExtra("NAME")
        val email = intent.getStringExtra("EMAIL")
        val phone = intent.getStringExtra("PHONE") // kalau mau dipakai nanti

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)

        // set data ke tampilan
        tvName.text = name
        tvEmail.text = email
        imgProfile.setImageResource(R.mipmap.ic_launcher) // default icon

        val tvGantiAkun = findViewById<TextView>(R.id.tvGantiAkun)

        tvGantiAkun.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // biar gak bisa balik ke Profile pakai tombol back
        }

    }
}
