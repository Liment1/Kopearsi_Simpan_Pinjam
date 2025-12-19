package com.example.project_map.ui.admin.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.project_map.R
import com.google.android.material.textfield.TextInputEditText

class AdminPengaturanFragment : Fragment() {

    private val viewModel: AdminSettingsViewModel by viewModels()

    private lateinit var etBunga: TextInputEditText
    private lateinit var etDenda: TextInputEditText
    private lateinit var btnSimpan: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_pengaturan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etBunga = view.findViewById(R.id.etBunga)
        etDenda = view.findViewById(R.id.etDenda)
        btnSimpan = view.findViewById(R.id.btnSimpanPengaturan)

        // Load initial data
        viewModel.loadSettings()

        btnSimpan.setOnClickListener {
            val bunga = etBunga.text.toString()
            val denda = etDenda.text.toString()
            viewModel.saveSettings(bunga, denda)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.settingsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AdminSettingsViewModel.SettingsState.Loading -> {
                    btnSimpan.isEnabled = false
                    btnSimpan.text = "Memuat..."
                }
                is AdminSettingsViewModel.SettingsState.Loaded -> {
                    btnSimpan.isEnabled = true
                    btnSimpan.text = "Simpan Perubahan"
                    etBunga.hint = "Saat ini: ${state.interestPercent}%"
                    etDenda.hint = "Saat ini: ${state.finePercent}%"
                }
                is AdminSettingsViewModel.SettingsState.Saved -> {
                    btnSimpan.isEnabled = true
                    btnSimpan.text = "Simpan Perubahan"
                    Toast.makeText(context, "Pengaturan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    etBunga.text?.clear()
                    etDenda.text?.clear()
                }
                is AdminSettingsViewModel.SettingsState.Error -> {
                    btnSimpan.isEnabled = true
                    btnSimpan.text = "Simpan Perubahan"
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}