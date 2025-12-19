package com.example.project_map.ui.auth.register

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ProgressBar
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.project_map.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val viewModel: RegisterViewModel by viewModels()

    // View References
    private lateinit var layoutName: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var layoutPhone: TextInputLayout
    private lateinit var etPhone: TextInputEditText
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var layoutConfirm: TextInputLayout
    private lateinit var etConfirm: TextInputEditText
    private lateinit var cbTerms: CheckBox
    private lateinit var btnRegister: MaterialButton
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Bind Views
        layoutName = view.findViewById(R.id.layoutName)
        etName = view.findViewById(R.id.etName)
        layoutPhone = view.findViewById(R.id.layoutPhone)
        etPhone = view.findViewById(R.id.etPhone)
        layoutEmail = view.findViewById(R.id.layoutEmail)
        etEmail = view.findViewById(R.id.etEmail)
        layoutPassword = view.findViewById(R.id.layoutPassword)
        etPassword = view.findViewById(R.id.etPassword)
        layoutConfirm = view.findViewById(R.id.layoutConfirm)
        etConfirm = view.findViewById(R.id.etConfirm)
        cbTerms = view.findViewById(R.id.cbTerms)
        btnRegister = view.findViewById(R.id.btnRegister)
        progressBar = view.findViewById(R.id.progressBar)

        setupListeners(view)
        observeViewModel()
    }

    private fun setupListeners(view: View) {
        // Back Button
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        // Login Redirect
        view.findViewById<View>(R.id.tvLoginRedirect).setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        // Register Action
        btnRegister.setOnClickListener {
            viewModel.register(
                name = etName.text.toString().trim(),
                phone = etPhone.text.toString().trim(),
                email = etEmail.text.toString().trim(),
                pass = etPassword.text.toString().trim(),
                confirmPass = etConfirm.text.toString().trim(),
                termsAccepted = cbTerms.isChecked
            )
        }

        // Clear errors on typing
        val clearError = { layout: TextInputLayout -> layout.error = null }
        etName.doOnTextChanged { _, _, _, _ -> clearError(layoutName) }
        etPhone.doOnTextChanged { _, _, _, _ -> clearError(layoutPhone) }
        etEmail.doOnTextChanged { _, _, _, _ -> clearError(layoutEmail) }
        etPassword.doOnTextChanged { _, _, _, _ -> clearError(layoutPassword) }
        etConfirm.doOnTextChanged { _, _, _, _ -> clearError(layoutConfirm) }
    }

    private fun observeViewModel() {
        // 1. Observe Loading & Success
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterViewModel.RegisterState.Loading -> setLoading(true)
                is RegisterViewModel.RegisterState.Success -> {
                    setLoading(false)
                    Snackbar.make(requireView(), "Registration Successful!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(resources.getColor(android.R.color.holo_green_dark))
                        .show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
                is RegisterViewModel.RegisterState.Error -> {
                    setLoading(false)
                    showSnackbar(state.message)
                }
                else -> setLoading(false)
            }
        }

        // 2. Observe Field Errors
        viewModel.nameError.observe(viewLifecycleOwner) { layoutName.error = it }
        viewModel.phoneError.observe(viewLifecycleOwner) { layoutPhone.error = it }
        viewModel.emailError.observe(viewLifecycleOwner) { layoutEmail.error = it }
        viewModel.passwordError.observe(viewLifecycleOwner) { layoutPassword.error = it }
        viewModel.confirmPasswordError.observe(viewLifecycleOwner) { layoutConfirm.error = it }

        viewModel.termsError.observe(viewLifecycleOwner) { isError ->
            if (isError) showSnackbar("You must agree to the Terms & Conditions")
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            btnRegister.text = ""
            btnRegister.isEnabled = false
            progressBar.visibility = View.VISIBLE
        } else {
            btnRegister.text = "REGISTER"
            btnRegister.isEnabled = true
            progressBar.visibility = View.GONE
        }
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark))
            .setTextColor(resources.getColor(android.R.color.white))
            .show()
    }
}