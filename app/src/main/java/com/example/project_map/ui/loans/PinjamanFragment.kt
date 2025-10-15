package com.example.project_map.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R

class PinjamanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pinjaman, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAjukanPinjaman = view.findViewById<Button>(R.id.btnAjukanPinjaman)
        // Find the new button by its ID
        val btnRiwayatPembayaran = view.findViewById<Button>(R.id.btnRiwayatPembayaran)

        btnAjukanPinjaman.setOnClickListener {
            findNavController().navigate(R.id.action_pinjamanFragment_to_loansFragment)
        }

        // Set the click listener for the new button
        btnRiwayatPembayaran.setOnClickListener {
            // Use an action to navigate. You will create this in the next step.
            findNavController().navigate(R.id.action_pinjamanFragment_to_paymentHistoryFragment)
        }
    }
}