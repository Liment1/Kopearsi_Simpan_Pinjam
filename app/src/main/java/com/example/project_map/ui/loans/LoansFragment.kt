package com.example.project_map.ui.loans

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.databinding.FragmentLoansBinding
import java.text.NumberFormat
import java.util.Locale

class LoansFragment : Fragment() {

    private var _b: FragmentLoansBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentLoansBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Spinners
        b.spinnerJenis.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.jenis_pinjaman, android.R.layout.simple_spinner_dropdown_item
        )
        b.spinnerPeruntukan.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.peruntukan_pinjaman, android.R.layout.simple_spinner_dropdown_item
        )
        b.spinnerTenor.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.tenor_pinjaman, android.R.layout.simple_spinner_dropdown_item
        )

        // Update ringkasan saat user mengetik
        b.edtNominal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { hitungRingkasan() }
        })

        b.btnAjukan.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.pengajuan_dikirim), Toast.LENGTH_SHORT).show()
            // Kembali ke layar sebelumnya (Pinjaman)
            findNavController().navigateUp()
            // Atau langsung ke destination tertentu:
            // findNavController().navigate(R.id.loansFragment)
        }
    }

    private fun hitungRingkasan() {
        val nominalText = b.edtNominal.text?.toString()?.trim().orEmpty()
        if (nominalText.isNotEmpty()) {
            // Aman dari koma/titik: ambil hanya digit
            val digitsOnly = nominalText.filter { it.isDigit() }
            val nominal = digitsOnly.toLongOrNull() ?: 0L

            val jasa = nominal * 10 / 100   // contoh: 10%
            val provisi = 0L
            val biayaLain = 0L
            val total = nominal - jasa - provisi - biayaLain

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            b.txtJasa.text   = getString(R.string.jasa_pinjaman, formatter.format(jasa))
            b.txtProvisi.text= getString(R.string.provisi_kredit, formatter.format(provisi))
            b.txtLain.text   = getString(R.string.biaya_lain, formatter.format(biayaLain))
            b.txtTotal.text  = getString(R.string.total_dana_diterima, formatter.format(total))
        } else {
            b.txtJasa.text   = getString(R.string.jasa_pinjaman, "Rp 0")
            b.txtProvisi.text= getString(R.string.provisi_kredit, "Rp 0")
            b.txtLain.text   = getString(R.string.biaya_lain, "Rp 0")
            b.txtTotal.text  = getString(R.string.total_dana_diterima, "Rp 0")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
