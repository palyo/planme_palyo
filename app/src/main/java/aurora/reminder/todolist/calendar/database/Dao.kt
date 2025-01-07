package aurora.reminder.todolist.calendar.database

import android.content.*
import aurora.reminder.todolist.calendar.database.dao.*

val Context.workspaceDao: WorkspaceDao get() = PlannerDatabase.getDatabase(applicationContext).workspaceDao()
val Context.taskDao: TaskDao get() = PlannerDatabase.getDatabase(applicationContext).taskDao()
val Context.workspaceTaskDao: WorkspaceTaskDao get() = PlannerDatabase.getDatabase(applicationContext).workspaceTaskDao()
val Context.activityDao: ActivityDao get() = PlannerDatabase.getDatabase(applicationContext).activityDao()
val Context.taskActivityDao: TaskActivityDao get() = PlannerDatabase.getDatabase(applicationContext).taskActivityDao()

