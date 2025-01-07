package aurora.reminder.todolist.calendar.view

import android.app.*
import android.graphics.*
import android.graphics.drawable.*
import android.view.*
import android.widget.*
import aurora.reminder.todolist.calendar.*
import aurora.reminder.todolist.calendar.activity.*
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.database.relations.*
import aurora.reminder.todolist.calendar.database.table.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.model.*
import coder.apps.space.library.extension.*
import kotlinx.coroutines.*

class TimelineTaskOptionsPopup(private val context: Activity) {
    private val popup = PopupWindow(context)

    fun show(tasks: MutableList<TaskWithActivities>?, currentDate: Long, task: TimelineTask, anchorView: View, touchX: Float, touchY: Float) {
        val popupView = LayoutPopupTaskOptionsBinding.inflate(LayoutInflater.from(context), null, false)
        val newTask = tasks?.find { task.id == it.task.id }
        newTask?.apply {
            popupView.taskTitle.text = task.title
            if (task.body.isBlank()) popupView.taskBody.beGone()
            else popupView.taskBody.text = task.body
            popupView.taskDetails.text = "${calculateDuration(this.task.dailyStartTime, this.task.dailyEndTime)} | ${createDateRangeString(this.task.startDate, this.task.endDate)}"
        }
        val existedTaskActivity = newTask?.activities?.find {
            (it.taskType == TaskType.DAILY_TASK.ordinal && task.id == it.taskId && it.completionDate == currentDate) ||
                    (it.taskType == TaskType.ONE_TIME.ordinal && task.id == it.taskId)
        }
        val isTaskCompleted = existedTaskActivity?.isCompleted == true

        popupView.buttonEdit.setOnClickListener {
            context.go(AddTaskActivity::class.java, listOf("task" to newTask?.task))
            popup.dismiss()
        }

        popupView.buttonDelete.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                newTask?.task?.apply {
                    context.taskDao.delete(this)
                    context.cancelReminder(taskId = id ?: 0)
                }
            }
            popup.dismiss()
        }

        popupView.buttonMark.text = if (isTaskCompleted) context.getString(R.string.menu_uncompleted) else context.getString(R.string.menu_completed)
        popupView.buttonMark.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                if (existedTaskActivity == null) {
                    newTask?.task?.apply {
                        context.activityDao.insert(
                            TaskActivity(
                                taskId = id ?: 0,
                                workspaceId = workspaceId,
                                completionDate = currentDate,
                                completionTime = (System.currentTimeMillis() - currentDate),
                                isCompleted = true,
                                taskType = taskType,
                            )
                        )
                        context.cancelReminder(taskId = id ?: 0)
                    }
                } else {
                    context.activityDao.delete(existedTaskActivity)
                    context.scheduleDailyReminders(newTask.task)
                }
            }
            popup.dismiss()
        }

        popup.apply {
            contentView = popupView.root
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            isOutsideTouchable = true
            elevation = context.resources.getDimension(com.intuit.sdp.R.dimen._4sdp)
            val location = IntArray(2)
            anchorView.getLocationOnScreen(location)
            val xPos = (location[0] + touchX).toInt()
            val yPos = (location[1] + touchY - height).toInt()

            showAtLocation(anchorView, Gravity.NO_GRAVITY, xPos, yPos)
        }
    }
}
