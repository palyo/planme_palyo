package aurora.reminder.todolist.calendar.adapter

import android.content.*
import android.view.*
import androidx.recyclerview.widget.*
import aurora.reminder.todolist.calendar.*
import aurora.reminder.todolist.calendar.database.relations.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.model.*
import coder.apps.space.library.extension.*

class CompletedTaskAdapter(val context: Context, val listener: (TaskWithActivities) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var data: MutableList<TaskWithActivities> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TaskHolder(LayoutRowItemTaskCompletionBinding.inflate(inflater, parent, false))
    }

    fun updateData(data: MutableList<TaskWithActivities>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TaskHolder -> holder.bind(data[position], listener)
        }
    }

    override fun getItemCount(): Int = data.size

    inner class TaskHolder(var binding: LayoutRowItemTaskCompletionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(taskActivity: TaskWithActivities, listener: (TaskWithActivities) -> Unit) {
            binding.apply {
                val task = taskActivity.task
                val existedTaskActivity = taskActivity.activities.find { (it.taskType == TaskType.ONE_TIME.ordinal && task.id == it.taskId) }
                val isOnetime = taskActivity.task.taskType == TaskType.ONE_TIME.ordinal
                taskTitle.text = task.title
                if (task.body.isBlank()) taskBody.beGone()
                else taskBody.text = task.body
                taskDetails.text = root.context.getString(R.string.task_duration_schedule, calculateDuration(task.dailyStartTime, task.dailyEndTime), createDateRangeString(task.startDate, task.endDate))
                val completionPercent = (taskActivity.activities.size * 100) / (daysBetween(task.startDate, task.endDate) + 1)
                progressBar.setProgress(completionPercent.toInt(), true)
                taskCompletion.text = "${completionPercent.toInt()}%"
                layoutProgress.beInvisibleIf(isOnetime)
                isCompleted.beVisibleIf(isOnetime)
                isCompleted.isChecked = existedTaskActivity?.isCompleted == true
                root.setOnClickListener {
                    listener.invoke(taskActivity)
                }
            }
        }
    }
}


