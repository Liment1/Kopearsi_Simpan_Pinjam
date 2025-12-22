package com.example.project_map.ui.user.profile.detail

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.example.project_map.R
import com.example.project_map.databinding.FragmentDetailProfileBinding // UPDATED BINDING CLASS
import com.example.project_map.ui.user.profile.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth

class UserDetailProfileFragment : Fragment() {

    private var _binding: FragmentDetailProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private var imageCacheParams: String = ""

    // MVVM: ViewModel
    private val viewModel: UserProfileViewModel by viewModels()

    // Image Picker
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val userId = auth.currentUser?.uid ?: return@let
            viewModel.uploadProfilePicture(userId, it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.navigationIcon?.setTint(android.graphics.Color.BLACK)
        // Setup Toolbar Back Button
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(context, "Sesi habis.", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupObservers()
        viewModel.loadUserProfile(currentUser.uid)
        setupListeners(currentUser)
    }

    private fun setupListeners(currentUser: com.google.firebase.auth.FirebaseUser) {
        // 1. Image Picker
        binding.fabChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 2. Save Changes
        binding.btnSave.setOnClickListener {
            val newName = binding.etName.text.toString().trim()
            val newEmail = binding.etEmail.text.toString().trim()
            val newPhone = binding.etPhone.text.toString().trim()

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(context, "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateUserProfile(
                uid = currentUser.uid,
                name = newName,
                email = newEmail,
                phone = newPhone
            )
        }
    }

    private fun setupObservers() {
        // 1. Observe Profile Data Loading
        viewModel.userProfileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UserProfileViewModel.ProfileState.Loading -> { }
                is UserProfileViewModel.ProfileState.Success -> {
                    val user = state.user
                    binding.etKodePegawai.setText(user.memberCode.ifEmpty { user.id })
                    binding.etName.setText(user.name)
                    binding.etEmail.setText(user.email)
                    binding.etPhone.setText(user.phone)
                    loadImageSafe(user.avatarUrl)
                }
                is UserProfileViewModel.ProfileState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 2. Observe Upload Status
        viewModel.uploadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UserProfileViewModel.UploadState.Idle -> {
                    binding.ivProfile.alpha = 1.0f
                }
                is UserProfileViewModel.UploadState.Loading -> {
                    binding.ivProfile.alpha = 0.5f
                    Toast.makeText(context, "Memproses...", Toast.LENGTH_SHORT).show()
                }
                is UserProfileViewModel.UploadState.Success -> {
                    binding.ivProfile.alpha = 1.0f
                    if (state.imageUrl.isNotEmpty()) {
                        Toast.makeText(context, "Foto berhasil diubah", Toast.LENGTH_SHORT).show()
                        imageCacheParams = "?v=${System.currentTimeMillis()}"
                        loadImageSafe(state.imageUrl)
                    } else {
                        Toast.makeText(context, "Data profil berhasil disimpan", Toast.LENGTH_SHORT).show()
                        // Optional: Navigate back after success
                        // findNavController().navigateUp()
                    }
                }
                is UserProfileViewModel.UploadState.Error -> {
                    binding.ivProfile.alpha = 1.0f
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadImageSafe(url: String) {
        if (url.isNotEmpty()) {
            val urlToLoad = url + imageCacheParams
            binding.ivProfile.load(urlToLoad) {
                crossfade(true)
                transformations(CircleCropTransformation())
                placeholder(R.drawable.account_circle)
                error(R.drawable.ic_launcher_background) // Consider using account_circle here too
                memoryCacheKey(urlToLoad)
                diskCacheKey(urlToLoad)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}