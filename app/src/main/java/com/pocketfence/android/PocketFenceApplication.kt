package com.pocketfence.android

import android.app.Application
import com.pocketfence.android.monetization.AdManager
import com.pocketfence.android.monetization.BillingManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for PocketFence.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class PocketFenceApplication : Application() {
    
    @Inject
    lateinit var adManager: AdManager
    
    @Inject
    lateinit var billingManager: BillingManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize monetization components
        initializeMonetization()
    }
    
    private fun initializeMonetization() {
        // Initialize Google Mobile Ads SDK
        adManager.initialize {
            // SDK initialized - load first interstitial ad
            adManager.loadInterstitialAd()
        }
        
        // Initialize Google Play Billing
        billingManager.initialize()
    }
}
