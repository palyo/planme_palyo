package aurora.reminder.todolist.calendar.database.dao

import androidx.lifecycle.*
import androidx.room.*
import aurora.reminder.todolist.calendar.database.relations.*

@Dao
interface WorkspaceTaskDao {
    @Transaction
    @Query(
        """
    SELECT DISTINCT w.*, t.*, ta.*
    FROM workspace w
    LEFT JOIN task t 
        ON w.wid = t.workspace_id 
        AND (t.start_date <= :endOfDay AND (t.end_date >= :startOfDay OR t.end_date IS NULL))
    LEFT JOIN task_activity ta 
        ON t.tid = ta.task_id
    WHERE w.is_system_space = 1
    ORDER BY w.wid
"""
    )
    fun findSystemWorkspacesWithTasks(
        startOfDay: Long,
        endOfDay: Long
    ): LiveData<MutableList<WorkspaceWithTasks>>

    @Transaction
    @Query(
        """
    SELECT DISTINCT w.*, t.*, ta.*
    FROM workspace w
    LEFT JOIN task t 
        ON w.wid = t.workspace_id 
        AND (t.start_date <= :endOfDay AND (t.end_date >= :startOfDay OR t.end_date IS NULL))
    LEFT JOIN task_activity ta 
        ON t.tid = ta.task_id
    WHERE w.is_system_space = 0
    ORDER BY w.wid
"""
    )
    fun findPersonalWorkspacesWithTasks(
        startOfDay: Long,
        endOfDay: Long
    ): LiveData<MutableList<WorkspaceWithTasks>>

    @Transaction
    @Query(
        """
    SELECT DISTINCT w.*, t.*, ta.*
    FROM workspace w
    LEFT JOIN task t 
        ON w.wid = t.workspace_id 
        AND (t.start_date <= :endOfDay AND (t.end_date >= :startOfDay OR t.end_date IS NULL))
    LEFT JOIN task_activity ta 
        ON t.tid = ta.task_id
    WHERE w.is_system_space = 1
    ORDER BY w.wid
"""
    )
    fun findSystemWorkspacesWithTasksNotLive(
        startOfDay: Long,
        endOfDay: Long
    ): MutableList<WorkspaceWithTasks>

    @Transaction
    @Query(
        """
    SELECT DISTINCT w.*, t.*, ta.*
    FROM workspace w
    LEFT JOIN task t 
        ON w.wid = t.workspace_id 
        AND (t.start_date <= :endOfDay AND (t.end_date >= :startOfDay OR t.end_date IS NULL))
    LEFT JOIN task_activity ta 
        ON t.tid = ta.task_id
    WHERE w.is_system_space = 0
    ORDER BY w.wid
"""
    )
    fun findPersonalWorkspacesWithTasksNotLive(
        startOfDay: Long,
        endOfDay: Long
    ): MutableList<WorkspaceWithTasks>
}
