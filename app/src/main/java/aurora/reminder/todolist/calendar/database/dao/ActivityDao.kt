package aurora.reminder.todolist.calendar.database.dao

import androidx.room.*
import aurora.reminder.todolist.calendar.database.table.*

@Dao
interface ActivityDao {
    @Query("SELECT * FROM task_activity")
    fun fetchAll(): MutableList<TaskActivity>

    @Query("SELECT * FROM task_activity WHERE task_id = :taskId")
    fun fetchByTask(taskId: Int): MutableList<TaskActivity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: TaskActivity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tasks: MutableList<TaskActivity>)

    @Update
    suspend fun update(task: TaskActivity)

    @Query("UPDATE task_activity SET is_completed = :isCompleted WHERE task_id = :taskId AND  completion_date = :date")
    suspend fun updateIsCompleted(taskId: Int, date: Long, isCompleted: Boolean)

    @Delete
    suspend fun delete(task: TaskActivity)

    @Query("DELETE FROM task_activity")
    suspend fun deleteAll()
}