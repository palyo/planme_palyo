package aurora.reminder.todolist.calendar.database.relations

import android.os.*
import androidx.room.*
import aurora.reminder.todolist.calendar.database.table.*
import kotlinx.parcelize.*

@Parcelize
data class WorkspaceWithTasks(
    @Embedded
    val workspace: Workspace,
    @Relation(
        entity = Task::class,
        parentColumn = "wid",
        entityColumn = "workspace_id"
    )
    val tasks: MutableList<TaskWithActivities>
) : Parcelable