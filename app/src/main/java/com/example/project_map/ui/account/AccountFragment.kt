package com.example.project_map.ui.account

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.project_map.databinding.FragmentSimpleListBinding

class AccountFragment : Fragment() {
    private var _b: FragmentSimpleListBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSimpleListBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) { b.tvTitle.text = "Akun" }
    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }

}
