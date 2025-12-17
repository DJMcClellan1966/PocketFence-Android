package com.pocketfence.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketfence.android.model.*
import com.pocketfence.android.repository.PocketFenceRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = PocketFenceRepository(application)
    
    val protectionStatus: StateFlow<ProtectionStatus> = repository.protectionStatus
    val connectedDevices: StateFlow<List<ConnectedDevice>> = repository.connectedDevices
    val blockedWebsites: StateFlow<List<BlockedWebsite>> = repository.blockedWebsites
    val timeLimit: StateFlow<TimeLimit> = repository.timeLimit
    
    fun refreshData() {
        viewModelScope.launch {
            repository.refreshAll()
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
}
