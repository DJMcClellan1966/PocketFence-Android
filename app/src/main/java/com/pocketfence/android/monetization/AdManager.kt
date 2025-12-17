package com.pocketfence.android.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class for handling Google AdMob ads.
 * Provides functionality for banner ads and interstitial ads.
 */
@Singleton
class AdManager @Inject constructor(
    private val context: Context,
    private val monetizationRepository: MonetizationRepository
) {
    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false
    
    companion object {
        private const val TAG = "AdManager"
        
        // Test ad unit IDs - Replace with your actual ad unit IDs in production
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        
        // Ad display frequency control
        private const val MIN_INTERSTITIAL_INTERVAL_MS = 180000L // 3 minutes
    }
    
    private var lastInterstitialTime = 0L
    
    /**
     * Initialize the Mobile Ads SDK.
     * Should be called once during app initialization.
     */
    fun initialize(onInitialized: () -> Unit = {}) {
        if (isInitialized) {
            onInitialized()
            return
        }
        
        MobileAds.initialize(context) { initializationStatus ->
            isInitialized = true
            Log.d(TAG, "Mobile Ads SDK initialized: ${initializationStatus.adapterStatusMap}")
            onInitialized()
        }
    }
    
    /**
     * Load a banner ad into the provided AdView.
     * Banner ads are only shown to non-premium users.
     */
    fun loadBannerAd(adView: AdView) {
        // Don't show ads to premium users
        if (monetizationRepository.getPremiumStatus()) {
            adView.visibility = ViewGroup.GONE
            Log.d(TAG, "Banner ad skipped - user has premium")
            return
        }
        
        if (!isInitialized) {
            Log.w(TAG, "AdManager not initialized. Call initialize() first.")
            return
        }
        
        adView.visibility = ViewGroup.VISIBLE
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        
        Log.d(TAG, "Banner ad loading...")
    }
    
    /**
     * Load an interstitial ad for later display.
     * Should be called in advance of when you want to show it.
     */
    fun loadInterstitialAd() {
        // Don't load ads for premium users
        if (monetizationRepository.getPremiumStatus()) {
            Log.d(TAG, "Interstitial ad loading skipped - user has premium")
            return
        }
        
        if (!isInitialized) {
            Log.w(TAG, "AdManager not initialized. Call initialize() first.")
            return
        }
        
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                }
                
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                }
            }
        )
    }
    
    /**
     * Show an interstitial ad if one is loaded and enough time has passed since the last one.
     * Respects frequency capping to avoid annoying users.
     * 
     * @param activity The activity to show the ad in
     * @param onAdClosed Callback invoked when the ad is closed or fails to show
     */
    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        // Don't show ads to premium users
        if (monetizationRepository.getPremiumStatus()) {
            Log.d(TAG, "Interstitial ad skipped - user has premium")
            onAdClosed()
            return
        }
        
        // Check if enough time has passed since last ad
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInterstitialTime < MIN_INTERSTITIAL_INTERVAL_MS) {
            Log.d(TAG, "Interstitial ad skipped - too soon since last ad")
            onAdClosed()
            return
        }
        
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed")
                    lastInterstitialTime = currentTime
                    interstitialAd = null
                    // Load the next ad
                    loadInterstitialAd()
                    onAdClosed()
                }
                
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Interstitial ad failed to show: ${error.message}")
                    interstitialAd = null
                    onAdClosed()
                }
                
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed")
                }
            }
            
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready")
            // Try to load one for next time
            loadInterstitialAd()
            onAdClosed()
        }
    }
    
    /**
     * Clean up resources when the ad is no longer needed.
     */
    fun destroy() {
        interstitialAd = null
    }
}
