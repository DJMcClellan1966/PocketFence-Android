package com.pocketfence.android.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.pocketfence.android.model.ConnectedDevice
import com.pocketfence.android.util.NotificationHelper
import com.pocketfence.android.util.PreferencesManager
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader

class MonitoringService : Service() {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isMonitoring = false
    private lateinit var prefsManager: PreferencesManager
    
    companion object {
        private const val TAG = "MonitoringService"
        private const val SCAN_INTERVAL = 10000L // 10 seconds
        const val ACTION_START_MONITORING = "com.pocketfence.android.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.pocketfence.android.STOP_MONITORING"
    }
    
    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferencesManager(this)
        NotificationHelper.createNotificationChannel(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> stopMonitoring()
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        
        // Start foreground service
        val notification = NotificationHelper.createProtectionNotification(
            this,
            prefsManager.getConnectedDevices().size
        )
        startForeground(NotificationHelper.getNotificationId(), notification)
        
        // Start monitoring loop
        scope.launch {
            monitorDevices()
        }
        
        Log.d(TAG, "Monitoring started")
    }
    
    private fun stopMonitoring() {
        isMonitoring = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d(TAG, "Monitoring stopped")
    }
    
    private suspend fun monitorDevices() = withContext(Dispatchers.IO) {
        while (isMonitoring) {
            try {
                scanConnectedDevices()
                updateTimeUsage()
                checkTimeLimits()
                
                // Update notification
                updateNotification()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error monitoring devices", e)
            }
            
            delay(SCAN_INTERVAL)
        }
    }
    
    private fun scanConnectedDevices() {
        try {
            // Read ARP table to find connected devices
            val arpEntries = readArpTable()
            val currentDevices = prefsManager.getConnectedDevices().toMutableList()
            val foundMacs = mutableSetOf<String>()
            
            for (entry in arpEntries) {
                val (ip, mac) = entry
                if (mac.isNotEmpty() && !mac.equals("00:00:00:00:00:00", ignoreCase = true)) {
                    foundMacs.add(mac)
                    
                    val existingDevice = currentDevices.find { it.macAddress == mac }
                    if (existingDevice != null) {
                        // Update existing device
                        val updated = existingDevice.copy(
                            ipAddress = ip,
                            lastSeen = System.currentTimeMillis()
                        )
                        currentDevices[currentDevices.indexOf(existingDevice)] = updated
                    } else {
                        // Add new device
                        val newDevice = ConnectedDevice(
                            macAddress = mac,
                            ipAddress = ip,
                            deviceName = getDeviceName(ip, mac),
                            isBlocked = prefsManager.blockUnknownDevices
                        )
                        currentDevices.add(newDevice)
                        Log.d(TAG, "New device connected: $mac")
                    }
                }
            }
            
            // Remove devices not seen recently (older than 1 minute)
            val oneMinuteAgo = System.currentTimeMillis() - 60000
            val activeDevices = currentDevices.filter { 
                it.lastSeen > oneMinuteAgo || foundMacs.contains(it.macAddress)
            }
            
            prefsManager.saveConnectedDevices(activeDevices)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning devices", e)
        }
    }
    
    private fun readArpTable(): List<Pair<String, String>> {
        val entries = mutableListOf<Pair<String, String>>()
        try {
            val process = Runtime.getRuntime().exec("cat /proc/net/arp")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            // Skip header line
            reader.readLine()
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val parts = line!!.split("\\s+".toRegex())
                if (parts.size >= 4) {
                    val ip = parts[0]
                    val mac = parts[3]
                    entries.add(Pair(ip, mac))
                }
            }
            
            reader.close()
            process.waitFor()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading ARP table", e)
        }
        return entries
    }
    
    private fun getDeviceName(ip: String, mac: String): String {
        // Try to get hostname
        try {
            val process = Runtime.getRuntime().exec("getprop net.hostname")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val hostname = reader.readLine()
            reader.close()
            process.waitFor()
            
            if (!hostname.isNullOrEmpty()) {
                return hostname
            }
        } catch (e: Exception) {
            // Ignore
        }
        
        // Fallback to MAC-based name
        return "Device-${mac.takeLast(8).replace(":", "")}"
    }
    
    private fun updateTimeUsage() {
        val devices = prefsManager.getConnectedDevices()
        val now = System.currentTimeMillis()
        val updatedDevices = devices.map { device ->
            if (!device.isBlocked && device.timeLimit > 0) {
                // Add scan interval to time used
                device.copy(timeUsedToday = device.timeUsedToday + SCAN_INTERVAL)
            } else {
                device
            }
        }
        prefsManager.saveConnectedDevices(updatedDevices)
    }
    
    private fun checkTimeLimits() {
        val devices = prefsManager.getConnectedDevices()
        devices.forEach { device ->
            if (device.isTimeLimitReached && !device.isBlocked) {
                // Block device due to time limit
                val blocked = device.copy(isBlocked = true)
                prefsManager.updateDevice(blocked)
                
                if (prefsManager.notificationsEnabled) {
                    NotificationHelper.showTimeLimitNotification(this, device.deviceName)
                }
                
                Log.d(TAG, "Device ${device.deviceName} blocked due to time limit")
            }
        }
    }
    
    private fun updateNotification() {
        val deviceCount = prefsManager.getConnectedDevices().size
        val notification = NotificationHelper.createProtectionNotification(this, deviceCount)
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NotificationHelper.getNotificationId(), notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        scope.cancel()
    }
}
