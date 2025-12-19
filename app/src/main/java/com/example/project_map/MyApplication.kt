package com.example.project_map

import android.app.Application
import com.example.project_map.BuildConfig
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = HashMap<String, String>()

        // USE THE GENERATED CONFIG HERE
        config["cloud_name"] =  BuildConfig.CLOUD_NAME
        config["secure"] = "true"

        MediaManager.init(this, config)
    }
}