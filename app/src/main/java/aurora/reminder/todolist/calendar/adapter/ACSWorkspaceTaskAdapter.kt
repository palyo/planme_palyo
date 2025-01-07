package aurora.reminder.todolist.calendar.adapter

import android.content.*
import android.content.res.*
import android.graphics.*
import android.view.*
import androidx.core.content.*
import androidx.core.view.*
import androidx.recyclerview.widget.*
import aurora.reminder.todolist.calendar.*
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.database.relations.*
import aurora.reminder.todolist.calendar.database.table.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.model.*
import kotlinx.coroutines.*
import java.util.*

class ACSWorkspaceTaskAdapter(val context: Context, val isSystem: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var data: MutableList<Any> = mutableListOf()
    private var startOfDay: Long = 0L

    init {
        startOfDay = fetchStartAndEndOfDay(Date().time).first
    }

    fun updateSelectedDate(time: Long) {
        startOfDay = fetchStartAndEndOfDay(time).first
    }

    companion object {
        private const val VIEW_TYPE_WORKSPACE = 0
        private const val VIEW_TYPE_TASK = 1
        private const val VIEW_TYPE_EMPTY = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is Workspace -> VIEW_TYPE_WORKSPACE
            is TaskWithActivities -> VIEW_TYPE_TASK
            is String -> VIEW_TYPE_EMPTY
            else -> 2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_WORKSPACE -> WorkspaceHolder(LayoutRowItemAcsWorkspaceTitleBinding.inflate(inflater, parent, false))
            VIEW_TYPE_TASK -> TaskHolder(LayoutRowItemAcsTaskCheckTitleBinding.inflate(inflater, parent, false))
            VIEW_TYPE_EMPTY -> EmptyHolder(LayoutRowItemAcsEmptyBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    fun updateData(data: MutableList<Any>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WorkspaceHolder -> holder.bind(data[position] as Workspace, position)
            is TaskHolder -> holder.bind(data[position] as TaskWithActivities)
            is EmptyHolder -> holder.bind(data[position] as String)
        }
    }

    override fun getItemCount(): Int = data.size

    inner class TaskHolder(var binding: LayoutRowItemAcsTaskCheckTitleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(taskActivity: TaskWithActivities) {
            binding.apply {
                val task = taskActivity.task
                val existedTaskActivity = taskActivity.activities.find {
                    (it.taskType == TaskType.DAILY_TASK.ordinal && task.id == it.taskId && it.completionDate == startOfDay) ||
                            (it.taskType == TaskType.ONE_TIME.ordinal && task.id == it.taskId)
                }
                val isTaskCompleted = existedTaskActivity?.isCompleted == true
                isCompleted.isChecked = isTaskCompleted
                taskTitle.text = task.title
                taskTitle.paintFlags = if (isTaskCompleted) taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
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
                isCompleted.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(root.context, if (isSystem) R.color.colorAccentTool else R.color.colorInverseText))
                taskTitle.setTextColor(ContextCompat.getColor(root.context, if (isSystem) R.color.colorAccentTool else R.color.colorInverseText))
            }
        }
    }

    inner class WorkspaceHolder(var binding: LayoutRowItemAcsWorkspaceTitleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(workspace: Workspace, position: Int) {
            binding.apply {
                if (position == 0) card.updatePadding(top = 0)
                workspaceTitle.text = workspace.title
                workspaceTitle.setTextColor(ContextCompat.getColor(root.context, if (isSystem) R.color.colorAccentTool else R.color.colorInverseText))
            }
        }
    }

    inner class EmptyHolder(var binding: LayoutRowItemAcsEmptyBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.apply {
                emptyTitle.text = "No task added"
                emptyTitle.setTextColor(ContextCompat.getColor(root.context, if (isSystem) R.color.colorAccentTool else R.color.colorInverseText))
            }
        }
    }
}


