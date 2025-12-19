package com.example.project_map.ui.admin.installments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        adapter = AdminInstallmentAdapter(emptyList())
        recycler.adapter = adapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.installments.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}