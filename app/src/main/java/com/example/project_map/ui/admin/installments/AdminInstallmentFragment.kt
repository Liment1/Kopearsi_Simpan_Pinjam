package com.example.project_map.ui.admin.installments

import android.app.Dialog
// import android.content.Intent // Not needed anymore
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController // <--- 1. ADD THIS IMPORT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project_map.R
import com.google.android.material.snackbar.Snackbar

class AdminInstallmentFragment : Fragment() {

    private val viewModel: AdminInstallmentViewModel by viewModels()
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AdminInstallmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_installment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerViewAngsuran)
        recycler.layoutManager = LinearLayoutManager(context)

        // Adapter setup
        adapter = AdminInstallmentAdapter(emptyList()) { item ->
            val bundle = Bundle().apply {
                putString("USER_ID", item.userId)
                putString("LOAN_ID", item.loanId)
                putString("INSTALLMENT_ID", item.id)
            }


            findNavController().navigate(R.id.action_adminInstallmentFragment_to_adminInstallmentDetailFragment, bundle)
        }
        recycler.adapter = adapter

        // Observers
        viewModel.installments.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showProofDialog(url: String) {
        if (url.isEmpty()) {
            Snackbar.make(requireView(), "User tidak melampirkan foto", Snackbar.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_image_preview)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val iv = dialog.findViewById<ImageView>(R.id.ivFullImage)
        Glide.with(this)
            .load(url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(iv)

        dialog.show()
    }
}