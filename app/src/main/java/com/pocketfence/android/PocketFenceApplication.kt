package com.pocketfence.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for PocketFence.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class PocketFenceApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize app-wide components here if needed
    }
}
