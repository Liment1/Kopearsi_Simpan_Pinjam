package com.example.project_map.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project_map.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class AdminPengaturanFragment : Fragment() {

    private lateinit var etBunga: TextInputEditText
    private lateinit var etDenda: TextInputEditText
    private lateinit var btnSimpan: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_pengaturan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        etBunga = view.findViewById(R.id.etBunga)
        etDenda = view.findViewById(R.id.etDenda)
        btnSimpan = view.findViewById(R.id.btnSimpanPengaturan)

        loadCurrentSettings()

        btnSimpan.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadCurrentSettings() {
        // Fetch from 'app_config' -> 'global_settings'
        db.collection("app_config").document("global_settings").get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentBunga = document.getDouble("defaultInterest") ?: 0.0
                    val currentDenda = document.getDouble("lateFinePercentage") ?: 0.0

                    // Show current values in HINT as requested
                    etBunga.hint = "Saat ini: ${(currentBunga * 100).toInt()}%"
                    etDenda.hint = "Saat ini: ${(currentDenda * 100).toInt()}%"
                } else {
                    // Seed defaults if document doesn't exist
                    etBunga.hint = "Saat ini: 0%"
                    etDenda.hint = "Saat ini: 0%"
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal memuat pengaturan", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveSettings() {
        val bungaInput = etBunga.text.toString()
        val dendaInput = etDenda.text.toString()

        if (bungaInput.isEmpty() && dendaInput.isEmpty()) {
            Toast.makeText(context, "Tidak ada perubahan", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = hashMapOf<String, Any>()

        // Convert percentage (e.g. 5) to decimal (0.05)
        if (bungaInput.isNotEmpty()) {
            val bungaDecimal = bungaInput.toDouble() / 100.0
            updates["defaultInterest"] = bungaDecimal
        }

        if (dendaInput.isNotEmpty()) {
            val dendaDecimal = dendaInput.toDouble() / 100.0
            updates["lateFinePercentage"] = dendaDecimal
        }

        db.collection("app_config").document("global_settings")
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Pengaturan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                etBunga.text?.clear()
                etDenda.text?.clear()
                loadCurrentSettings() // Refresh hints
            }
            .addOnFailureListener {
                // If document doesn't exist, use Set instead of Update
                db.collection("app_config").document("global_settings").set(updates)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Pengaturan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                        loadCurrentSettings()
                    }
            }
    }
}