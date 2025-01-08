package aurora.reminder.todolist.calendar.viewmodel

import androidx.lifecycle.*
import aurora.reminder.todolist.calendar.database.dao.*
import aurora.reminder.todolist.calendar.database.relations.*
import aurora.reminder.todolist.calendar.extension.*
import java.util.*

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {
    private val _startAndEndOfDay = MutableLiveData<Pair<Long, Long>>()
    private val _tasks = MediatorLiveData<MutableList<TaskWithActivities>>()
    val tasks: LiveData<MutableList<TaskWithActivities>> get() = _tasks
    private val _taskActivities = MediatorLiveData<MutableList<TaskWithActivities>>()
    val taskActivities: LiveData<MutableList<TaskWithActivities>> get() = _taskActivities
    private var source: LiveData<MutableList<TaskWithActivities>>? = null
    private var sourceTaskActivities: LiveData<MutableList<TaskWithActivities>>? = null

    init {
        _startAndEndOfDay.value = fetchStartAndEndOfDay(Date().time)
        observeDatabase()
        observeTaskDatabase()
    }

    private fun observeDatabase() {
        _startAndEndOfDay.observeForever { range ->
            source?.let { _tasks.removeSource(it) }
            val newSource = taskDao.fetchAll()
            source = newSource
            _tasks.addSource(newSource) { tasks ->
                _tasks.value = tasks
            }
        }
    }

    private fun observeTaskDatabase() {
        _startAndEndOfDay.observeForever { range ->
            val (start, end) = range
            sourceTaskActivities?.let { _taskActivities.removeSource(it) }
            val newSource = taskDao.fetchTodayTasksWithActivities(start, end)
            sourceTaskActivities = newSource
            _taskActivities.addSource(newSource) { tasks ->
                _taskActivities.value = tasks
            }
        }
    }

    fun changeDateForTasks(newStartDate: Long) {
        _startAndEndOfDay.value = fetchStartAndEndOfDay(newStartDate)
    }
}

class TaskViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
