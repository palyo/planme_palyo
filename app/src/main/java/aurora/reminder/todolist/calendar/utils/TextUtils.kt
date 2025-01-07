package aurora.reminder.todolist.calendar.utils

import android.graphics.Paint
import android.text.TextPaint
import android.text.TextUtils

object TextUtils {
    fun ellipsizeText(text: String, paint: Paint, maxWidth: Float): String {
        return TextUtils.ellipsize(
            text,
            TextPaint(paint),
            maxWidth,
            TextUtils.TruncateAt.END
        ).toString()
    }

    fun formatTime(time: TimelineTime): String {
        val period = if (time.hour < 12) "AM" else "PM"
        val displayHour = when {
            time.hour == 0 -> 12
            time.hour > 12 -> time.hour - 12
            else -> time.hour
        }
        return if (time.minute == 0) {
            String.format("%d:00 %s", displayHour, period)
        } else {
            String.format("%d:%02d %s", displayHour, time.minute, period)
        }
    }
}