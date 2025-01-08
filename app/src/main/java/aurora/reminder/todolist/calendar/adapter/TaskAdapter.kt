package aurora.reminder.todolist.calendar.adapter

import android.content.*
import android.graphics.*
import android.view.*
import androidx.recyclerview.widget.*
import aurora.reminder.todolist.calendar.*
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.database.relations.*
import aurora.reminder.todolist.calendar.database.table.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.model.*
import coder.apps.space.library.extension.*
import kotlinx.coroutines.*
import java.util.*

class TaskAdapter(val context: Context, val listener: (Task) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var startOfDay: Long = 0L
    private var data: MutableList<TaskWithActivities> = mutableListOf()

    init {
        startOfDay = fetchStartAndEndOfDay(Date().time).first
    }

    fun updateSelectedDate(time: Long) {
        startOfDay = fetchStartAndEndOfDay(time).first
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TaskHolder(LayoutRowItemTaskBinding.inflate(inflater, parent, false))
    }

    fun updateData(data: MutableList<TaskWithActivities>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TaskHolder -> holder.bind(data[position])
        }
    }

    override fun getItemCount(): Int = data.size

    inner class TaskHolder(var binding: LayoutRowItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(taskActivity: TaskWithActivities) {
            binding.apply {
                val task = taskActivity.task
                val existedTaskActivity = taskActivity.activities.find {
                    (it.taskType == TaskType.DAILY_TASK.ordinal && task.id == it.taskId && it.completionDate == startOfDay) ||
                            (it.taskType == TaskType.ONE_TIME.ordinal && task.id == it.taskId)
                }
                val isTaskCompleted = existedTaskActivity?.isCompleted == true
                taskTitle.text = task.title
                if (task.body.isBlank()) taskBody.beGone()
                else taskBody.text = task.body
                isCompleted.isChecked = isTaskCompleted
                taskDetails.text = root.context.getString(R.string.task_duration_schedule, calculateDuration(task.dailyStartTime, task.dailyEndTime), createDateRangeString(task.startDate, task.endDate))
                taskTitle.paintFlags = if (isTaskCompleted) taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                taskBody.paintFlags = if (isTaskCompleted) taskBody.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else taskBody.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                isCompleted.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.isPressed) {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (existedTaskActivity == null) {
                                context.activityDao.insert(
                                    TaskActivity(
                                        taskId = task.id ?: 0,
                                        workspaceId = task.workspaceId,
                                        completionDate = startOfDay,
                                        completionTime = (System.currentTimeMillis() - startOfDay),
                                        isCompleted = isChecked,
                                        taskType = task.taskType,
                                    )
                                )
                                context.cancelReminder(taskId = task.id ?: 0)
                            } else {
                                context.activityDao.delete(existedTaskActivity)
                                context.scheduleDailyReminders(task)
                            }
                        }
                    }
                }
                root.setOnClickListener {
                    listener.invoke(task)
                }
            }
        }
    }
}


