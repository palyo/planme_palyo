package aurora.reminder.todolist.calendar.view

import android.view.*
import kotlin.math.*

class TimelineScaleGestureDetector(
    private val timelineView: TimelineView,
    private val timelineHourView: TimelineHourView
) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    private var MIN_HOUR_WIDTH = 60f
    private var MAX_HOUR_WIDTH = 300f

    init {
        MIN_HOUR_WIDTH = timelineView.context.resources.getDimension(com.intuit.sdp.R.dimen._84sdp)
        MAX_HOUR_WIDTH = timelineView.context.resources.getDimension(com.intuit.sdp.R.dimen._220sdp)
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scaleFactor = detector.scaleFactor
        // Calculate new hour width
        val newHourWidth = timelineView.hourWidth * scaleFactor
        // Constrain the hour width between min and max values
        val constrainedHourWidth = max(MIN_HOUR_WIDTH, min(newHourWidth, MAX_HOUR_WIDTH))
        // Update both views with the new hour width
        timelineView.apply {
            hourWidth = constrainedHourWidth
            sideMargin = hourWidth / 2
            requestLayout()
            invalidate()
        }

        timelineHourView.apply {
            hourWidth = constrainedHourWidth
            sideMargin = hourWidth / 2
            requestLayout()
            invalidate()
        }

        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        // Clean up if needed
    }
}