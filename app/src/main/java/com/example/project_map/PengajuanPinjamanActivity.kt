package com.example.project_map

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.*

class PengajuanPinjamanActivity : AppCompatActivity() {

    private lateinit var edtNominal: TextInputEditText
    private lateinit var txtJasa: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtProvisi: TextView
    private lateinit var txtLain: TextView
    private lateinit var btnAjukan: Button   // ✅ tambahkan ini

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengajuan_pinjaman)

        // Binding
        edtNominal = findViewById(R.id.edtNominal)
        txtJasa = findViewById(R.id.txtJasa)
        txtTotal = findViewById(R.id.txtTotal)
        txtProvisi = findViewById(R.id.txtProvisi)
        txtLain = findViewById(R.id.txtLain)
        btnAjukan = findViewById(R.id.btnAjukan)  // ✅ inisialisasi tombol

        val spinnerJenis: Spinner = findViewById(R.id.spinnerJenis)
        val spinnerPeruntukan: Spinner = findViewById(R.id.spinnerPeruntukan)
        val spinnerTenor: Spinner = findViewById(R.id.spinnerTenor)

        // Isi spinner dengan string-array dari strings.xml
        spinnerJenis.adapter = ArrayAdapter.createFromResource(
            this, R.array.jenis_pinjaman, android.R.layout.simple_spinner_dropdown_item
        )
        spinnerPeruntukan.adapter = ArrayAdapter.createFromResource(
            this, R.array.peruntukan_pinjaman, android.R.layout.simple_spinner_dropdown_item
        )
        spinnerTenor.adapter = ArrayAdapter.createFromResource(
            this, R.array.tenor_pinjaman, android.R.layout.simple_spinner_dropdown_item
        )

        // Update ringkasan saat user mengetik
        edtNominal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hitungRingkasan()
            }
        })

        btnAjukan.setOnClickListener {
            Toast.makeText(this, getString(R.string.pengajuan_dikirim), Toast.LENGTH_SHORT).show()

            // Pindah ke MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun hitungRingkasan() {
        val nominalText = edtNominal.text.toString()
        if (nominalText.isNotEmpty()) {
            val nominal = nominalText.toLong()
            val jasa = nominal * 10 / 100  // contoh: 10%
            val provisi = 0L
            val biayaLain = 0L
            val total = nominal - jasa - provisi - biayaLain

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            txtJasa.text = getString(R.string.jasa_pinjaman, formatter.format(jasa))
            txtProvisi.text = getString(R.string.provisi_kredit, formatter.format(provisi))
            txtLain.text = getString(R.string.biaya_lain, formatter.format(biayaLain))
            txtTotal.text = getString(R.string.total_dana_diterima, formatter.format(total))
        } else {
            txtJasa.text = getString(R.string.jasa_pinjaman, "Rp 0")
            txtProvisi.text = getString(R.string.provisi_kredit, "Rp 0")
            txtLain.text = getString(R.string.biaya_lain, "Rp 0")
            txtTotal.text = getString(R.string.total_dana_diterima, "Rp 0")
        }
    }
}
