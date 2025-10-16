    package com.example.project_map.ui.loans

    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.LinearLayout
    import android.widget.TextView
    import androidx.fragment.app.Fragment
    import com.example.project_map.R
    import java.text.NumberFormat
    import java.util.Locale
    import androidx.navigation.fragment.findNavController


    class LoanHistoryFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            // Inflate layout utama untuk fragment ini
            return inflater.inflate(R.layout.fragment_blank_container, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Set up tombol back untuk navigasi
            val btnBack = view.findViewById<View>(R.id.btnBack)
            btnBack.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

            val container = view.findViewById<LinearLayout>(R.id.containerLoanHistory)
            val loans = LoanStorage.getAllLoans(requireContext())
            val layoutInflater = LayoutInflater.from(requireContext())  // Ganti nama variabel untuk menghindari konflik potensial
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))  // Formatter untuk mata uang Indonesia

            container.removeAllViews()  // Hapus semua view yang ada di container

            for (loan in loans) {
                val itemView = layoutInflater.inflate(R.layout.item_loan_history, container, false)

                val statusView = itemView.findViewById<TextView>(R.id.tvStatus)
                val infoView = itemView.findViewById<TextView>(R.id.tvInfo)
                val detailView = itemView.findViewById<TextView>(R.id.tvDetail)

                val status = loan.optString("status", "-")
                val nominal = loan.optDouble("nominal", 0.0)
                val tenor = loan.optString("tenor", "-")
                val tujuan = loan.optString("tujuan", "-")
                val bunga = loan.optDouble("bunga", 0.0)
                val sisaAngsuran = loan.optDouble("sisaAngsuran", 0.0)

                statusView.text = status
                infoView.text = "${currencyFormatter.format(nominal)} - $tenor"
                detailView.text = "Tujuan: $tujuan\nBunga: ${(bunga * 100)}%\nSisa Angsuran: ${currencyFormatter.format(sisaAngsuran)}"
                // Tambahkan alasan penolakan jika status == "Ditolak"
                val alasanPenolakan = loan.optString("alasanPenolakan", "")
                if (status.equals("Ditolak", ignoreCase = true) && alasanPenolakan.isNotEmpty()) {
                    detailView.text = detailView.text.toString() + "\n❌ Alasan Penolakan: $alasanPenolakan"
                }


                // 🔹 Ubah warna status
                statusView.setTextColor(
                    when (status.lowercase()) {
                        "lunas" -> resources.getColor(android.R.color.holo_green_dark)
                        "disetujui" -> resources.getColor(android.R.color.holo_blue_dark)
                        "ditolak" -> resources.getColor(android.R.color.holo_red_dark)
                        "proses" -> resources.getColor(android.R.color.holo_orange_dark)
                        else -> resources.getColor(android.R.color.black)
                    }
                )

                // Set click listener to navigate to the detail fragment with arguments
                itemView.setOnClickListener {
                    val bundle = Bundle().apply {
                        putString("loanData", loan.toString())
                    }
                    findNavController().navigate(R.id.action_loanHistoryFragment_to_loanDetailFragment, bundle)
                }

                container.addView(itemView)
            }
        }
    }
