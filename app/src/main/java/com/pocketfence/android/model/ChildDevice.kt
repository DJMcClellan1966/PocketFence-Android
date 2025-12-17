package com.pocketfence.android.model

/**
 * Represents a child's device managed by parents with access control capabilities
 */
data class ChildDevice(
    val deviceId: String,  // Unique identifier (MAC address or device-specific ID)
    val deviceName: String = "Unknown Device",
    val childName: String = "Child",  // Name of the child assigned to this device
    val macAddress: String = "",
    val ipAddress: String = "",
    val isWifiEnabled: Boolean = true,  // WiFi access control
    val isCellularEnabled: Boolean = true,  // Cellular access control
    val isOnline: Boolean = false,
    val networkType: NetworkType = NetworkType.UNKNOWN,
    val lastSeen: Long = System.currentTimeMillis(),
    val addedTime: Long = System.currentTimeMillis()
) {
    /**
     * Returns true if device has any restrictions active
     */
    val hasRestrictions: Boolean
        get() = !isWifiEnabled || !isCellularEnabled
    
    /**
     * Returns current access status description
     */
    fun getAccessStatusText(): String {
        return when {
            !isWifiEnabled && !isCellularEnabled -> "All Access Blocked"
            !isWifiEnabled -> "WiFi Blocked"
            !isCellularEnabled -> "Cellular Blocked"
            else -> "Full Access"
        }
    }
}

/**
 * Network type for device connectivity
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    UNKNOWN,
    OFFLINE
}
