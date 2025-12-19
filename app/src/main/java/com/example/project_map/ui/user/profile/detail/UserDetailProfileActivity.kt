package com.example.project_map.ui.user.profile.detail

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.example.project_map.R
import com.example.project_map.databinding.ActivityDetailProfileBinding // Make sure this matches your XML file name
import com.example.project_map.ui.user.profile.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth

class UserDetailProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProfileBinding
    private lateinit var auth: FirebaseAuth
    private var imageCacheParams: String = ""
    // MVVM: Inject ViewModel
    private val viewModel: UserProfileViewModel by viewModels()

    // Variable to track the current image URL for refreshing
    private var currentAvatarUrl: String = ""

    // Image Picker
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val userId = auth.currentUser?.uid ?: return@let
            viewModel.uploadProfilePicture(userId, it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar (Optional based on your XML)
        // setSupportActionBar(binding.toolbar)
        // supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Sesi habis.", Toast.LENGTH_SHORT).show()
            finish()
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

        // 2. Save Changes (Name, Email, Phone)
        binding.btnSave.setOnClickListener {
            val newName = binding.etName.text.toString().trim()
            val newEmail = binding.etEmail.text.toString().trim()
            val newPhone = binding.etPhone.text.toString().trim()

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call ViewModel to update data (Create this function in your ViewModel if missing)
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
        viewModel.userProfileState.observe(this) { state ->
            when (state) {
                is UserProfileViewModel.ProfileState.Loading -> {
                    // Optional: Show loading progress
                }
                is UserProfileViewModel.ProfileState.Success -> {
                    val user = state.user

                    // Set Text Fields
                    binding.etKodePegawai.setText(user.memberCode.ifEmpty { user.id })
                    binding.etName.setText(user.name)
                    binding.etEmail.setText(user.email)
                    binding.etPhone.setText(user.phone)

                    // Load Image
                    loadImageSafe(user.avatarUrl)
                }
                is UserProfileViewModel.ProfileState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 2. Observe Upload/Update Status
        viewModel.uploadState.observe(this) { state ->
            when (state) {
                is UserProfileViewModel.UploadState.Idle -> {
                    // Reset UI state if needed
                    binding.ivProfile.alpha = 1.0f
                }
                is UserProfileViewModel.UploadState.Loading -> {
                    binding.ivProfile.alpha = 0.5f
                    Toast.makeText(this, "Memproses...", Toast.LENGTH_SHORT).show()
                }
                is UserProfileViewModel.UploadState.Success -> {
                    binding.ivProfile.alpha = 1.0f

                    // If an image URL was returned, update the image
                    if (state.imageUrl.isNotEmpty()) {
                        Toast.makeText(this, "Foto berhasil diubah", Toast.LENGTH_SHORT).show()

                        // UPDATE VERSION TIMESTAMP (The Cache Fix)
                        imageCacheParams = "?v=${System.currentTimeMillis()}"
                        loadImageSafe(state.imageUrl)
                    } else {
                        // If empty string, it was just a text data update
                        Toast.makeText(this, "Data profil berhasil disimpan", Toast.LENGTH_SHORT).show()
                    }
                }
                is UserProfileViewModel.UploadState.Error -> {
                    binding.ivProfile.alpha = 1.0f
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadImageSafe(url: String) {
        if (url.isNotEmpty()) {


            // Construct a URL that looks like: "https://cloudinary.../image.jpg?v=17099999"
            val urlToLoad = url + imageCacheParams
            android.util.Log.d("DEBUG_IMAGE", "Loading URL: $urlToLoad")
            binding.ivProfile.load(urlToLoad) {
                crossfade(true)
                transformations(CircleCropTransformation())
                placeholder(R.drawable.account_circle) // Make sure this drawable exists
                error(R.drawable.ic_launcher_background)

                // This ensures Coil treats it as a unique memory key
                memoryCacheKey(urlToLoad)
                diskCacheKey(urlToLoad)
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}