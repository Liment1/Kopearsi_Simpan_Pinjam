package com.example.project_map.ui.admin.users

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.project_map.data.model.UserData
import com.example.project_map.databinding.DialogAdminEditUserBinding
import com.example.project_map.databinding.FragmentAdminUserBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

class AdminUserFragment : Fragment() {

    private var _binding: FragmentAdminUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminUserViewModel by viewModels()
    private lateinit var adminUserAdapter: AdminUserAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adminUserAdapter = AdminUserAdapter(emptyList()) { anggota ->
            showTabbedEditDialog(anggota)
        }

        binding.rvAnggota.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAnggota.adapter = adminUserAdapter

        viewModel.members.observe(viewLifecycleOwner) { list ->
            adminUserAdapter.submitList(list)
        }


    }

    private fun showTabbedEditDialog(user: UserData) {
        val dialog = Dialog(requireContext())
        val dBinding = DialogAdminEditUserBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 1. Setup Data - Personal
        dBinding.etName.setText(user.name)
        if (user.avatarUrl.isNotEmpty()) {
            Glide.with(this).load(user.avatarUrl).into(dBinding.ivProfilePreview)
        }

        // 2. Setup Data - Financial (Read Only Score)
        dBinding.tvScoreValue.text = "${user.creditScore} / 850"

        // Use ProgressBar instead of Slider
        dBinding.progressCreditScore.progress = user.creditScore

        dBinding.etSimpananPokok.setText(user.simpananPokok.toInt().toString())
        dBinding.etSimpananWajib.setText(user.simpananWajib.toInt().toString())

        // 3. Tab Logic
        dBinding.tabLayoutUser.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { // Personal
                        dBinding.layoutPersonal.visibility = View.VISIBLE
                        dBinding.layoutFinancial.visibility = View.GONE
                    }
                    1 -> { // Financial
                        dBinding.layoutPersonal.visibility = View.GONE
                        dBinding.layoutFinancial.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 4. Save Action
        dBinding.btnSaveUser.setOnClickListener {
            val newName = dBinding.etName.text.toString()
            // Note: We DO NOT get the score from the UI anymore. It's read-only.

            val newPokok = dBinding.etSimpananPokok.text.toString().toDoubleOrNull() ?: 0.0
            val newWajib = dBinding.etSimpananWajib.text.toString().toDoubleOrNull() ?: 0.0

            // Call ViewModel (Function updated to not accept score)
            viewModel.updateMemberComplete(user.id, newName, newPokok, newWajib)

            dialog.dismiss()
            Snackbar.make(binding.root, "Data anggota diperbarui", Snackbar.LENGTH_SHORT).show()
        }

        dBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}