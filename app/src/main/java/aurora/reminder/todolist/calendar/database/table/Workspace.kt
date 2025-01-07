package aurora.reminder.todolist.calendar.database.table

import android.os.*
import androidx.room.*
import kotlinx.parcelize.*

@Parcelize
@Entity(tableName = "workspace", indices = [Index(value = ["workspace_title"], unique = true)])
data class Workspace(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "wid") var id: Int? = null,
    @ColumnInfo(name = "workspace_title") var title: String?,
    @ColumnInfo(name = "is_system_space") var isSystemSpace: Boolean? = false
) : Parcelable
