package aurora.reminder.todolist.calendar.view

import android.os.Handler
import android.os.Looper
import java.util.*

class TimeUpdateHandler(private val timelineView: TimelineView) {
    private val handler = Handler(Looper.getMainLooper())
    private var isUpdating = false

    private val updateRunnable = object : Runnable {
        override fun run() {
            timelineView.invalidate()
            scheduleNextUpdate()
        }
    }

    fun startUpdates() {
        if (!isUpdating) {
            isUpdating = true
            scheduleNextUpdate()
        }
    }

    fun stopUpdates() {
        isUpdating = false
        handler.removeCallbacks(updateRunnable)
    }

    private fun scheduleNextUpdate() {
        if (!isUpdating) return

        val calendar = Calendar.getInstance(Locale.ENGLISH)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)
        val currentMillis = calendar.get(Calendar.MILLISECOND)

        // Calculate delay until next 10-minute mark
        val minutesToNext = 10 - (currentMinute % 10)
        val delayMillis = (minutesToNext * 60 * 1000 - 
                          currentSecond * 1000 - 
                          currentMillis).toLong()

        handler.postDelayed(updateRunnable, delayMillis)
    }
}