package com.pocketfence.android.monetization

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing monetization-related data persistence.
 * Stores premium status and user preferences related to monetization.
 */
@Singleton
class MonetizationRepository @Inject constructor(
    private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    private val _isPremium = MutableStateFlow(getPremiumStatus())
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()
    
    companion object {
        private const val PREFS_NAME = "monetization_prefs"
        private const val KEY_PREMIUM_STATUS = "is_premium"
        private const val KEY_LAST_AD_SHOWN = "last_ad_shown_time"
        private const val KEY_AD_CLICKS = "ad_clicks_count"
        private const val KEY_PURCHASE_DATE = "premium_purchase_date"
    }
    
    /**
     * Get the current premium status.
     */
    fun getPremiumStatus(): Boolean {
        return sharedPreferences.getBoolean(KEY_PREMIUM_STATUS, false)
    }
    
    /**
     * Set the premium status.
     */
    fun setPremiumStatus(isPremium: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_PREMIUM_STATUS, isPremium)
            if (isPremium) {
                putLong(KEY_PURCHASE_DATE, System.currentTimeMillis())
            }
            apply()
        }
        _isPremium.value = isPremium
    }
    
    /**
     * Get the timestamp of when premium was purchased.
     */
    fun getPurchaseDate(): Long {
        return sharedPreferences.getLong(KEY_PURCHASE_DATE, 0L)
    }
    
    /**
     * Record that an ad was shown.
     */
    fun recordAdShown() {
        sharedPreferences.edit().apply {
            putLong(KEY_LAST_AD_SHOWN, System.currentTimeMillis())
            apply()
        }
    }
    
    /**
     * Get the timestamp of the last ad shown.
     */
    fun getLastAdShownTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_AD_SHOWN, 0L)
    }
    
    /**
     * Increment the ad click counter (for analytics).
     */
    fun incrementAdClicks() {
        val currentClicks = sharedPreferences.getInt(KEY_AD_CLICKS, 0)
        sharedPreferences.edit().apply {
            putInt(KEY_AD_CLICKS, currentClicks + 1)
            apply()
        }
    }
    
    /**
     * Get the total number of ad clicks.
     */
    fun getAdClicksCount(): Int {
        return sharedPreferences.getInt(KEY_AD_CLICKS, 0)
    }
    
    /**
     * Clear all monetization data.
     * Useful for testing or troubleshooting.
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
        _isPremium.value = false
    }
}
