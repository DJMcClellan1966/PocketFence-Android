package com.pocketfence.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketfence.android.model.*
import com.pocketfence.android.repository.PocketFenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main ViewModel for the PocketFence app.
 * Manages UI state and business logic for all fragments.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PocketFenceRepository
) : ViewModel() {
    
    val protectionStatus: StateFlow<ProtectionStatus> = repository.protectionStatus
    val connectedDevices: StateFlow<List<ConnectedDevice>> = repository.connectedDevices
    val blockedWebsites: StateFlow<List<BlockedWebsite>> = repository.blockedWebsites
    val timeLimit: StateFlow<TimeLimit> = repository.timeLimit
    val childDevices: StateFlow<List<ChildDevice>> = repository.childDevices
    
    fun refreshData() {
        viewModelScope.launch {
            try {
                repository.refreshAll()
            } catch (e: Exception) {
                // Log error - in production, use proper logging framework
                android.util.Log.e("MainViewModel", "Error refreshing data", e)
            }
        }
    }
    
    // Protection Control
    fun setProtectionActive(active: Boolean) {
        viewModelScope.launch {
            repository.setProtectionActive(active)
        }
    }
    
    // Device Management
    fun blockDevice(macAddress: String, block: Boolean) {
        viewModelScope.launch {
            repository.blockDevice(macAddress, block)
        }
    }
    
    fun setDeviceTimeLimit(macAddress: String, hours: Int, minutes: Int) {
        viewModelScope.launch {
            val timeLimitMs = (hours * 60 * 60 * 1000L) + (minutes * 60 * 1000L)
            repository.setDeviceTimeLimit(macAddress, timeLimitMs)
        }
    }
    
    fun resetDeviceTimeUsage(macAddress: String) {
        viewModelScope.launch {
            repository.resetDeviceTimeUsage(macAddress)
        }
    }
    
    fun renameDevice(macAddress: String, newName: String) {
        viewModelScope.launch {
            repository.renameDevice(macAddress, newName)
        }
    }
    
    // Website Blocking
    fun addBlockedWebsite(url: String, category: WebsiteCategory = WebsiteCategory.CUSTOM) {
        viewModelScope.launch {
            val website = BlockedWebsite(url = url, category = category)
            repository.addBlockedWebsite(website)
        }
    }
    
    fun removeBlockedWebsite(url: String) {
        viewModelScope.launch {
            repository.removeBlockedWebsite(url)
        }
    }
    
    fun addPresetCategory(category: WebsiteCategory) {
        viewModelScope.launch {
            repository.addPresetCategory(category)
        }
    }
    
    // Time Limits
    fun setDailyTimeLimit(hours: Int, minutes: Int) {
        viewModelScope.launch {
            val timeLimitMs = (hours * 60 * 60 * 1000L) + (minutes * 60 * 1000L)
            val currentLimit = timeLimit.value
            val newLimit = currentLimit.copy(dailyLimitMs = timeLimitMs)
            repository.updateTimeLimit(newLimit)
        }
    }
    
    fun setQuietHours(enabled: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch {
            val startMinutes = startHour * 60 + startMinute
            val endMinutes = endHour * 60 + endMinute
            val currentLimit = timeLimit.value
            val newLimit = currentLimit.copy(
                quietHoursEnabled = enabled,
                quietHoursStart = startMinutes,
                quietHoursEnd = endMinutes
            )
            repository.updateTimeLimit(newLimit)
        }
    }
    
    // Settings
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
        }
    }
    
    fun setBlockUnknownDevices(block: Boolean) {
        viewModelScope.launch {
            repository.setBlockUnknownDevices(block)
        }
    }
    
    fun setStrictMode(strict: Boolean) {
        viewModelScope.launch {
            repository.setStrictMode(strict)
        }
    }
    
    fun getNotificationsEnabled(): Boolean = repository.getNotificationsEnabled()
    fun getBlockUnknownDevices(): Boolean = repository.getBlockUnknownDevices()
    fun getStrictMode(): Boolean = repository.getStrictMode()
    
    // Child Device Management
    fun addChildDevice(device: ChildDevice) {
        viewModelScope.launch {
            repository.addChildDevice(device)
        }
    }
    
    fun updateChildDevice(device: ChildDevice) {
        viewModelScope.launch {
            repository.updateChildDevice(device)
        }
    }
    
    fun removeChildDevice(deviceId: String) {
        viewModelScope.launch {
            repository.removeChildDevice(deviceId)
        }
    }
    
    fun setChildDeviceWifiAccess(deviceId: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.setChildDeviceWifiAccess(deviceId, enabled)
        }
    }
    
    fun setChildDeviceCellularAccess(deviceId: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.setChildDeviceCellularAccess(deviceId, enabled)
        }
    }
}
