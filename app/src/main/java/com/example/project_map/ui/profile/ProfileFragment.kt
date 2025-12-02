package com.example.project_map.ui.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.ui.profile.detail.DetailProfileActivity
import com.example.project_map.ui.profile.laporan.LaporanBulananActivity
import com.example.project_map.ui.profile.syarat.SyaratKetentuanActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize Views
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val btnDetailProfile = view.findViewById<RelativeLayout>(R.id.btnDetailProfile)
        val btnLaporanBulanan = view.findViewById<RelativeLayout>(R.id.btnLaporanBulanan)
        val btnSyaratKetentuan = view.findViewById<RelativeLayout>(R.id.btnSyaratKetentuan)
        val btnKeluar = view.findViewById<Button>(R.id.btnKeluar)

        // Admin Menus
        val btnAdminMenu = view.findViewById<RelativeLayout>(R.id.btnAdminMenu)
        val btnLaporanKeuangan = view.findViewById<RelativeLayout>(R.id.btnLaporanKeuangan)
        val dividerAdmin = view.findViewById<View>(R.id.dividerAdmin)
        val dividerLaporan = view.findViewById<View>(R.id.dividerLaporan)

        // Default visibility (Hidden until data loads)
        btnAdminMenu.visibility = View.GONE
        btnLaporanKeuangan.visibility = View.GONE
        dividerAdmin.visibility = View.GONE
        dividerLaporan.visibility = View.GONE

        // --- FETCH USER DATA ---
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Set email directly from Auth (it's faster)
            tvEmail.text = currentUser.email

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "No Name"
                        val status = document.getString("status") ?: "Tidak Aktif"
                        val isAdmin = document.getBoolean("admin") ?: false

                        tvName.text = name
                        tvStatus.text = status

                        // Set Status Color
                        val statusBackground = tvStatus.background as GradientDrawable
                        statusBackground.setColor(getStatusColor(status))

                        // Show/Hide Admin Menus based on Firestore data
                        if (isAdmin) {
                            btnAdminMenu.visibility = View.VISIBLE
                            btnLaporanKeuangan.visibility = View.VISIBLE
                            dividerAdmin.visibility = View.VISIBLE
                            dividerLaporan.visibility = View.VISIBLE
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                }
        }

        // --- NAVIGATION ---

        btnDetailProfile.setOnClickListener {
            val intent = Intent(requireContext(), DetailProfileActivity::class.java)
            startActivity(intent)
        }

        btnLaporanBulanan.setOnClickListener {
            val intent = Intent(requireContext(), LaporanBulananActivity::class.java)
            startActivity(intent)
        }

        btnSyaratKetentuan.setOnClickListener {
            val intent = Intent(requireContext(), SyaratKetentuanActivity::class.java)
            startActivity(intent)
        }

        btnKeluar.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    private fun getStatusColor(status: String): Int {
        return when (status) {
            "Anggota Aktif" -> Color.parseColor("#1E8E3E") // Hijau
            "Calon Anggota" -> Color.parseColor("#F9AB00") // Kuning
            "Anggota Tidak Aktif" -> Color.parseColor("#5F6368") // Abu-abu
            "Diblokir Sementara" -> Color.parseColor("#E67C73") // Oranye
            "Dikeluarkan" -> Color.parseColor("#D93025") // Merah
            else -> Color.LTGRAY
        }
    }
}