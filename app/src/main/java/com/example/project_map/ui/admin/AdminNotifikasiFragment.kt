package com.example.project_map.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project_map.R
import com.example.project_map.data.UserDatabase
import com.google.android.material.textfield.TextInputEditText

class AdminNotifikasiFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_notifikasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userDropdown = view.findViewById<AutoCompleteTextView>(R.id.acNamaAnggota)
        val templateDropdown = view.findViewById<AutoCompleteTextView>(R.id.acTemplatePesan)
        val customMessageInput = view.findViewById<TextInputEditText>(R.id.etPesanKustom)
        val btnKirim = view.findViewById<Button>(R.id.btnKirimNotifikasi)

        // Populate user dropdown
        val users = UserDatabase.allUsers.map { it.name }
        val userAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, users)
        userDropdown.setAdapter(userAdapter)

        // Populate message template dropdown
        val templates = listOf(
            "Cicilan Anda akan jatuh tempo dalam 3 hari.",
            "Pengajuan pinjaman Anda telah disetujui.",
            "Mohon maaf, pengajuan pinjaman Anda ditolak.",
            "Akan diadakan rapat anggota pada hari Sabtu."
        )
        val templateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, templates)
        templateDropdown.setAdapter(templateAdapter)

        // Set text in custom message box when a template is chosen
        templateDropdown.setOnItemClickListener { _, _, position, _ ->
            customMessageInput.setText(templates[position])
        }

        btnKirim.setOnClickListener {
            val selectedUser = userDropdown.text.toString()
            val message = customMessageInput.text.toString()

            if (selectedUser.isBlank() || message.isBlank()) {
                Toast.makeText(requireContext(), "Harap pilih anggota dan isi pesan", Toast.LENGTH_SHORT).show()
            } else {
                // In a real app, this would trigger a push notification
                // For this prototype, we show a Toast
                Log.d("Notification", "To: $selectedUser | Message: $message")
                Toast.makeText(requireContext(), "Notifikasi terkirim ke $selectedUser", Toast.LENGTH_LONG).show()

                // Clear fields after sending
                userDropdown.setText("")
                templateDropdown.setText("")
                customMessageInput.setText("")
            }
        }
    }
}
