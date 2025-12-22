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

        debugAdminRule()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_admin)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.admin_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_admin_dashboard,
                R.id.nav_admin_data_anggota,
                R.id.nav_admin_pengajuan_pinjaman,
                R.id.nav_admin_riwayat_simpanan,
                R.id.nav_admin_angsuran_berjalan,
                R.id.nav_admin_laporan_keuangan,
                R.id.nav_admin_notifikasi,
                R.id.nav_admin_pengaturan,
                R.id.nav_admin_permintaan_penarikan
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

    fun debugAdminRule() {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            android.util.Log.e("DEBUG_RULES", "FATAL: User is NOT logged in!")
            return
        }

        val uid = currentUser.uid
        android.util.Log.d("DEBUG_RULES", "1. Your Auth UID is: $uid")

        // Check the User Document exactly how the Rule does
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    android.util.Log.d("DEBUG_RULES", "2. Document found at: users/$uid")

                    // CHECK THE ADMIN FIELD
                    val adminValue = document.get("admin")
                    val adminType = adminValue?.javaClass?.simpleName

                    android.util.Log.d("DEBUG_RULES", "3. Raw 'admin' field value: $adminValue")
                    android.util.Log.d("DEBUG_RULES", "4. 'admin' field Type: $adminType")

                    if (adminValue == true) {
                        android.util.Log.i("DEBUG_RULES", "SUCCESS: Data looks correct. You are an Admin.")
                    } else {
                        android.util.Log.e("DEBUG_RULES", "FAILURE: You are NOT an admin in the database.")
                        if (adminType == "String") {
                            android.util.Log.e("DEBUG_RULES", "FIX: You saved 'true' as a String. Change it to a Boolean in Firebase Console.")
                        }
                    }
                } else {
                    android.util.Log.e("DEBUG_RULES", "FAILURE: No document found for this UID. Rules cannot find your admin status.")
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("DEBUG_RULES", "ERROR: Could not read user profile: ${e.message}")
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