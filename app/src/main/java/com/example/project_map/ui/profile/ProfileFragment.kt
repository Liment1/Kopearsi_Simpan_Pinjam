package com.example.project_map.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_map.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("UserData",
            Context.MODE_PRIVATE
        )
        val name = sharedPreferences.getString("NAME", "Nama User")
        val email = sharedPreferences.getString("EMAIL", "email@example.com")
        val phone = sharedPreferences.getString("PHONE", "") // if you want to use it later

        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val imgProfile = view.findViewById<ImageView>(R.id.imgProfile)

        // set data to the view
        tvName.text = name
        tvEmail.text = email
        imgProfile.setImageResource(R.mipmap.ic_launcher) // default icon

        val tvGantiAkun = view.findViewById<TextView>(R.id.tvGantiAkun)

        tvGantiAkun.setOnClickListener {
            // Navigate back to the login screen
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }
}