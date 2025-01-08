package aurora.reminder.todolist.calendar.viewmodel

import androidx.lifecycle.*
import aurora.reminder.todolist.calendar.database.dao.*
import aurora.reminder.todolist.calendar.database.relations.*
import aurora.reminder.todolist.calendar.extension.*
import java.util.*

class WorkspaceTaskViewModel(private val workspaceTaskDao: WorkspaceTaskDao) : ViewModel() {
    private val _startAndEndOfDay = MutableLiveData<Pair<Long, Long>>()
    private val _systemWorkspaceWithTask = MediatorLiveData<MutableList<WorkspaceWithTasks>>()
    val systemWorkspaceWithTask: LiveData<MutableList<WorkspaceWithTasks>> get() = _systemWorkspaceWithTask
    private val _personalWorkspaceWithTask = MediatorLiveData<MutableList<WorkspaceWithTasks>>()
    val personalWorkspaceWithTask: LiveData<MutableList<WorkspaceWithTasks>> get() = _personalWorkspaceWithTask
    private var sourceSystem: LiveData<MutableList<WorkspaceWithTasks>>? = null
    private var sourcePersonal: LiveData<MutableList<WorkspaceWithTasks>>? = null

    init {
        _startAndEndOfDay.value = fetchStartAndEndOfDay(Date().time)
        observeTaskDatabase()
    }

    private fun observeTaskDatabase() {
        _startAndEndOfDay.observeForever { range ->
            val (start, end) = range
            sourceSystem?.let { _systemWorkspaceWithTask.removeSource(it) }
            val newSource = workspaceTaskDao.findSystemWorkspacesWithTasks(start, end)
            sourceSystem = newSource
            _systemWorkspaceWithTask.addSource(newSource) { tasks ->
                val newTasks = groupWorkspacesById(tasks, start, end)
                _systemWorkspaceWithTask.value = newTasks
            }

            sourcePersonal?.let { _personalWorkspaceWithTask.removeSource(it) }
            val newSourcePersonal = workspaceTaskDao.findPersonalWorkspacesWithTasks(start, end)
            sourcePersonal = newSourcePersonal
            _personalWorkspaceWithTask.addSource(newSourcePersonal) { tasks ->
                val newTasks = groupWorkspacesById(tasks, start, end)
                _personalWorkspaceWithTask.value = newTasks
            }
        }
    }

    private fun groupWorkspacesById(
        workspaceList: MutableList<WorkspaceWithTasks>,
        startOfDay: Long,
        endOfDay: Long
    ): MutableList<WorkspaceWithTasks> {
        val groupedMap = mutableMapOf<Int, WorkspaceWithTasks>()

        for (workspaceWithTasks in workspaceList) {
            val workspaceId = workspaceWithTasks.workspace.id ?: continue  // Skip if workspace id is null
            // Filter tasks based on the desired date range
            val filteredTasks = workspaceWithTasks.tasks.filter { taskWithActivities ->
                val task = taskWithActivities.task
                // Check if the task's start and end dates fall within the date range
                (task.startDate <= endOfDay && (task.endDate >= startOfDay || task.endDate == null))
            }.distinctBy { it.task.id }  // Remove duplicate tasks based on task id

            if (filteredTasks.isNotEmpty()) {
                // Create a new WorkspaceWithTasks object with filtered tasks
                val newWorkspaceWithTasks = workspaceWithTasks.copy(tasks = filteredTasks.toMutableList())
                val existingWorkspace = groupedMap[workspaceId]

                if (existingWorkspace != null) {
                    // If the workspace exists, add non-duplicate tasks to the existing list
                    existingWorkspace.tasks.addAll(filteredTasks.filter { newTask ->
                        // Check if the task doesn't already exist in the existing workspace's task list
                        existingWorkspace.tasks.none { it.task.id == newTask.task.id }
                    })
                } else {
                    // If the workspace doesn't exist, add a new entry
                    groupedMap[workspaceId] = newWorkspaceWithTasks
                }
            } else {
                // If no tasks match the range, just add the workspace with an empty task list
                val emptyWorkspaceWithTasks = workspaceWithTasks.copy(tasks = mutableListOf())
                groupedMap[workspaceId] = emptyWorkspaceWithTasks
            }
        }
        // Return the list of workspaces grouped by ID
        return groupedMap.values.toMutableList()
    }

    fun changeDateForTasks(newStartDate: Long) {
        _startAndEndOfDay.value = fetchStartAndEndOfDay(newStartDate)
    }
}

class WorkspaceTaskViewModelFactory(private val workspaceTaskDao: WorkspaceTaskDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkspaceTaskViewModel::class.java)) {
            return WorkspaceTaskViewModel(workspaceTaskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
