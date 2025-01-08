package aurora.reminder.todolist.calendar.receiver

import android.app.*
import android.content.*
import androidx.core.app.*
import aurora.reminder.todolist.calendar.*

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("TASK_TITLE")
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = taskId
        val notification = NotificationCompat.Builder(context, "task_channel")
            .setContentTitle("Reminder")
            .setContentText("It's time to work on: $taskTitle")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
