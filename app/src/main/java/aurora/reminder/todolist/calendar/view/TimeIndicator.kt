package aurora.reminder.todolist.calendar.view

import android.graphics.*
import androidx.core.content.*
import aurora.reminder.todolist.calendar.*
import java.util.*

class TimeIndicator(private val timelineView: TimelineView) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(timelineView.context, R.color.colorAccent)
        strokeWidth = 4f
    }

    fun draw(canvas: Canvas) {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        // Calculate position of the time indicator
        val x = timelineView.sideMargin + calculateTimePosition(currentHour, currentMinute)
        // Draw the circle (handle) at the top of the line
        val circleRadius = 10f
        canvas.drawCircle(x, timelineView.timelineHeight + 10f, circleRadius, paint)
        // Draw the line from the bottom of the circle to the bottom of the timeline
        canvas.drawLine(x, timelineView.timelineHeight, x, timelineView.height.toFloat(), paint)
    }

    private fun calculateTimePosition(hour: Int, minute: Int): Float {
        return hour * timelineView.hourWidth + (minute / 60f) * timelineView.hourWidth
    }
}
