package aurora.reminder.todolist.calendar.utils

import aurora.reminder.todolist.calendar.model.*

object TimelineUtils {

    fun createTask(
        id: Int,
        title: String,
        body: String,
        timestamp: String,
        startTime: String,
        endTime: String,
        color: Int,
        isCompleted: Boolean,
    ): TimelineTask {
        return TimelineTask(
            id = id,
            title = title,
            body = body,
            timestamp = timestamp,
            startTime = TimelineTime.fromString(startTime),
            endTime = TimelineTime.fromString(endTime),
            color = color,
            isCompleted = isCompleted,
        )
    }
}