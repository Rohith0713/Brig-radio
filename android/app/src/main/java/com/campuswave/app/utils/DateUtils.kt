package com.campuswave.app.utils

object DateUtils {
    fun formatRadioDate(startTime: String): String {
        return try {
            val date = startTime.substringBefore("T")
            val time = startTime.substringAfter("T").take(5)
            "$date • $time"
        } catch (e: Exception) {
            startTime
        }
    }

    fun formatRadioDateTime(startTime: String, endTime: String): String {
        return try {
            val date = startTime.substringBefore("T")
            val start = startTime.substringAfter("T").take(5)
            val end = endTime.substringAfter("T").take(5)
            "$date • $start - $end"
        } catch (e: Exception) {
            startTime
        }
    }

    fun formatRadioTime(time: String): String {
        return try {
            time.substringAfter("T").take(5)
        } catch (e: Exception) {
            time
        }
    }
    
    /**
     * Formats a timestamp as relative time (e.g., "5 minutes ago", "2 hours ago")
     */
    fun formatRelativeTime(isoTimestamp: String): String {
        return try {
            val dateTime = isoTimestamp.substringBefore(".")
            val parts = dateTime.split("T")
            if (parts.size != 2) return isoTimestamp
            
            val dateParts = parts[0].split("-")
            val timeParts = parts[1].split(":")
            
            if (dateParts.size != 3 || timeParts.size < 2) return isoTimestamp
            
            val year = dateParts[0].toIntOrNull() ?: return isoTimestamp
            val month = dateParts[1].toIntOrNull() ?: return isoTimestamp
            val day = dateParts[2].toIntOrNull() ?: return isoTimestamp
            val hour = timeParts[0].toIntOrNull() ?: return isoTimestamp
            val minute = timeParts[1].toIntOrNull() ?: return isoTimestamp
            
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
            cal.set(year, month - 1, day, hour, minute, 0)
            
            val diffMs = System.currentTimeMillis() - cal.timeInMillis
            val diffMinutes = diffMs / (1000 * 60)
            val diffHours = diffMinutes / 60
            val diffDays = diffHours / 24
            
            when {
                diffMinutes < 1 -> "just now"
                diffMinutes < 60 -> "${diffMinutes}m ago"
                diffHours < 24 -> "${diffHours}h ago"
                diffDays < 7 -> "${diffDays}d ago"
                else -> "${parts[0]}"
            }
        } catch (e: Exception) {
            isoTimestamp.substringBefore("T")
        }
    }
    
