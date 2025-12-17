package com.pocketfence.android.service

import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.pocketfence.android.util.NetworkUtils
import com.pocketfence.android.util.NotificationHelper
import com.pocketfence.android.util.PreferencesManager
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class VpnFilterService : VpnService() {
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var prefsManager: PreferencesManager
    
    companion object {
        private const val TAG = "VpnFilterService"
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"
        const val ACTION_START = "com.pocketfence.android.START_VPN"
        const val ACTION_STOP = "com.pocketfence.android.STOP_VPN"
    }
    
    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferencesManager(this)
        NotificationHelper.createNotificationChannel(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startVpn()
            ACTION_STOP -> stopVpn()
        }
        return START_STICKY
    }
    
    private fun startVpn() {
        if (isRunning) return
        
        try {
            // Build VPN interface
            val builder = Builder()
            builder.setSession("PocketFence VPN")
            builder.addAddress(VPN_ADDRESS, 24)
            builder.addRoute(VPN_ROUTE, 0)
            builder.addDnsServer("8.8.8.8")
            builder.addDnsServer("8.8.4.4")
            
            // Set MTU
            builder.setMtu(1500)
            
            // Establish VPN
            vpnInterface = builder.establish()
            
            if (vpnInterface != null) {
                isRunning = true
                
                // Start foreground service
                val notification = NotificationHelper.createProtectionNotification(
                    this,
                    prefsManager.getConnectedDevices().size
                )
                startForeground(NotificationHelper.getNotificationId(), notification)
                
                // Start packet processing
                scope.launch {
                    processPackets()
                }
                
                Log.d(TAG, "VPN started successfully")
            } else {
                Log.e(TAG, "Failed to establish VPN")
                stopSelf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            stopSelf()
        }
    }
    
    private fun stopVpn() {
        isRunning = false
        
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping VPN", e)
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private suspend fun processPackets() = withContext(Dispatchers.IO) {
        val vpnInput = FileInputStream(vpnInterface?.fileDescriptor)
        val vpnOutput = FileOutputStream(vpnInterface?.fileDescriptor)
        
        val buffer = ByteBuffer.allocate(32767)
        val packet = ByteArray(32767)
        
        try {
            while (isRunning) {
                // Read packet from VPN interface
                val length = vpnInput.read(packet)
                if (length > 0) {
                    buffer.clear()
                    buffer.put(packet, 0, length)
                    buffer.flip()
                    
                    // Process packet
                    val shouldBlock = processPacket(buffer)
                    
                    if (!shouldBlock) {
                        // Forward packet if not blocked
                        vpnOutput.write(packet, 0, length)
                    } else {
                        Log.d(TAG, "Packet blocked")
                    }
                }
                
                // Small delay to prevent busy-waiting
                delay(1)
            }
        } catch (e: Exception) {
            if (isRunning) {
                Log.e(TAG, "Error processing packets", e)
            }
        }
    }
    
    private fun processPacket(buffer: ByteBuffer): Boolean {
        try {
            // Read IP version
            val versionAndIHL = buffer.get(0).toInt() and 0xFF
            val version = versionAndIHL shr 4
            
            if (version != 4) {
                // Only handle IPv4 for now
                return false
            }
            
            // Get protocol
            val protocol = buffer.get(9).toInt() and 0xFF
            
            // Get source and destination addresses
            val destAddressBytes = ByteArray(4)
            buffer.position(16)
            buffer.get(destAddressBytes)
            
            val destAddress = String.format(
                "%d.%d.%d.%d",
                destAddressBytes[0].toInt() and 0xFF,
                destAddressBytes[1].toInt() and 0xFF,
                destAddressBytes[2].toInt() and 0xFF,
                destAddressBytes[3].toInt() and 0xFF
            )
            
            // Check if this destination should be blocked
            return shouldBlockAddress(destAddress)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing packet", e)
            return false
        }
    }
    
    private fun shouldBlockAddress(address: String): Boolean {
        // Check blocked websites
        val blockedWebsites = prefsManager.getBlockedWebsites()
        
        // Try to resolve domain names (simplified)
        // In a real implementation, this would need DNS caching and proper resolution
        for (website in blockedWebsites) {
            // This is a simplified check
            // A full implementation would maintain a DNS cache
            if (address.startsWith(website.url) || website.url.contains(address)) {
                prefsManager.incrementBlockedCount()
                if (prefsManager.notificationsEnabled) {
                    NotificationHelper.showBlockedSiteNotification(this, website.url)
                }
                return true
            }
        }
        
        // Check time limits and quiet hours
        val timeLimit = prefsManager.getTimeLimit()
        if (timeLimit.isQuietHoursActive()) {
            return true
        }
        
        return false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        scope.cancel()
    }
}
