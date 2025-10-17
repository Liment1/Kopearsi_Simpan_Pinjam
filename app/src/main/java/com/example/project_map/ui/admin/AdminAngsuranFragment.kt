package com.example.project_map.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project_map.R

class AdminAngsuranFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // This is a placeholder screen. In a real app, you would inflate a layout
        // with a RecyclerView to show a list of all ongoing installments.
        return inflater.inflate(R.layout.fragment_admin_angsuran, container, false)
    }
}