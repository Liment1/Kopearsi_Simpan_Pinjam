package com.example.project_map.ui.profile.syarat

import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.project_map.R
import com.google.android.material.appbar.MaterialToolbar

class SyaratKetentuanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_syarat_ketentuan)

        // Setup Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Inisialisasi TextView
        val tvContent = findViewById<TextView>(R.id.tvSyaratKetentuanContent)

        // Mengisi TextView dengan teks S&K
        tvContent.text = getSyaratKetentuanText()
    }

    // Fungsi untuk menyediakan teks Syarat dan Ketentuan
    private fun getSyaratKetentuanText(): CharSequence {
        // Kita gunakan HTML sederhana untuk formatting (bold, paragraf)
        val text = """
            <b>Pembaruan Terakhir: 15 Oktober 2025</b><br><br>
            
            Selamat datang di aplikasi Koperasi Digital kami. Dengan mendaftar dan menggunakan layanan kami, Anda setuju untuk terikat oleh Syarat dan Ketentuan ("S&K") ini. Mohon baca seluruh dokumen ini dengan saksama.<br><br>
            
            <b>1. Definisi</b><br>
            - <b>"Aplikasi"</b> merujuk pada platform perangkat lunak Koperasi Digital ini.<br>
            - <b>"Pengguna"</b> atau <b>"Anda"</b> merujuk pada anggota koperasi yang terdaftar dan menggunakan Aplikasi.<br>
            - <b>"Layanan"</b> mencakup semua fitur yang disediakan oleh Aplikasi, termasuk simpanan, pinjaman, dan laporan keuangan.<br><br>
            
            <b>2. Keanggotaan dan Pendaftaran</b><br>
            2.1. Untuk menggunakan Layanan, Anda harus menjadi anggota sah dari koperasi kami dan melakukan pendaftaran akun melalui Aplikasi.<br>
            2.2. Anda bertanggung jawab untuk menjaga kerahasiaan informasi akun Anda, termasuk Kode Pegawai, email, dan password. Segala aktivitas yang terjadi di bawah akun Anda adalah tanggung jawab Anda sepenuhnya.<br>
            2.3. Anda setuju untuk memberikan informasi yang akurat, terkini, dan lengkap saat proses pendaftaran dan memperbaruinya jika ada perubahan.<br><br>
            
            <b>3. Layanan Simpanan</b><br>
            3.1. Aplikasi menyediakan fasilitas untuk melakukan Simpanan Wajib dan Simpanan Sukarela.<br>
            3.2. Semua transaksi simpanan akan tercatat secara digital dan dapat dilihat pada menu Laporan Bulanan.<br>
            3.3. Pihak koperasi akan menetapkan suku bunga atau bagi hasil sesuai dengan Anggaran Dasar/Anggaran Rumah Tangga (AD/ART) yang berlaku.<br><br>
            
            <b>4. Layanan Pinjaman dan Angsuran</b><br>
            4.1. Pengajuan pinjaman hanya dapat dilakukan oleh anggota aktif yang memenuhi kriteria kelayakan yang ditetapkan oleh koperasi.<br>
            4.2. Persetujuan dan jumlah pinjaman bersifat mutlak dan ditentukan oleh pengurus koperasi berdasarkan analisis risiko.<br>
            4.3. Jadwal dan jumlah angsuran akan diinformasikan secara jelas saat pinjaman disetujui. Keterlambatan pembayaran angsuran dapat dikenakan denda sesuai kebijakan yang berlaku.<br><br>
            
            <b>5. Privasi dan Keamanan Data</b><br>
            Kami berkomitmen untuk melindungi privasi Anda. Data pribadi yang Anda berikan akan dikelola sesuai dengan Kebijakan Privasi kami. Kami tidak akan membagikan data pribadi Anda kepada pihak ketiga tanpa persetujuan Anda, kecuali diwajibkan oleh hukum.<br><br>
            
            <b>6. Batasan Tanggung Jawab</b><br>
            Koperasi tidak bertanggung jawab atas kerugian tidak langsung yang mungkin timbul dari penggunaan atau ketidakmampuan menggunakan Aplikasi. Layanan disediakan "sebagaimana adanya" tanpa jaminan apa pun.<br><br>
            
            <b>7. Perubahan Syarat dan Ketentuan</b><br>
            Kami berhak untuk mengubah S&K ini dari waktu ke waktu. Setiap perubahan akan diinformasikan melalui notifikasi di Aplikasi atau email. Dengan terus menggunakan Layanan setelah perubahan, Anda dianggap menyetujui S&K yang baru.<br><br>
            
            Terima kasih telah menjadi bagian dari Koperasi Digital kami.
        """.trimIndent()

        // Menggunakan Html.fromHtml untuk memproses tag HTML
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
    }
}