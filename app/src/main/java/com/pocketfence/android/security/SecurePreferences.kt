package com.pocketfence.android.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides encrypted SharedPreferences for storing sensitive data.
 * Uses AndroidX Security library to encrypt data at rest.
 * 
 * This should be used for storing:
 * - API keys
 * - User credentials
 * - Premium status
 * - Payment information
 * - Any other sensitive user data
 */
@Singleton
class SecurePreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "SecurePreferences"
        private const val SECURE_PREFS_NAME = "pocket_fence_secure_prefs"
    }
    
    private var encryptedPreferences: SharedPreferences? = null
    
    /**
     * Gets the encrypted SharedPreferences instance.
     * Creates it if it doesn't exist.
     * 
     * @return Encrypted SharedPreferences or null if creation fails
     */
    fun getSecurePreferences(): SharedPreferences? {
        if (encryptedPreferences != null) {
            return encryptedPreferences
        }
        
        try {
            // Create or retrieve the master key for encryption
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            // Create encrypted shared preferences
            encryptedPreferences = EncryptedSharedPreferences.create(
                context,
                SECURE_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            Log.d(TAG, "Encrypted SharedPreferences initialized successfully")
            return encryptedPreferences
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize encrypted SharedPreferences", e)
            return null
        }
    }
    
    /**
     * Stores a string value securely.
     * 
     * @param key The key to store the value under
     * @param value The value to store
     * @return true if successful, false otherwise
     */
    fun putString(key: String, value: String): Boolean {
        return try {
            getSecurePreferences()?.edit()?.apply {
                putString(key, value)
                apply()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store string securely", e)
            false
        }
    }
    
    /**
     * Retrieves a string value securely.
     * 
     * @param key The key to retrieve
     * @param defaultValue The default value if key doesn't exist
     * @return The stored value or defaultValue
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return try {
            getSecurePreferences()?.getString(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve string securely", e)
            defaultValue
        }
    }
    
    /**
     * Stores a boolean value securely.
     * 
     * @param key The key to store the value under
     * @param value The value to store
     * @return true if successful, false otherwise
     */
    fun putBoolean(key: String, value: Boolean): Boolean {
        return try {
            getSecurePreferences()?.edit()?.apply {
                putBoolean(key, value)
                apply()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store boolean securely", e)
            false
        }
    }
    
    /**
     * Retrieves a boolean value securely.
     * 
     * @param key The key to retrieve
     * @param defaultValue The default value if key doesn't exist
     * @return The stored value or defaultValue
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            getSecurePreferences()?.getBoolean(key, defaultValue) ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve boolean securely", e)
            defaultValue
        }
    }
    
    /**
     * Stores a long value securely.
     * 
     * @param key The key to store the value under
     * @param value The value to store
     * @return true if successful, false otherwise
     */
    fun putLong(key: String, value: Long): Boolean {
        return try {
            getSecurePreferences()?.edit()?.apply {
                putLong(key, value)
                apply()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store long securely", e)
            false
        }
    }
    
    /**
     * Retrieves a long value securely.
     * 
     * @param key The key to retrieve
     * @param defaultValue The default value if key doesn't exist
     * @return The stored value or defaultValue
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return try {
            getSecurePreferences()?.getLong(key, defaultValue) ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve long securely", e)
            defaultValue
        }
    }
    
    /**
     * Removes a value from secure storage.
     * 
     * @param key The key to remove
     * @return true if successful, false otherwise
     */
    fun remove(key: String): Boolean {
        return try {
            getSecurePreferences()?.edit()?.apply {
                remove(key)
                apply()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove key securely", e)
            false
        }
    }
    
    /**
     * Clears all secure preferences.
     * Use with caution as this will remove all encrypted data.
     * 
     * @return true if successful, false otherwise
     */
    fun clear(): Boolean {
        return try {
            getSecurePreferences()?.edit()?.apply {
                clear()
                apply()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear secure preferences", e)
            false
        }
    }
    
    /**
     * Checks if a key exists in secure storage.
     * 
     * @param key The key to check
     * @return true if key exists, false otherwise
     */
    fun contains(key: String): Boolean {
        return try {
            getSecurePreferences()?.contains(key) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check key existence", e)
            false
        }
    }
}
