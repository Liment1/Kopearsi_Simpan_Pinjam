package com.example.project_map.ui.other.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.ui.admin.AdminActivity

class SplashFragment : Fragment() {

    private val viewModel: SplashViewModel by viewModels()
    private var progressBar: ProgressBar? = null
    private var rootLayout: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootLayout = view.findViewById(R.id.splashRoot)
        progressBar = view.findViewById(R.id.progressBar) // Ensure this ID exists in XML

        // 1. Setup Click Listener
        rootLayout?.setOnClickListener {
            viewModel.checkUserSession()
        }

        // 2. Observe ViewModel States
        observeViewModel()
    }

    private fun observeViewModel() {
        // Observe Navigation
        viewModel.navigationState.observe(viewLifecycleOwner) { state ->
            if (state == null) return@observe

            when (state) {
                is SplashNavigation.ToAdmin -> {
                    // Toast Removed. Direct navigation.
                    val intent = Intent(requireActivity(), AdminActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                is SplashNavigation.ToHome -> {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                }
                is SplashNavigation.ToLogin -> {
                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                }
            }
            viewModel.onNavigationComplete()
        }

        // Observe Loading (New!)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                progressBar?.visibility = View.VISIBLE
                rootLayout?.isClickable = false // Prevent double clicks
            } else {
                progressBar?.visibility = View.GONE
                rootLayout?.isClickable = true
            }
        }
    }
}