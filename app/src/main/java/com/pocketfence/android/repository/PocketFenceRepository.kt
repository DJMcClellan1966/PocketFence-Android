package com.pocketfence.android.repository

import android.content.Context
import com.pocketfence.android.model.*
import com.pocketfence.android.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PocketFenceRepository(context: Context) {
    
    private val prefsManager = PreferencesManager(context)
    
    private val _protectionStatus = MutableStateFlow(getProtectionStatus())
    val protectionStatus: StateFlow<ProtectionStatus> = _protectionStatus.asStateFlow()
    
    private val _connectedDevices = MutableStateFlow<List<ConnectedDevice>>(emptyList())
    val connectedDevices: StateFlow<List<ConnectedDevice>> = _connectedDevices.asStateFlow()
    
    private val _blockedWebsites = MutableStateFlow<List<BlockedWebsite>>(emptyList())
    val blockedWebsites: StateFlow<List<BlockedWebsite>> = _blockedWebsites.asStateFlow()
    
    private val _timeLimit = MutableStateFlow(TimeLimit())
    val timeLimit: StateFlow<TimeLimit> = _timeLimit.asStateFlow()
    
    init {
        refreshAll()
    }
    
    fun refreshAll() {
        _connectedDevices.value = prefsManager.getConnectedDevices()
        _blockedWebsites.value = prefsManager.getBlockedWebsites()
        _timeLimit.value = prefsManager.getTimeLimit()
        _protectionStatus.value = getProtectionStatus()
    }
    
    private fun getProtectionStatus(): ProtectionStatus {
        return ProtectionStatus(
            isActive = prefsManager.isProtectionActive,
            connectedDevicesCount = prefsManager.getConnectedDevices().size,
            blockedSitesToday = prefsManager.getBlockedCountToday(),
            vpnConnected = prefsManager.isProtectionActive
        )
    }
    
    // Protection
    fun setProtectionActive(active: Boolean) {
        prefsManager.isProtectionActive = active
        refreshAll()
    }
    
    // Devices
    fun updateDevice(device: ConnectedDevice) {
        prefsManager.updateDevice(device)
        _connectedDevices.value = prefsManager.getConnectedDevices()
        refreshAll()
    }
    
    fun blockDevice(macAddress: String, block: Boolean) {
        val devices = prefsManager.getConnectedDevices()
        val device = devices.find { it.macAddress == macAddress }
        if (device != null) {
            updateDevice(device.copy(isBlocked = block))
        }
    }
    
    fun setDeviceTimeLimit(macAddress: String, timeLimitMs: Long) {
        val devices = prefsManager.getConnectedDevices()
        val device = devices.find { it.macAddress == macAddress }
        if (device != null) {
            updateDevice(device.copy(timeLimit = timeLimitMs))
        }
    }
    
    fun resetDeviceTimeUsage(macAddress: String) {
        val devices = prefsManager.getConnectedDevices()
        val device = devices.find { it.macAddress == macAddress }
        if (device != null) {
            updateDevice(device.copy(timeUsedToday = 0))
        }
    }
    
    fun renameDevice(macAddress: String, newName: String) {
        val devices = prefsManager.getConnectedDevices()
        val device = devices.find { it.macAddress == macAddress }
        if (device != null) {
            updateDevice(device.copy(deviceName = newName))
        }
    }
    
    // Blocked Websites
    fun addBlockedWebsite(website: BlockedWebsite) {
        prefsManager.addBlockedWebsite(website)
        _blockedWebsites.value = prefsManager.getBlockedWebsites()
    }
    
    fun removeBlockedWebsite(url: String) {
        prefsManager.removeBlockedWebsite(url)
        _blockedWebsites.value = prefsManager.getBlockedWebsites()
    }
    
    fun addPresetCategory(category: WebsiteCategory) {
        val presetWebsites = getPresetWebsitesForCategory(category)
        presetWebsites.forEach { url ->
            val website = BlockedWebsite(url = url, category = category)
            prefsManager.addBlockedWebsite(website)
        }
        _blockedWebsites.value = prefsManager.getBlockedWebsites()
    }
    
    private fun getPresetWebsitesForCategory(category: WebsiteCategory): List<String> {
        return when (category) {
            WebsiteCategory.SOCIAL_MEDIA -> listOf(
                "facebook.com", "instagram.com", "twitter.com", "x.com",
                "tiktok.com", "snapchat.com", "reddit.com", "pinterest.com"
            )
            WebsiteCategory.ADULT_CONTENT -> listOf(
                "pornhub.com", "xvideos.com", "xnxx.com", "xvideosxxx.com"
            )
            WebsiteCategory.GAMBLING -> listOf(
                "bet365.com", "888casino.com", "pokerstars.com", "draftkings.com"
            )
            WebsiteCategory.GAMING -> listOf(
                "twitch.tv", "roblox.com", "minecraft.net", "fortnite.com",
                "steam.com", "epicgames.com"
            )
            WebsiteCategory.VIOLENCE -> listOf(
                "liveleak.com", "bestgore.com"
            )
            WebsiteCategory.DRUGS -> listOf(
                "weedmaps.com", "leafly.com"
            )
            else -> emptyList()
        }
    }
    
    // Time Limits
    fun updateTimeLimit(timeLimit: TimeLimit) {
        prefsManager.saveTimeLimit(timeLimit)
        _timeLimit.value = timeLimit
    }
    
    // Settings
    fun setNotificationsEnabled(enabled: Boolean) {
        prefsManager.notificationsEnabled = enabled
    }
    
    fun setBlockUnknownDevices(block: Boolean) {
        prefsManager.blockUnknownDevices = block
    }
    
    fun setStrictMode(strict: Boolean) {
        prefsManager.strictMode = strict
    }
    
    fun getNotificationsEnabled(): Boolean = prefsManager.notificationsEnabled
    fun getBlockUnknownDevices(): Boolean = prefsManager.blockUnknownDevices
    fun getStrictMode(): Boolean = prefsManager.strictMode
}
