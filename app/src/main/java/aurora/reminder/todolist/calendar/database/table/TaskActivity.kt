package aurora.reminder.todolist.calendar.database.table

import android.os.*
import androidx.room.*
import kotlinx.parcelize.*

@Parcelize
@Entity(
    tableName = "task_activity",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["tid"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["wid"],
            childColumns = ["workspace_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskActivity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "taid") var id: Int? = null,
    @ColumnInfo(name = "task_id") var taskId: Int,
    @ColumnInfo(name = "workspace_id") var workspaceId: Int,
    @ColumnInfo(name = "completion_date") var completionDate: Long,
    @ColumnInfo(name = "completion_time") var completionTime: Long,
    @ColumnInfo(name = "task_type") var taskType: Int,
    @ColumnInfo(name = "is_completed") var isCompleted: Boolean
) : Parcelable
