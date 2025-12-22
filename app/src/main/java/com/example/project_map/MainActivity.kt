package com.example.project_map

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.project_map.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val db = Firebase.firestore

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Notification permission granted")
        } else {
            Log.w("FCM", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController

        NavigationUI.setupWithNavController(binding.bottomNav, navController)

        // HIDE Bottom Nav on these screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Auth & Splash
                R.id.splashFragment,
                R.id.loginFragment,
                R.id.registerFragment,

                    // Detail / Form Screens (Hide here too for better UX)
                R.id.loanDetailFragment,
                R.id.angsuranFragment,
                R.id.LoanForm,
                R.id.userDetailProfileFragment,
                R.id.userMonthlyReportFragment -> {
                    binding.bottomNav.visibility = View.GONE
                }

                // Show on Main Tabs
                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }


        FirebaseMessaging.getInstance().subscribeToTopic("all_members")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Subscribe failed", task.exception)
                }
            }
    }
}