package com.example.project_map.ui.profile

import android.content.Context
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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.ui.profile.detail.DetailProfileActivity
import com.example.project_map.ui.profile.laporan.LaporanBulananActivity
import com.example.project_map.ui.profile.syarat.SyaratKetentuanActivity

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("USER_NAME", "Guest") ?: "Guest"
        val email = sharedPreferences.getString("USER_EMAIL", "-") ?: "-"
        val isAdmin = sharedPreferences.getBoolean("IS_ADMIN", false)
        val status = sharedPreferences.getString("USER_STATUS", "Tidak Aktif") ?: "Tidak Aktif"

        // Inisialisasi semua view
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val btnDetailProfile = view.findViewById<RelativeLayout>(R.id.btnDetailProfile)
        val btnLaporanBulanan = view.findViewById<RelativeLayout>(R.id.btnLaporanBulanan)
        val btnSyaratKetentuan = view.findViewById<RelativeLayout>(R.id.btnSyaratKetentuan)
        val btnKeluar = view.findViewById<Button>(R.id.btnKeluar)
        // ▼▼▼ Inisialisasi view baru untuk menu admin ▼▼▼
        val btnAdminMenu = view.findViewById<RelativeLayout>(R.id.btnAdminMenu)
        val btnLaporanKeuangan = view.findViewById<RelativeLayout>(R.id.btnLaporanKeuangan)
        val dividerAdmin = view.findViewById<View>(R.id.dividerAdmin)
        val dividerLaporan = view.findViewById<View>(R.id.dividerLaporan)


        // Set data yang sudah diambil ke tampilan
        tvName.text = name
        tvEmail.text = email
        tvStatus.text = status

        val statusBackground = tvStatus.background as GradientDrawable
        statusBackground.setColor(getStatusColor(status))

        // Sembunyikan menu-menu admin jika user bukan admin
        if (!isAdmin) {
            btnAdminMenu.visibility = View.GONE
            btnLaporanKeuangan.visibility = View.GONE
            dividerAdmin.visibility = View.GONE
            dividerLaporan.visibility = View.GONE
        }

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

        // ▼▼▼ Tambahkan listener untuk tombol-tombol admin ▼▼▼
        btnAdminMenu.setOnClickListener {
            // Mengarah ke halaman kelola anggota (nama fragment lama tidak masalah)
            findNavController().navigate(R.id.action_profileFragment_to_adminDataAnggotaFragment)
        }

        btnLaporanKeuangan.setOnClickListener {
            // Mengarah ke halaman laporan keuangan yang baru
            findNavController().navigate(R.id.action_profileFragment_to_adminLaporanKeuanganFragment)
        }

        btnKeluar.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
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