package aurora.reminder.todolist.calendar.view

import aurora.reminder.todolist.calendar.model.*

interface TimelineTaskClickListener {
    fun onTaskClick(task: TimelineTask, x: Float, y: Float)
}