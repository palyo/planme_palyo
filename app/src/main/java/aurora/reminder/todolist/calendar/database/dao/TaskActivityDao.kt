package aurora.reminder.todolist.calendar.database.dao

import androidx.room.*
import aurora.reminder.todolist.calendar.database.relations.*

@Dao
interface TaskActivityDao {
    @Transaction
    @Query("SELECT * FROM task")
    fun findTaskWithActivities(): MutableList<TaskWithActivities>
}