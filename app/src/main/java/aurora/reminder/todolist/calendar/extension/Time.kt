package aurora.reminder.todolist.calendar.extension

import java.text.*
import java.util.*

fun fetchStartAndEndOfDay(): Pair<Long, Long> {
    val calendar = Calendar.getInstance(Locale.ENGLISH)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfDay = calendar.timeInMillis

    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val endOfDay = calendar.timeInMillis

    return Pair(startOfDay, endOfDay)
}

fun fetchStartAndEndOfDay(timeInMillis: Long): Pair<Long, Long> {
    val calendar = Calendar.getInstance(Locale.ENGLISH)
    // Set calendar to the given date
    calendar.timeInMillis = timeInMillis
    // Set to the start of the day
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfDay = calendar.timeInMillis
    // Set to the end of the day
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val endOfDay = calendar.timeInMillis
    return Pair(startOfDay, endOfDay)
}

fun Long.convertMillisTo24HourTime(): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return dateFormat.format(Date(this))
}

fun createDateRangeString(startDateMillis: Long, endDateMillis: Long): String {
    val dayFormat = SimpleDateFormat("d", Locale.ENGLISH)
    val monthFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
    val startDay = dayFormat.format(Date(startDateMillis))
    val endDay = dayFormat.format(Date(endDateMillis))
    val startMonth = monthFormat.format(Date(startDateMillis))
    val endMonth = monthFormat.format(Date(endDateMillis))

    return if (startMonth == endMonth) {
        "$startDay-$endDay $startMonth" // Same month
    } else {
        "$startDay $startMonth - $endDay $endMonth" // Different months
    }
}

fun calculateDuration(startTimeMillis: Long, endTimeMillis: Long): String {
    val durationMillis = endTimeMillis - startTimeMillis
    val hours = durationMillis / (1000 * 60 * 60)
    val minutes = (durationMillis / (1000 * 60)) % 60
    return if (hours > 0) {
        String.format("%dh %02dmin", hours, minutes)
    } else {
        String.format("%02d min", minutes)
    }
}

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)
    return dateFormat.format(Date())
}

fun Long.getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)
    return dateFormat.format(this)
}

fun daysBetween(startMillis: Long, endMillis: Long): Long {
    val millisecondsPerDay = 24 * 60 * 60 * 1000
    return (endMillis - startMillis) / millisecondsPerDay
}