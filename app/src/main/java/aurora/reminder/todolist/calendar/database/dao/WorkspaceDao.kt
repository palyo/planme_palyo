package aurora.reminder.todolist.calendar.database.dao

import androidx.room.*
import aurora.reminder.todolist.calendar.database.table.*

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspace")
    fun fetchAll(): MutableList<Workspace>

    @Query("SELECT * FROM workspace WHERE is_system_space = 1")
    fun fetchSystem(): MutableList<Workspace>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workspace: Workspace)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(workspaces: MutableList<Workspace>)

    @Update
    suspend fun update(workspace: Workspace)

    @Delete
    suspend fun delete(workspace: Workspace)

    @Query("DELETE FROM workspace")
    suspend fun deleteAll()
}