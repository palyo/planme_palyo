package aurora.reminder.todolist.calendar.model

import aurora.reminder.todolist.calendar.utils.*

data class TimelineTask(
    val id: Int,
    val title: String,
    val body: String,
    val timestamp: String,
    val startTime: TimelineTime,
    val endTime: TimelineTime,
    val color: Int,
    val isCompleted: Boolean,
)