package com.example.project_map.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.data.UserDatabase
import com.example.project_map.data.UserData

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val cbTerms = view.findViewById<CheckBox>(R.id.cbTerms)
        val tvLoginRedirect = view.findViewById<TextView>(R.id.tvLoginRedirect)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            // --- VALIDASI ---
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Format email tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (UserDatabase.allUsers.any { it.email == email }) {
                Toast.makeText(requireContext(), "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!cbTerms.isChecked) {
                Toast.makeText(requireContext(), "Anda harus menyetujui Syarat dan Ketentuan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- LOGIKA PENYIMPANAN BARU ---

            // 1. Buat ID baru yang berurutan (sama seperti di AdminFragment)
            val lastUser = UserDatabase.allUsers.lastOrNull()
            var lastNumber = 0
            if (lastUser != null) {
                lastNumber = lastUser.id.substring(3).toInt()
            }
            val newNumber = lastNumber + 1
            val newId = "AGT" + String.format("%04d", newNumber)

            // 2. Buat objek UserData baru dengan status default "Calon Anggota"
            val newUser = UserData(
                id = newId,
                name = name,
                phone = phone,
                email = email,
                pass = password,
                isAdmin = false,
                status = "Calon Anggota" // Status default untuk pendaftar baru
            )

            // 3. Tambahkan pengguna baru ke database terpusat
            UserDatabase.allUsers.add(newUser)

            // --- Logika lama yang menyimpan ke SharedPreferences dihapus ---

            Toast.makeText(requireContext(), "Registrasi berhasil!", Toast.LENGTH_SHORT).show()

            // Kembali ke halaman login
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        tvLoginRedirect.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
}