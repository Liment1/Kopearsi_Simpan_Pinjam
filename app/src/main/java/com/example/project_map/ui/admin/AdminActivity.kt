package com.example.project_map.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.project_map.MainActivity
import com.example.project_map.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth // Import this!

class AdminActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_admin)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.admin_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.adminDashboardFragment,
                R.id.adminDataAnggotaFragment,
                R.id.fragmentPinjamanAdmin,
                R.id.fragmentTransaksiSimpanan,
                R.id.adminAngsuranFragment,
                R.id.adminLaporanKeuanganFragment,
                R.id.adminNotifikasiFragment,
                R.id.adminPengaturanFragment
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> {
                    navController.navigate(menuItem.itemId)
                    drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }

    private fun logout() {
        // 1. SIGN OUT FROM FIREBASE (This is the missing link)
        FirebaseAuth.getInstance().signOut()

        // 2. Clear legacy preferences (Optional, but good cleanup)
        val sessionPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        sessionPrefs.edit().clear().apply()

        // 3. Restart App from Splash Screen
        val intent = Intent(this, MainActivity::class.java)
        // Clear back stack so user can't press back to return to Admin
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}