package com.example.project_map.ui.profile

import android.content.Context
import android.content.Intent
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

        // ▼▼▼ AMBIL DATA DARI SHaredPreferences ▼▼▼
        val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        // Jika tidak ada data, gunakan nilai default "Guest" atau "-"
        val name = sharedPreferences.getString("USER_NAME", "Guest")
        val email = sharedPreferences.getString("USER_EMAIL", "-")
        val isAdmin = sharedPreferences.getBoolean("IS_ADMIN", false)
        // ▲▲▲ ---------------------------------- ▲▲▲

        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val btnDetailProfile = view.findViewById<RelativeLayout>(R.id.btnDetailProfile)
        val btnLaporanBulanan = view.findViewById<RelativeLayout>(R.id.btnLaporanBulanan)
        val btnSyaratKetentuan = view.findViewById<RelativeLayout>(R.id.btnSyaratKetentuan)
        val btnAdminMenu = view.findViewById<RelativeLayout>(R.id.btnAdminMenu)
        val btnKeluar = view.findViewById<Button>(R.id.btnKeluar)

        // Set data yang sudah diambil ke tampilan
        tvName.text = name
        tvEmail.text = email

        // Sembunyikan menu admin jika user bukan admin
        if (!isAdmin) {
            btnAdminMenu.visibility = View.GONE
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

        btnAdminMenu.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_adminFragment)
        }

        btnKeluar.setOnClickListener {
            // ▼▼▼ KOSONGKAN SharedPreferences SAAT LOGOUT ▼▼▼
            val editor = sharedPreferences.edit()
            editor.clear() // Hapus semua data (nama, email, dll)
            editor.apply()

            // Arahkan kembali ke halaman login
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }
}