package aurora.reminder.todolist.calendar.database.relations

import android.os.*
import androidx.room.*
import aurora.reminder.todolist.calendar.database.table.*
import kotlinx.parcelize.*

@Parcelize
data class TaskWithActivities(
    @Embedded
    val task: Task,
    @Relation(
        parentColumn = "tid",
        entityColumn = "task_id"
    )
    val activities: MutableList<TaskActivity>
) : Parcelable