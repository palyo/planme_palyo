package aurora.reminder.todolist.calendar.calldorado

import android.content.*
import android.view.*
import androidx.recyclerview.widget.*
import aurora.reminder.todolist.calendar.activity.*
import aurora.reminder.todolist.calendar.adapter.*
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.database.relations.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import com.calldorado.ui.aftercall.*
import com.calldorado.ui.views.custom.*
import kotlinx.coroutines.*
import java.util.*

class AfterCallCustomView(private var mContext: Context) : CalldoradoCustomView(mContext) {
    private var systemWorkspaceTaskAdapter: ACSWorkspaceTaskAdapter? = null
    private var personalWorkspaceTaskAdapter: ACSWorkspaceTaskAdapter? = null

    override fun getRootView(): View {
        val binding = LayoutAftercallNativeBinding.inflate(LayoutInflater.from(mContext), linearViewGroup, false)
        binding.initView()
        return binding.root
    }

    private fun LayoutAftercallNativeBinding.initView() {
        recyclerViewWorkspaceSystem.apply {
            systemWorkspaceTaskAdapter = ACSWorkspaceTaskAdapter(mContext, true)
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            adapter = systemWorkspaceTaskAdapter
        }

        recyclerViewWorkspaceUser.apply {
            personalWorkspaceTaskAdapter = ACSWorkspaceTaskAdapter(mContext, false)
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            adapter = personalWorkspaceTaskAdapter
        }
        CoroutineScope(Dispatchers.IO).launch {
            val (start, end) = fetchStartAndEndOfDay(Date().time)
            val systems = mContext.workspaceTaskDao.findSystemWorkspacesWithTasksNotLive(start, end)
            val personals = mContext.workspaceTaskDao.findPersonalWorkspacesWithTasksNotLive(start, end)
            val newSystems = groupWorkspacesById(systems, start, end)
            val newPersonals = groupWorkspacesById(personals, start, end)
            val systemWorkspaceTasks = mutableListOf<Any>()
            newSystems.forEach {
                systemWorkspaceTasks.add(it.workspace)
                if (it.tasks.isNotEmpty()) {
                    it.tasks.sortBy { it.task.dailyStartTime }
                    it.tasks.forEach { task ->
                        systemWorkspaceTasks.add(task)
                    }
                } else {
                    systemWorkspaceTasks.add(EMPTY_HOLDER)
                }
            }
            launch(Dispatchers.IO) {
                systemWorkspaceTaskAdapter?.updateData(systemWorkspaceTasks)
            }
            val personalWorkspaceTasks = mutableListOf<Any>()
            newPersonals.forEach {
                personalWorkspaceTasks.add(it.workspace)
                if (it.tasks.isNotEmpty()) {
                    it.tasks.sortBy { it.task.dailyStartTime }
                    it.tasks.forEach { task ->
                        personalWorkspaceTasks.add(task)
                    }
                } else {
                    personalWorkspaceTasks.add(EMPTY_HOLDER)
                }
            }
            launch(Dispatchers.IO) {
                personalWorkspaceTaskAdapter?.updateData(personalWorkspaceTasks)
            }
        }

        buttonCreate.setOnClickListener {
            createNewTask()
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

    private fun createNewTask() {
        Intent(mContext, AddTaskActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("isCallAfter", true)
            mContext.startActivity(this)
        }
        if (calldoradoContext is CallerIdActivity) {
            (calldoradoContext as CallerIdActivity).finish()
        }
    }
}