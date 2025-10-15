// PaymentHistoryFragment.kt
package com.example.project_map.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R

class PaymentHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler: RecyclerView = view.findViewById(R.id.recyclerPaymentHistory)

        // Dummy data for the list
        val paymentHistoryList = listOf(
            PaymentHistory("15 Sep 2025", "Rp 550.000", "Angsuran ke-1 (Lunas)"),
            PaymentHistory("15 Agu 2025", "Rp 550.000", "Angsuran ke-2 (Lunas)"),
            PaymentHistory("15 Jul 2025", "Rp 550.000", "Angsuran ke-3 (Lunas)"),
            PaymentHistory("15 Jun 2025", "Rp 550.000", "Angsuran ke-4 (Lunas)")
        )

        val adapter = PaymentHistoryAdapter(paymentHistoryList)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
    }
}