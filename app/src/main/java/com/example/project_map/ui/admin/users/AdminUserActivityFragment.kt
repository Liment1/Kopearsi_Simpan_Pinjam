package com.example.project_map.ui.admin.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.ui.admin.loans.AdminLoanAdapter
import com.example.project_map.ui.admin.savings.SimpananAdapter
// Pastikan Anda punya adapter simpanan yang bisa diakses (bukan inner class)
// import com.example.project_map.ui.adapter.SimpananAdapter
import com.google.android.material.tabs.TabLayout

class AdminUserActivityFragment : Fragment() {

    // Gunakan ViewModel baru
    private val viewModel: AdminUserActivityViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout
    private var userId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_user_activity, container, false)
        userId = arguments?.getString("userId") ?: ""
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerActivity)
        tabLayout = view.findViewById(R.id.tabActivity)
        recyclerView.layoutManager = LinearLayoutManager(context)

        setupTabs()
        setupObservers()

        // Initial Load
        if (userId.isNotEmpty()) {
            viewModel.loadSavings(userId)
        }
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Simpanan"))
        tabLayout.addTab(tabLayout.newTab().setText("Pinjaman"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> viewModel.loadSavings(userId) // Minta VM ambil data
                    1 -> viewModel.loadLoans(userId)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupObservers() {
        // Observe Simpanan
        viewModel.savingsList.observe(viewLifecycleOwner) { list ->
            val adapter = SimpananAdapter(list) { savings ->
            }
            recyclerView.adapter = adapter
        }

        // Observe Pinjaman
        viewModel.loanList.observe(viewLifecycleOwner) { list ->
            val adapter = AdminLoanAdapter(list) { _, _ -> }
            recyclerView.adapter = adapter
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            // Tampilkan progress bar jika ada
        }
    }
}