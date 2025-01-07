package aurora.reminder.todolist.calendar.database.table

import android.os.*
import androidx.room.*
import kotlinx.parcelize.*

@Parcelize
@Entity(
    tableName = "task",
    foreignKeys = [
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["wid"],
            childColumns = ["workspace_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tid") var id: Int? = null,
    @ColumnInfo(name = "ttitle") var title: String,
    @ColumnInfo(name = "tbody") var body: String,
    @ColumnInfo(name = "start_date") var startDate: Long,
    @ColumnInfo(name = "end_date") var endDate: Long,
    @ColumnInfo(name = "daily_start_time") var dailyStartTime: Long,
    @ColumnInfo(name = "daily_end_time") var dailyEndTime: Long,
    @ColumnInfo(name = "workspace_id") var workspaceId: Int,
    @ColumnInfo(name = "task_type") var taskType: Int,
    @ColumnInfo(name = "is_complete") var isComplete: Boolean,
) : Parcelable
