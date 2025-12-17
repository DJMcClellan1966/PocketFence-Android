package com.pocketfence.android.model

data class ProtectionStatus(
    val isActive: Boolean = false,
    val connectedDevicesCount: Int = 0,
    val blockedSitesToday: Int = 0,
    val vpnConnected: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
