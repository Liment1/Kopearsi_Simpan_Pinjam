package com.example.project_map

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Init Cloudinary
        val config = HashMap<String, String>()
        config["cloud_name"] = "dy9xihbkh" // From Cloudinary Dashboard
        config["secure"] = "true"

        MediaManager.init(this, config)
    }
}