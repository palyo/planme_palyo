package aurora.reminder.todolist.calendar.extension

import android.*
import android.app.*
import android.content.*
import android.content.pm.*
import android.os.*
import android.os.Build.VERSION.SDK_INT
import android.provider.*
import androidx.core.app.*

val STORAGE_PERMISSION = if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
else if (SDK_INT >= Build.VERSION_CODES.Q) arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

fun Activity.hasPermissions(permissions: Array<String>): Boolean = permissions.all { ActivityCompat.checkSelfPermission(applicationContext, it) == PackageManager.PERMISSION_GRANTED }

fun Activity.hasOverlayPermission(): Boolean {
    return Settings.canDrawOverlays(this)
}

fun Activity.hasAlarmPermission(): Boolean {
    if (SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    } else return true
}