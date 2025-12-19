package com.example.project_map.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.example.project_map.ui.admin.AdminActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    // UI References
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvRegister: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize Views
        // Note: These IDs must match your new professional XML layout
        layoutEmail = view.findViewById(R.id.layoutEmail)
        etEmail = view.findViewById(R.id.etEmail)
        layoutPassword = view.findViewById(R.id.layoutPassword)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        progressBar = view.findViewById(R.id.progressBar)
        tvRegister = view.findViewById(R.id.tvRegister)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Login Button Click
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        // Register Click
        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // UX improvement: Clear red error text when user starts typing
        etEmail.doOnTextChanged { _, _, _, _ -> layoutEmail.error = null }
        etPassword.doOnTextChanged { _, _, _, _ -> layoutPassword.error = null }

        // UX improvement: Allow "Done" button on keyboard to trigger login
        etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnLogin.performClick()
                true
            } else false
        }
    }

    private fun observeViewModel() {
        // A. General State Observer
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    setLoadingState(true)
                }
                is LoginViewModel.LoginState.Error -> {
                    setLoadingState(false)
                    // REPLACED TOAST WITH SNACKBAR
                    showErrorSnackbar(state.message)
                }
                is LoginViewModel.LoginState.NavigateToHome -> {
                    setLoadingState(false)
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    viewModel.resetState()
                }
                is LoginViewModel.LoginState.NavigateToAdmin -> {
                    setLoadingState(false)
                    val intent = Intent(requireActivity(), AdminActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                    viewModel.resetState()
                }
                else -> setLoadingState(false)
            }
        }

        // B. Field Error Observers (The "Professional" part)
        viewModel.emailError.observe(viewLifecycleOwner) { errorMsg ->
            // This puts the error text directly on the input field
            layoutEmail.error = errorMsg
        }

        viewModel.passwordError.observe(viewLifecycleOwner) { errorMsg ->
            layoutPassword.error = errorMsg
        }
    }

    // Helper to toggle button text vs spinner
    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            btnLogin.text = "" // Hides text so spinner is visible clearly
            btnLogin.isEnabled = false
            progressBar.visibility = View.VISIBLE
            layoutEmail.isEnabled = false
            layoutPassword.isEnabled = false
        } else {
            btnLogin.text = "LOGIN"
            btnLogin.isEnabled = true
            progressBar.visibility = View.GONE
            layoutEmail.isEnabled = true
            layoutPassword.isEnabled = true
        }
    }

    private fun showErrorSnackbar(message: String) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(resources.getColor(android.R.color.holo_red_dark))
        snackbar.setTextColor(resources.getColor(android.R.color.white))
        snackbar.show()
    }
}