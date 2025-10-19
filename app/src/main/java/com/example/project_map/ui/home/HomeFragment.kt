package com.example.project_map.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.R
import com.example.project_map.databinding.FragmentHomeBinding
import com.example.project_map.ui.home.RecentActivity
import com.example.project_map.ui.home.RecentActivityAdapter
import com.example.project_map.ui.loans.LoanStorage
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        s: Bundle?
    ): View {
        _b = FragmentHomeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        // 1. Get user data from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("USER_NAME", "Anggota Koperasi")
        val userId = sharedPreferences.getString("USER_ID", "ANG-000-0000-000")

        // 2. Set the user's name in the welcome message
        b.tvWelcomeName.text = name
        b.tvAngNumber.text = userId


        // Make the "Pengajuan Peminjaman" button navigate to the loan screen
        b.btnPengajuanPenarikan.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_pinjamanFragment)
        }


        // --- DYNAMIC DATA AND DUMMY DATA ---
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0

        b.tvSaldo.text = formatter.format(10000000)
        b.tvSimpananWajib.text = "Simpanan Wajib ${formatter.format(100000)}"

        // Calculate loan data from storage for more realism
        val allLoans = LoanStorage.getAllLoans(requireContext())
        val activeLoans = allLoans.filter { it.optString("status").equals("Disetujui", true) }
        val paidLoans = allLoans.filter { it.optString("status").equals("Lunas", true) }
        val usedLoanAmount = activeLoans.sumOf { it.optDouble("nominal", 0.0) }

        b.tvLimitPinjamanValue.text = formatter.format(100000000)
        b.tvPinjamanTerpakaiValue.text = formatter.format(usedLoanAmount)
        b.tvPinjamanAktifValue.text = activeLoans.size.toString()
        b.tvPinjamanLunasValue.text = paidLoans.size.toString()

        // 3. Populate the recent activity list with more dummy data
        val recentActivities = listOf(
            RecentActivity("Setoran Simpanan Wajib", "15 Okt 2025", "+${formatter.format(100000)}"),
            RecentActivity("Pembayaran Angsuran", "10 Okt 2025", "-${formatter.format(550000)}"),
            RecentActivity("Penarikan Dana", "05 Okt 2025", "-${formatter.format(200000)}"),
            RecentActivity("Setoran Simpanan Sukarela", "01 Okt 2025", "+${formatter.format(500000)}")
        )
        b.recyclerRecent.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerRecent.adapter = RecentActivityAdapter(recentActivities)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}