    /**
     * Formats a timestamp for chat message display (e.g., "10:30 AM")
     */
    fun formatMessageTime(isoTimestamp: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            
            val outputFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
            outputFormat.timeZone = java.util.TimeZone.getDefault()
            
            // Handle fractional seconds if present
            val cleanTimestamp = if (isoTimestamp.contains(".")) {
                 isoTimestamp.substringBefore(".")
            } else {
                 isoTimestamp
            }
            
            val date = inputFormat.parse(cleanTimestamp)
            if (date != null) {
                outputFormat.format(date)
            } else {
                isoTimestamp.substringAfter("T").take(5)
            }
        } catch (e: Exception) {
            // Fallback for when parsing fails (e.g. invalid format)
            try {
                isoTimestamp.substringAfter("T").take(5)
            } catch (e2: Exception) {
                ""
            }
        }
    }

    /**
     * Data class for comprehensive radio status info
     */
    data class RadioStatusInfo(
        val statusLabel: String,         // "LIVE NOW", "Upcoming", "Ended"
        val statusIcon: String,          // "🔴", "⏳", "✅"
        val timeInfo: String?,           // "Time left: 18 mins", "Starts in 25 mins", "Starts at 10:00 AM"
        val startTimeFormatted: String,  // "10:00 AM"
        val endTimeFormatted: String,    // "11:00 AM"
        val isLive: Boolean,
        val isUpcoming: Boolean,
        val isEnded: Boolean,
        val isStartingSoon: Boolean      // Transitioning soon (< 30 mins)
    )

    fun getRadioStatusInfo(startTimeStr: String, endTimeStr: String, synchronizedTimeMillis: Long = System.currentTimeMillis()): RadioStatusInfo {
        try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            inputFormat.timeZone = java.util.TimeZone.getDefault() // Match backend local time

            val cleanStart = startTimeStr.substringBefore(".")
            val cleanEnd = endTimeStr.substringBefore(".")
            
            val startDate = inputFormat.parse(cleanStart) ?: return fallbackInfo(startTimeStr, endTimeStr)
            val endDate = inputFormat.parse(cleanEnd) ?: return fallbackInfo(startTimeStr, endTimeStr)
            
            val startTime = startDate.time
            val endTime = endDate.time
            val currentTime = synchronizedTimeMillis
            
            val timeOutputFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val startTimeFormatted = timeOutputFormat.format(startDate)
            val endTimeFormatted = timeOutputFormat.format(endDate)
            
            return when {
                currentTime < startTime -> {
                    val diffMins = (startTime - currentTime) / (1000 * 60)
                    val diffSecs = (startTime - currentTime) / 1000
                    
                    if (diffMins < 60) {
                        val mm = diffMins
                        val ss = diffSecs % 60
                        val timeString = if (mm > 0) String.format("%02dm %02ds", mm, ss) else String.format("%02ds", ss)
                        
                        RadioStatusInfo(
                            statusLabel = "Upcoming",
                            statusIcon = "⏳",
                            timeInfo = "Starts in $timeString",
                            startTimeFormatted = startTimeFormatted,
                            endTimeFormatted = endTimeFormatted,
                            isLive = false,
                            isUpcoming = true,
                            isEnded = false,
                            isStartingSoon = true
                        )
                    } else {
                        RadioStatusInfo(
                            statusLabel = "Upcoming",
                            statusIcon = "⏳",
                            timeInfo = "Starts at $startTimeFormatted",
                            startTimeFormatted = startTimeFormatted,
                            endTimeFormatted = endTimeFormatted,
                            isLive = false,
                            isUpcoming = true,
                            isEnded = false,
                            isStartingSoon = false
                        )
                    }
                }
                currentTime in startTime..endTime -> {
                    val timeLeftMins = (endTime - currentTime) / (1000 * 60)
                    RadioStatusInfo(
                        statusLabel = "LIVE NOW",
                        statusIcon = "🔴",
                        timeInfo = if (timeLeftMins > 0) "Time left: $timeLeftMins mins" else "Ending shortly",
                        startTimeFormatted = startTimeFormatted,
                        endTimeFormatted = endTimeFormatted,
                        isLive = true,
                        isUpcoming = false,
                        isEnded = false,
                        isStartingSoon = false
                    )
                }
                else -> {
                    RadioStatusInfo(
                        statusLabel = "Ended",
                        statusIcon = "✅",
                        timeInfo = null,
                        startTimeFormatted = startTimeFormatted,
                        endTimeFormatted = endTimeFormatted,
                        isLive = false,
                        isUpcoming = false,
                        isEnded = true,
                        isStartingSoon = false
                    )
                }
            }
        } catch (e: Exception) {
            return fallbackInfo(startTimeStr, endTimeStr)
        }
    }

    /**
     * Parses an ISO date string to Epoch Millis
     */
    fun parseIsoToMillis(isoTimestamp: String?): Long? {
        if (isoTimestamp == null) return null
        return try {
            val cleanTimestamp = isoTimestamp.substringBefore(".")
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            inputFormat.timeZone = java.util.TimeZone.getDefault() // Match backend local time
            inputFormat.parse(cleanTimestamp)?.time
        } catch (e: Exception) {
            null
        }
    }

    private fun fallbackInfo(start: String, end: String): RadioStatusInfo {
        return RadioStatusInfo(
            statusLabel = "Scheduled",
            statusIcon = "⏳",
            timeInfo = null,
            startTimeFormatted = start.substringAfter("T").take(5),
            endTimeFormatted = end.substringAfter("T").take(5),
            isLive = false,
            isUpcoming = true,
            isEnded = false,
            isStartingSoon = false
        )
    }
}
