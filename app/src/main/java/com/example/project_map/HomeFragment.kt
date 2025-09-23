package com.example.project_map

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.project_map.databinding.FragmentHomeBinding
import com.example.project_map.ui.home.RecentActivity
import com.example.project_map.ui.home.RecentActivityAdapter

class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        b.tvWelcomeName.text = "Muhammad Abdullah Said"
        b.tvSaldo.text = "Rp 15.450.000"
        b.tvSimpananWajib.text = "Simpanan Wajib Rp 50.000"
        b.tvLimitPinjamanValue.text = "Rp 100.000.000"
        b.tvPinjamanTerpakaiValue.text = "Rp 5.000.000"
        b.tvPinjamanAktifValue.text = "3"
        b.tvPinjamanLunasValue.text = "3"
        b.recyclerRecent.adapter = RecentActivityAdapter(
            listOf(
                RecentActivity("Setoran Simpanan", "14 Mar 2025", "+Rp 300.000"),
                RecentActivity("Penarikan Simpanan", "12 Mar 2025", "-Rp 300.000")
            )
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}