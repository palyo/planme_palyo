package aurora.reminder.todolist.calendar.extension

import android.annotation.*
import android.app.*
import android.content.*
import aurora.reminder.todolist.calendar.database.table.*
import aurora.reminder.todolist.calendar.receiver.*
import java.util.*

@SuppressLint("ScheduleExactAlarm")
fun Context.scheduleDailyReminders(task: Task) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(this, TaskReminderReceiver::class.java).apply {
        putExtra("TASK_TITLE", task.title)
        putExtra("TASK_ID", task.id)
    }
    val startDate = Calendar.getInstance().apply {
        timeInMillis = task.startDate
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val endDate = Calendar.getInstance().apply {
        timeInMillis = task.endDate
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    while (startDate.timeInMillis <= endDate.timeInMillis) {
        // Calculate the reminder time (10 minutes before the daily start time)
        val reminderTime = Calendar.getInstance().apply {
            timeInMillis = startDate.timeInMillis
            add(Calendar.HOUR_OF_DAY, (task.dailyStartTime / (1000 * 60 * 60)).toInt())
            add(Calendar.MINUTE, ((task.dailyStartTime / (1000 * 60)) % 60).toInt())
            add(Calendar.MILLISECOND, ((task.dailyStartTime % (1000 * 60)).toInt()))
            add(Calendar.MINUTE, -10) // 10 minutes before the start time
        }
        // Schedule the alarm
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            reminderTime.timeInMillis,
            PendingIntent.getBroadcast(
                this,
                task.id?.plus(startDate.get(Calendar.DAY_OF_YEAR)) ?: 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        // Move to the next day
        startDate.add(Calendar.DAY_OF_YEAR, 1)
    }
}

fun Context.cancelReminder(taskId: Int) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(this, TaskReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        this, taskId, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
    }
}
