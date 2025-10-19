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

        // Defines the top-level destinations for the navigation drawer.
        // The hamburger icon will show on these screens.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.adminDashboardFragment,
                R.id.adminDataAnggotaFragment,
                R.id.fragmentPinjamanAdmin,
                R.id.fragmentTransaksiSimpanan,
                R.id.adminAngsuranFragment,
                R.id.adminLoanListFragment,
                R.id.adminLaporanKeuanganFragment
            ), drawerLayout
        )

        // Connects the toolbar with the NavController to automatically
        // update the title and handle the hamburger/up icon.
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Sets up a listener to handle clicks on the menu items in the sidebar.
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    logout()
                    true // Indicates the click has been handled.
                }
                else -> {
                    // For all other items, navigate to the corresponding fragment.
                    navController.navigate(menuItem.itemId)
                    drawerLayout.closeDrawers() // Close the drawer after selection.
                    true
                }
            }
        }
    }

    /**
     * Handles the logout process by clearing the user session and
     * returning to the main login screen.
     */
    private fun logout() {
        val sessionPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        sessionPrefs.edit().clear().apply()

        val intent = Intent(this, MainActivity::class.java)
        // Clears the activity stack so the user can't press "back" to re-enter the admin area.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Closes the AdminActivity.
    }

    /**
     * This function is required to make the hamburger and up buttons work correctly.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}

