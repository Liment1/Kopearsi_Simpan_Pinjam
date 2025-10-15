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
import com.example.project_map.data.UserDatabase // <-- Tambahkan import ini
import com.example.project_map.data.UserData

class LoginFragment : Fragment() {

    // ▼▼▼ BAGIAN INI DIHAPUS KARENA SUDAH PINDAH KE UserDatabase.kt ▼▼▼
    /*
    private val allUsers = listOf(
        UserData("0825012", "admin@gmail.com", "admin", "Administrator Utama", "081234567890", true),
        UserData("1025045", "user@gmail.com", "user", "Budi Santoso", "087654321098"),
        UserData("1125077", "siti@gmail.com", "siti123", "Siti Aminah", "089988776655")
    )
    */
    // ▲▲▲ -------------------------------------------------------- ▲▲▲

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

        // Mengisi otomatis untuk kemudahan testing
        etEmail.setText("admin@gmail.com")
        etPassword.setText("admin")

        btnLogin.setOnClickListener {
            val inputEmailOrCode = etEmail.text.toString().trim()
            val inputPassword = etPassword.text.toString().trim()

            // ▼▼▼ DIUBAH AGAR MENGAMBIL DARI UserDatabase.allUsers ▼▼▼
            val userFromList = UserDatabase.allUsers.firstOrNull {
                it.id == inputEmailOrCode || it.email == inputEmailOrCode
            }
            // ▲▲▲ ----------------------------------------------- ▲▲▲

            if (userFromList != null) {
                // ... (sisa logika untuk mengecek password dan navigasi tetap sama)
                val credentialsPrefs = requireActivity().getSharedPreferences("UserCredentials", Context.MODE_PRIVATE)
                val correctPassword = credentialsPrefs.getString(userFromList.id, userFromList.pass)

                if (inputPassword == correctPassword) {
                    val sessionPrefs = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                    val editor = sessionPrefs.edit()
                    editor.putString("USER_NAME", userFromList.name)
                    editor.putString("USER_EMAIL", userFromList.email)
                    editor.putString("USER_PHONE", userFromList.phone)
                    editor.putString("USER_ID", userFromList.id)
                    editor.putBoolean("IS_ADMIN", userFromList.isAdmin)
                    editor.putString("USER_STATUS", userFromList.status) // <-- TAMBAHKAN BARIS INI
                    editor.apply()

                    if (userFromList.isAdmin) {
                        Toast.makeText(requireContext(), "Login sebagai Admin", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_adminFragment)
                    } else {
                        Toast.makeText(requireContext(), "Login sebagai User", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                } else {
                    Toast.makeText(requireContext(), "Kode Pegawai/Email atau Password salah", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Kode Pegawai/Email atau Password salah", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}