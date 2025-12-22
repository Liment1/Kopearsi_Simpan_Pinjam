package com.example.project_map.ui.admin.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_map.R
import com.example.project_map.data.model.Loan
import com.example.project_map.data.model.Savings
import com.example.project_map.ui.admin.loans.AdminLoanAdapter
import com.example.project_map.ui.admin.savings.FragmentAdminSavingsHistory
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore

// IMPORTANT: You need to create 'fragment_admin_user_activity.xml' with a TabLayout and RecyclerView
class AdminUserActivityFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout
    private val db = FirebaseFirestore.getInstance()
    private var userId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate your layout containing R.id.tabActivity and R.id.recyclerActivity
        val view = inflater.inflate(R.layout.fragment_admin_user_activity, container, false)
        userId = arguments?.getString("userId") ?: ""
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerActivity)
        tabLayout = view.findViewById(R.id.tabActivity)

        recyclerView.layoutManager = LinearLayoutManager(context)

        // Set Tabs
        tabLayout.addTab(tabLayout.newTab().setText("Simpanan"))
        tabLayout.addTab(tabLayout.newTab().setText("Pinjaman"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> loadSavings()
                    1 -> loadLoans()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Initial Load
        loadSavings()
    }

    private fun loadSavings() {
        db.collectionGroup("savings")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener {
                val list = it.toObjects(Savings::class.java)
                // Reuse your savings adapter or create a simple one
                // For demonstration, creating a quick instance of the adapter from the other fragment might be hard
                // Ideally, extract the adapter class to its own file.
                // Assuming you extracted 'SimpananAdapter' to 'com.example.project_map.ui.adapter.SimpananAdapter'
                // recyclerView.adapter = SimpananAdapter(list) {}
            }
    }

    private fun loadLoans() {
        db.collection("loans")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener {
                val list = it.toObjects(Loan::class.java)
                val adapter = AdminLoanAdapter(list) { _, _ -> } // View only
                recyclerView.adapter = adapter
            }
    }
}