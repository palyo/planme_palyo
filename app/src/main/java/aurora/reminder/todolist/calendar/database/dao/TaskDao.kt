package aurora.reminder.todolist.calendar.database.dao

import androidx.lifecycle.*
import androidx.room.*
import aurora.reminder.todolist.calendar.database.relations.*
import aurora.reminder.todolist.calendar.database.table.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM task")
    fun fetchAll(): LiveData<MutableList<TaskWithActivities>>

    @Query("SELECT * FROM task WHERE start_date <= :endOfDay AND end_date >= :startOfDay")
    fun fetchTodayTasks(startOfDay: Long, endOfDay: Long): MutableList<Task>

    @Query("SELECT * FROM task WHERE start_date <= :endOfDay AND end_date >= :startOfDay")
    fun fetchTodayTasksWithActivities(startOfDay: Long, endOfDay: Long): LiveData<MutableList<TaskWithActivities>>

    @Query("SELECT * FROM task WHERE start_date <= :endOfDay AND end_date >= :startOfDay")
    fun fetchTodayTasksLive(startOfDay: Long, endOfDay: Long): LiveData<MutableList<Task>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tasks: MutableList<Task>)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task")
    suspend fun deleteAll()
}