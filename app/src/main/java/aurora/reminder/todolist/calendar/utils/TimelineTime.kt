package aurora.reminder.todolist.calendar.utils

data class TimelineTime(
    val hour: Int,
    val minute: Int
) {
    fun toPosition(hourWidth: Float): Float {
        return hour * hourWidth + (minute / 60f * hourWidth)
    }
    
    companion object {
        fun fromString(time: String): TimelineTime {
            val parts = time.split(":")
            return TimelineTime(
                hour = parts[0].toInt(),
                minute = parts.getOrNull(1)?.toInt() ?: 0
            )
        }
    }
}