package aurora.reminder.todolist.calendar.activity

import android.os.*
import androidx.activity.*
import androidx.recyclerview.widget.*
import aurora.reminder.todolist.calendar.R
import aurora.reminder.todolist.calendar.adapter.*
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.database.table.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import coder.apps.space.library.base.*
import coder.apps.space.library.extension.*
import kotlinx.coroutines.*
import java.text.*
import java.util.*

class TrackTaskActivity : BaseActivity<ActivityTrackTaskBinding>(ActivityTrackTaskBinding::inflate) {
    private var taskAdapter: TrackTaskAdapter? = null
    private val timeSdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun ActivityTrackTaskBinding.initExtra() {
        setupAdapter()
        setupData()
    }

    private fun ActivityTrackTaskBinding.setupData() {
        val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("task", Task::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("task")
        }
        task?.apply {
            taskTitle.text = task.title
            if (task.body.isBlank()) taskBody.beGone()
            else taskBody.text = task.body
            taskDuration.text = getString(R.string.task_duration_time, calculateDuration(task.dailyStartTime, task.dailyEndTime), timeSdf.format(task.dailyStartTime), timeSdf.format(task.dailyEndTime))
            CoroutineScope(Dispatchers.IO).launch {
                val details = activityDao.fetchByTask(task.id ?: 0)
                val startDate = task.startDate
                val endDate = task.endDate
                val allDates = mutableListOf<Long>()
                val calendar = Calendar.getInstance()
                var currentDate = startDate
                while (currentDate <= endDate) {
                    allDates.add(currentDate)
                    calendar.timeInMillis = currentDate
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    currentDate = calendar.timeInMillis
                }
                val existingDates = details.map { it.completionDate }.toSet()
                val missingDates = allDates.filter { it !in existingDates }
                val dummyActivities = missingDates.map { missingDate ->
                    TaskActivity(
                        id = null,
                        taskId = task.id ?: 0,
                        workspaceId = task.workspaceId,
                        completionDate = missingDate,
                        completionTime = 0,
                        isCompleted = false,
                        taskType = task.taskType
                    )
                }
                val combinedData = (details + dummyActivities).sortedBy { it.completionDate }.toMutableList()

                runOnUiThread {
                    taskDetails.text = getString(R.string.task_schedule_remaining, createDateRangeString(task.startDate, task.endDate), "${(daysBetween(task.startDate, task.endDate) + 1) - details.size}")
                    taskAdapter?.updateData(combinedData)
                }
            }
        }
    }

    private fun ActivityTrackTaskBinding.setupAdapter() {
        recyclerViewTask.apply {
            taskAdapter = TrackTaskAdapter(this@TrackTaskActivity)
            layoutManager = LinearLayoutManager(this@TrackTaskActivity, LinearLayoutManager.VERTICAL, false)
            adapter = taskAdapter
        }
    }

    override fun ActivityTrackTaskBinding.initListeners() {

    }

    override fun ActivityTrackTaskBinding.initView() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        onBackPressedDispatcher.addCallback { finish() }
    }
}