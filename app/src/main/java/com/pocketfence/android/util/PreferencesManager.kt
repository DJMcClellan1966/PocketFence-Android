package com.pocketfence.android.util

import android.content.Context
import android.content.SharedPreferences
import com.pocketfence.android.model.BlockedWebsite
import com.pocketfence.android.model.ConnectedDevice
import com.pocketfence.android.model.TimeLimit
import com.pocketfence.android.model.WebsiteCategory
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("pocketfence_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_BLOCKED_WEBSITES = "blocked_websites"
        private const val KEY_CONNECTED_DEVICES = "connected_devices"
        private const val KEY_TIME_LIMIT = "time_limit"
        private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
        private const val KEY_QUIET_HOURS_START = "quiet_hours_start"
        private const val KEY_QUIET_HOURS_END = "quiet_hours_end"
        private const val KEY_BLOCKED_COUNT_TODAY = "blocked_count_today"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val KEY_PROTECTION_ACTIVE = "protection_active"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_BLOCK_UNKNOWN = "block_unknown"
        private const val KEY_STRICT_MODE = "strict_mode"
    }
    
    // Blocked Websites
    fun getBlockedWebsites(): List<BlockedWebsite> {
        val json = prefs.getString(KEY_BLOCKED_WEBSITES, "[]") ?: "[]"
        val list = mutableListOf<BlockedWebsite>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(BlockedWebsite(
                    url = obj.getString("url"),
                    category = WebsiteCategory.valueOf(obj.optString("category", "CUSTOM")),
                    addedTime = obj.optLong("addedTime", System.currentTimeMillis()),
                    timesBlocked = obj.optInt("timesBlocked", 0)
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
    
    fun saveBlockedWebsites(websites: List<BlockedWebsite>) {
        val array = JSONArray()
        websites.forEach { website ->
            val obj = JSONObject()
            obj.put("url", website.url)
            obj.put("category", website.category.name)
            obj.put("addedTime", website.addedTime)
            obj.put("timesBlocked", website.timesBlocked)
            array.put(obj)
        }
        prefs.edit().putString(KEY_BLOCKED_WEBSITES, array.toString()).apply()
    }
    
    fun addBlockedWebsite(website: BlockedWebsite) {
        val websites = getBlockedWebsites().toMutableList()
        websites.add(website)
        saveBlockedWebsites(websites)
    }
    
    fun removeBlockedWebsite(url: String) {
        val websites = getBlockedWebsites().filter { it.url != url }
        saveBlockedWebsites(websites)
    }
    
    // Connected Devices
    fun getConnectedDevices(): List<ConnectedDevice> {
        val json = prefs.getString(KEY_CONNECTED_DEVICES, "[]") ?: "[]"
        val list = mutableListOf<ConnectedDevice>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(ConnectedDevice(
                    macAddress = obj.getString("macAddress"),
                    ipAddress = obj.getString("ipAddress"),
                    deviceName = obj.optString("deviceName", "Unknown Device"),
                    isBlocked = obj.optBoolean("isBlocked", false),
                    timeLimit = obj.optLong("timeLimit", 0),
                    timeUsedToday = obj.optLong("timeUsedToday", 0),
                    lastSeen = obj.optLong("lastSeen", System.currentTimeMillis()),
                    firstConnected = obj.optLong("firstConnected", System.currentTimeMillis())
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
    
    fun saveConnectedDevices(devices: List<ConnectedDevice>) {
        val array = JSONArray()
        devices.forEach { device ->
            val obj = JSONObject()
            obj.put("macAddress", device.macAddress)
            obj.put("ipAddress", device.ipAddress)
            obj.put("deviceName", device.deviceName)
            obj.put("isBlocked", device.isBlocked)
            obj.put("timeLimit", device.timeLimit)
            obj.put("timeUsedToday", device.timeUsedToday)
            obj.put("lastSeen", device.lastSeen)
            obj.put("firstConnected", device.firstConnected)
            array.put(obj)
        }
        prefs.edit().putString(KEY_CONNECTED_DEVICES, array.toString()).apply()
    }
    
    fun updateDevice(device: ConnectedDevice) {
        val devices = getConnectedDevices().toMutableList()
        val index = devices.indexOfFirst { it.macAddress == device.macAddress }
        if (index >= 0) {
            devices[index] = device
        } else {
            devices.add(device)
        }
        saveConnectedDevices(devices)
    }
    
    // Time Limits
    fun getTimeLimit(): TimeLimit {
        return TimeLimit(
            dailyLimitMs = prefs.getLong(KEY_TIME_LIMIT, 0),
            quietHoursEnabled = prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false),
            quietHoursStart = prefs.getInt(KEY_QUIET_HOURS_START, 22 * 60),
            quietHoursEnd = prefs.getInt(KEY_QUIET_HOURS_END, 7 * 60)
        )
    }
    
    fun saveTimeLimit(timeLimit: TimeLimit) {
        prefs.edit()
            .putLong(KEY_TIME_LIMIT, timeLimit.dailyLimitMs)
            .putBoolean(KEY_QUIET_HOURS_ENABLED, timeLimit.quietHoursEnabled)
            .putInt(KEY_QUIET_HOURS_START, timeLimit.quietHoursStart)
            .putInt(KEY_QUIET_HOURS_END, timeLimit.quietHoursEnd)
            .apply()
    }
    
    // Statistics
    fun getBlockedCountToday(): Int {
        checkAndResetDailyStats()
        return prefs.getInt(KEY_BLOCKED_COUNT_TODAY, 0)
    }
    
    fun incrementBlockedCount() {
        checkAndResetDailyStats()
        val count = prefs.getInt(KEY_BLOCKED_COUNT_TODAY, 0)
        prefs.edit().putInt(KEY_BLOCKED_COUNT_TODAY, count + 1).apply()
    }
    
    private fun checkAndResetDailyStats() {
        val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val lastReset = prefs.getInt(KEY_LAST_RESET_DATE, -1)
        
        if (today != lastReset) {
            // Reset daily statistics
            prefs.edit()
                .putInt(KEY_BLOCKED_COUNT_TODAY, 0)
                .putInt(KEY_LAST_RESET_DATE, today)
                .apply()
            
            // Reset time used for all devices
            val devices = getConnectedDevices().map { it.copy(timeUsedToday = 0) }
            saveConnectedDevices(devices)
        }
    }
    
    // Settings
    var isProtectionActive: Boolean
        get() = prefs.getBoolean(KEY_PROTECTION_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_PROTECTION_ACTIVE, value).apply()
    
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
    
    var blockUnknownDevices: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_UNKNOWN, false)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_UNKNOWN, value).apply()
    
    var strictMode: Boolean
        get() = prefs.getBoolean(KEY_STRICT_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_STRICT_MODE, value).apply()
}
