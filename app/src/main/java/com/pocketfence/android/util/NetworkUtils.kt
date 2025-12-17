package com.pocketfence.android.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import java.net.InetAddress
import java.net.NetworkInterface

object NetworkUtils {
    
    fun isWifiEnabled(context: Context): Boolean {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    fun isValidUrl(url: String): Boolean {
        return try {
            if (url.isBlank()) return false
            
            // Remove protocol if present
            val cleanUrl = url.lowercase()
                .removePrefix("http://")
                .removePrefix("https://")
                .removePrefix("www.")
            
            // Basic validation: must contain at least one dot or be localhost
            // and not contain invalid characters
            val hasValidFormat = cleanUrl.isNotEmpty() && 
                   (cleanUrl.contains(".") || cleanUrl == "localhost")
            
            val hasNoInvalidChars = !cleanUrl.contains(" ") && 
                   !cleanUrl.contains("\"") &&
                   !cleanUrl.contains("'")
            
            hasValidFormat && hasNoInvalidChars
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun normalizeUrl(url: String): String {
        return try {
            url.lowercase()
                .removePrefix("http://")
                .removePrefix("https://")
                .removePrefix("www.")
                .split("/")[0] // Get domain only
                .trim()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    
    fun urlMatches(requestedUrl: String, blockedUrl: String): Boolean {
        return try {
            val normalizedRequested = normalizeUrl(requestedUrl)
            val normalizedBlocked = normalizeUrl(blockedUrl)
            
            if (normalizedRequested.isEmpty() || normalizedBlocked.isEmpty()) {
                return false
            }
            
            // Check if the requested URL contains the blocked domain
            normalizedRequested.contains(normalizedBlocked) || 
                   normalizedRequested == normalizedBlocked ||
                   normalizedRequested.endsWith(".$normalizedBlocked")
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Check if WiFi hotspot is enabled.
     * Note: This method uses reflection to access non-public Android APIs.
     * It may not work on all devices/Android versions and could fail in future Android releases.
     * Always has a fallback to return false on error.
     */
    fun isHotspotEnabled(context: Context): Boolean {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            method.invoke(wifiManager) as Boolean
        } catch (e: Exception) {
            // Expected to fail on some devices or Android versions
            // This is a limitation of using non-public APIs
            e.printStackTrace()
            false
        }
    }
    
    fun getConnectedClients(context: Context): List<String> {
        val clients = mutableListOf<String>()
        try {
            // Scan local network for connected devices
            // This is a simplified approach
            val localIp = getLocalIpAddress() ?: return clients
            val subnet = localIp.substring(0, localIp.lastIndexOf('.'))
            
            // Note: Full network scanning requires background thread and can take time
            // This is a placeholder for the actual implementation
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return clients
    }
    
    fun getMacAddress(ipAddress: String): String? {
        try {
            val networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(ipAddress))
            val mac = networkInterface?.hardwareAddress ?: return null
            
            val stringBuilder = StringBuilder()
            for (byte in mac) {
                stringBuilder.append(String.format("%02X:", byte))
            }
            
            if (stringBuilder.isNotEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length - 1)
            }
            
            return stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    fun isInternetAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                @Suppress("DEPRECATION")
                networkInfo?.isConnected == true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
