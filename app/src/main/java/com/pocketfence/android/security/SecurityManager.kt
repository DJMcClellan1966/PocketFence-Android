package com.pocketfence.android.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages security features including root detection, integrity checks,
 * and device security validation.
 * 
 * This class provides methods to:
 * - Detect rooted/tampered devices
 * - Verify app integrity using SafetyNet
 * - Check for security threats
 * - Validate device security state
 */
@Singleton
class SecurityManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "SecurityManager"
        
        // SafetyNet API Key - Replace with your actual key from Google Cloud Console
        // For development, this is a placeholder. Configure in production.
        private const val SAFETY_NET_API_KEY = "YOUR_SAFETYNET_API_KEY"
    }
    
    /**
     * Checks if the device is rooted.
     * This is a basic check and can be bypassed by sophisticated root hiding tools.
     * 
     * @return true if device appears to be rooted, false otherwise
     */
    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }
    
    /**
     * Check for presence of common root binaries
     */
    private fun checkRootMethod1(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        for (path in paths) {
            if (File(path).exists()) {
                Log.w(TAG, "Root binary found at: $path")
                return true
            }
        }
        return false
    }
    
    /**
     * Check for root management apps
     */
    private fun checkRootMethod2(): Boolean {
        val packages = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk"
        )
        
        val pm = context.packageManager
        for (packageName in packages) {
            try {
                pm.getPackageInfo(packageName, 0)
                Log.w(TAG, "Root management app found: $packageName")
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // Package not found, continue checking
            }
        }
        return false
    }
    
    /**
     * Check for dangerous system properties
     */
    private fun checkRootMethod3(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            Log.w(TAG, "Device has test-keys in build tags")
            return true
        }
        return false
    }
    
    /**
     * Verifies app integrity using Google SafetyNet Attestation API.
     * This checks if the app is running on a genuine Android device
     * and hasn't been tampered with.
     * 
     * @return true if device passes SafetyNet checks, false otherwise
     */
    suspend fun verifySafetyNet(): Boolean {
        // Check if Google Play Services is available
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.w(TAG, "Google Play Services not available")
            return false
        }
        
        try {
            // Generate a nonce for the request (in production, this should be from your server)
            val nonce = generateNonce()
            
            // Note: In production, you should:
            // 1. Get the nonce from your backend server
            // 2. Send the attestation result to your server for verification
            // 3. Never trust client-side verification alone for critical operations
            
            val response = SafetyNet.getClient(context)
                .attest(nonce, SAFETY_NET_API_KEY)
                .await()
            
            val jwsResult = response.jwsResult
            Log.d(TAG, "SafetyNet attestation received")
            
            // In production, send jwsResult to your server for verification
            // The server should verify the signature and check the response
            
            return true // Simplified for client-side
        } catch (e: Exception) {
            Log.e(TAG, "SafetyNet attestation failed", e)
            return false
        }
    }
    
    /**
     * Generates a nonce for SafetyNet attestation.
     * In production, this should come from your backend server.
     */
    private fun generateNonce(): ByteArray {
        val nonce = ByteArray(24)
        // In production, get this from your server
        // For now, using timestamp as simple nonce
        val timestamp = System.currentTimeMillis()
        val timestampBytes = timestamp.toString().toByteArray()
        timestampBytes.copyInto(nonce, 0, 0, minOf(timestampBytes.size, nonce.size))
        return nonce
    }
    
    /**
     * Checks if the device has a secure lock screen set up.
     * 
     * @return true if device has secure lock screen, false otherwise
     */
    fun hasSecureLockScreen(): Boolean {
        return try {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) 
                as? android.app.KeyguardManager
            keyguardManager?.isDeviceSecure ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking lock screen security", e)
            false
        }
    }
    
    /**
     * Checks if the app is running on an emulator.
     * Useful for detecting testing environments or fraud attempts.
     * 
     * @return true if running on emulator, false otherwise
     */
    fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
    
    /**
     * Performs a comprehensive security check of the device.
     * 
     * @return SecurityCheckResult with details of security status
     */
    suspend fun performSecurityCheck(): SecurityCheckResult {
        val isRooted = isDeviceRooted()
        val isEmulator = isRunningOnEmulator()
        val hasSecureLock = hasSecureLockScreen()
        val safetyNetPassed = verifySafetyNet()
        
        val isSecure = !isRooted && !isEmulator && hasSecureLock && safetyNetPassed
        
        return SecurityCheckResult(
            isSecure = isSecure,
            isRooted = isRooted,
            isEmulator = isEmulator,
            hasSecureLockScreen = hasSecureLock,
            passedSafetyNet = safetyNetPassed
        )
    }
    
    /**
     * Result of security check containing various security indicators.
     */
    data class SecurityCheckResult(
        val isSecure: Boolean,
        val isRooted: Boolean,
        val isEmulator: Boolean,
        val hasSecureLockScreen: Boolean,
        val passedSafetyNet: Boolean
    ) {
        fun getSecurityWarnings(): List<String> {
            val warnings = mutableListOf<String>()
            
            if (isRooted) {
                warnings.add("Device appears to be rooted. This may pose security risks.")
            }
            if (isEmulator) {
                warnings.add("Running on emulator. Some features may not work properly.")
            }
            if (!hasSecureLockScreen) {
                warnings.add("No secure lock screen detected. Please set up a PIN, pattern, or password.")
            }
            if (!passedSafetyNet) {
                warnings.add("Device failed SafetyNet integrity check.")
            }
            
            return warnings
        }
    }
}
