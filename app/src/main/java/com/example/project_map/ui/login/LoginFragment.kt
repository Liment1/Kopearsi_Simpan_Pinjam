package com.example.project_map.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.data.UserData

class LoginFragment : Fragment() {

    private val allUsers = listOf(
        UserData("0825012", "admin@gmail.com", "admin", "Administrator Utama", "081234567890", true),
        UserData("1025045", "user@gmail.com", "user", "Budi Santoso", "087654321098"),
        UserData("1125077", "siti@gmail.com", "siti123", "Siti Aminah", "089988776655")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegister = view.findViewById<TextView>(R.id.tvRegister)

        etEmail.setText("user@gmail.com")
        etPassword.setText("user")

        btnLogin.setOnClickListener {
            val inputEmailOrCode = etEmail.text.toString().trim()
            val inputPassword = etPassword.text.toString().trim()

            // Cari user berdasarkan id atau email di list hardcode
            val userFromList = allUsers.firstOrNull {
                it.id == inputEmailOrCode || it.email == inputEmailOrCode
            }

            if (userFromList != null) {
                // ▼▼▼ LOGIKA BARU: CEK PASSWORD TERUPDATE ▼▼▼
                // 1. Ambil SharedPreferences tempat password baru disimpan
                val credentialsPrefs = requireActivity().getSharedPreferences("UserCredentials", Context.MODE_PRIVATE)

                // 2. Cek apakah ada password baru untuk user ini?
                //    Gunakan password dari list hardcode sebagai default jika tidak ada.
                val correctPassword = credentialsPrefs.getString(userFromList.id, userFromList.pass)

                // 3. Bandingkan input dengan password yang benar (yang mungkin sudah baru)
                if (inputPassword == correctPassword) {
                    // Login berhasil, simpan data sesi seperti biasa
                    val sessionPrefs = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                    val editor = sessionPrefs.edit()
                    editor.putString("USER_NAME", userFromList.name)
                    editor.putString("USER_EMAIL", userFromList.email)
                    editor.putString("USER_PHONE", userFromList.phone)
                    editor.putString("USER_ID", userFromList.id)
                    editor.putBoolean("IS_ADMIN", userFromList.isAdmin)
                    editor.apply()

                    if (userFromList.isAdmin) {
                        Toast.makeText(requireContext(), "Login sebagai Admin", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_adminFragment)
                    } else {
                        Toast.makeText(requireContext(), "Login sebagai User", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                } else {
                    // Password salah
                    Toast.makeText(requireContext(), "Kode Pegawai/Email atau Password salah", Toast.LENGTH_SHORT).show()
                }
            } else {
                // User tidak ditemukan
                Toast.makeText(requireContext(), "Kode Pegawai/Email atau Password salah", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}