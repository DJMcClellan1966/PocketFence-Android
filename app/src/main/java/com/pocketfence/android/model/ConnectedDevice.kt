package com.pocketfence.android.model

data class ConnectedDevice(
    val macAddress: String,
    val ipAddress: String,
    val deviceName: String = "Unknown Device",
    val isBlocked: Boolean = false,
    val timeLimit: Long = 0, // in milliseconds, 0 = unlimited
    val timeUsedToday: Long = 0, // in milliseconds
    val lastSeen: Long = System.currentTimeMillis(),
    val firstConnected: Long = System.currentTimeMillis()
) {
    val timeRemaining: Long
        get() = if (timeLimit > 0) {
            (timeLimit - timeUsedToday).coerceAtLeast(0)
        } else {
            Long.MAX_VALUE
        }
    
    val isTimeLimitReached: Boolean
        get() = timeLimit > 0 && timeUsedToday >= timeLimit
    
    fun getFormattedTimeRemaining(): String {
        if (timeLimit == 0L) return "Unlimited"
        val remaining = timeRemaining
        val hours = remaining / (1000 * 60 * 60)
        val minutes = (remaining % (1000 * 60 * 60)) / (1000 * 60)
        return "${hours}h ${minutes}m"
    }
}
