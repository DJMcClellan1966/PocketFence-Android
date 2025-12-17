package com.pocketfence.android.model

data class TimeLimit(
    val dailyLimitMs: Long = 0, // 0 = unlimited
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 22 * 60, // minutes from midnight (22:00)
    val quietHoursEnd: Int = 7 * 60 // minutes from midnight (07:00)
) {
    fun isQuietHoursActive(): Boolean {
        if (!quietHoursEnabled) return false
        
        val calendar = java.util.Calendar.getInstance()
        val currentMinutes = calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 + 
                            calendar.get(java.util.Calendar.MINUTE)
        
        return if (quietHoursStart < quietHoursEnd) {
            currentMinutes in quietHoursStart..quietHoursEnd
        } else {
            // Spans midnight
            currentMinutes >= quietHoursStart || currentMinutes <= quietHoursEnd
        }
    }
}
