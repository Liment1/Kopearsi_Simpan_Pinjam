package com.example.project_map.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project_map.R

class LoanApplicationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // This fragment should inflate its own layout, NOT the pinjaman one
        return inflater.inflate(R.layout.fragment_loan_application, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the button from this fragment's layout
        val btnSubmitApplication = view.findViewById<Button>(R.id.btnAjukan) // Make sure this ID is correct in your XML

        btnSubmitApplication.setOnClickListener {
            // Your logic for submitting the loan application goes here
            Toast.makeText(requireContext(), "Pengajuan pinjaman berhasil!", Toast.LENGTH_SHORT).show()
        }
    }
}