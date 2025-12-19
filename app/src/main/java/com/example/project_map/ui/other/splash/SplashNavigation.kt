package com.example.project_map.ui.other.splash

sealed class SplashNavigation {
    object ToLogin : SplashNavigation()
    object ToHome : SplashNavigation()
    object ToAdmin : SplashNavigation()
}