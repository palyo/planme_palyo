package aurora.reminder.todolist.calendar.activity

import android.os.*
import android.text.*
import android.widget.*
import androidx.activity.*
import androidx.lifecycle.*
import aurora.reminder.todolist.calendar.R
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.database.table.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.model.*
import coder.apps.space.library.base.*
import coder.apps.space.library.extension.*
import com.google.android.material.datepicker.*
import com.google.android.material.timepicker.*
import kotlinx.coroutines.*
import java.text.*
import java.util.*

class AddTaskActivity : BaseActivity<ActivityAddTaskBinding>(ActivityAddTaskBinding::inflate) {
    private var task: Task? = null
    private val dateSdf = SimpleDateFormat("dd MMM", Locale.ENGLISH)
    private val timeSdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val calendar = Calendar.getInstance(Locale.ENGLISH)
    private val currentTimeOffsetInMillis: Long = calendar.run {
        get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000L + get(Calendar.MINUTE) * 60 * 1000L + get(Calendar.SECOND) * 1000L + get(Calendar.MILLISECOND)
    }
    private val startOfTodayInMillis: Long = calendar.run {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }
    private val oneDayInMillis = 24 * 60 * 60 * 1000L
    private var selectedDueDateInMillis: Long = startOfTodayInMillis
    private var selectedEndDateInMillis: Long = startOfTodayInMillis + oneDayInMillis
    private var selectedStartTimeInMillis: Long = currentTimeOffsetInMillis
    private var selectedEndTimeInMillis: Long = currentTimeOffsetInMillis + (1 * 60 * 60 * 1000L)
    private var selectedWorkSpaceIndex: Int = -1
    private var workspaces: MutableList<Workspace>? = null

    override fun ActivityAddTaskBinding.initExtra() {
        task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("task", Task::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("task")
        }
        editTitle.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        editBody.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        if (task == null) {
            editDueDate.setText(dateSdf.format(selectedDueDateInMillis))
            editEndDate.setText(dateSdf.format(selectedEndDateInMillis))
            editStartTime.setText(timeSdf.format(selectedStartTimeInMillis))
            editEndTime.setText(timeSdf.format(selectedEndTimeInMillis))
            buttonDaily.isSelected = true
            buttonOnetime.isSelected = false
        } else {
            task?.let {
                buttonCreate.text = getString(R.string.button_update_task)
                toolbar.title = getString(R.string.button_update_task)
                selectedDueDateInMillis = it.startDate
                selectedEndDateInMillis = it.endDate
                selectedStartTimeInMillis = it.dailyStartTime
                selectedEndTimeInMillis = it.dailyEndTime
                editDueDate.setText(dateSdf.format(selectedDueDateInMillis))
                editEndDate.setText(dateSdf.format(selectedEndDateInMillis))
                editStartTime.setText(timeSdf.format(selectedStartTimeInMillis))
                editEndTime.setText(timeSdf.format(selectedEndTimeInMillis))
                editTitle.setText(it.title)
                editBody.setText(it.body)
                buttonDaily.isSelected = it.taskType == TaskType.DAILY_TASK.ordinal
                buttonOnetime.isSelected = it.taskType == TaskType.ONE_TIME.ordinal
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.setupWorkspace()
    }

    private fun ActivityAddTaskBinding.setupWorkspace() {
        lifecycleScope.launch(Dispatchers.IO) {
            workspaces = workspaceDao.fetchAll()
            launch(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@AddTaskActivity, R.layout.item_dropdown_workspace_title, workspaces?.map { it.title } ?: mutableListOf())
                editWorkspace.setAdapter(adapter)
                if (task != null) {
                    selectedWorkSpaceIndex = (workspaces?.find { (task?.workspaceId ?: 0) == it.id }?.id ?: 0) - 1
                    editWorkspace.setText(editWorkspace.adapter.getItem(selectedWorkSpaceIndex).toString(), false)
                }
            }
        }
        editWorkspace.setDropDownBackgroundResource(R.drawable.ic_bg_rounded_12sdp)
        editWorkspace.setOnItemClickListener { parent, view, position, id ->
            selectedWorkSpaceIndex = position
        }
    }

    override fun ActivityAddTaskBinding.initListeners() {
        buttonDaily.setOnClickListener {
            buttonDaily.isSelected = true
            buttonOnetime.isSelected = false
        }
        buttonOnetime.setOnClickListener {
            buttonDaily.isSelected = false
            buttonOnetime.isSelected = true
        }
        editDueDate.setOnClickListener {
            viewDatePicker(selectedMillis = selectedDueDateInMillis, startAt = startOfTodayInMillis, title = R.string.label_due_date) {
                val cal = Calendar.getInstance(Locale.ENGLISH)
                cal.timeInMillis = it
                editDueDate.setText(dateSdf.format(cal.time))
                selectedDueDateInMillis = it
                if (selectedDueDateInMillis > selectedEndDateInMillis) {
                    selectedEndDateInMillis = it
                    editEndDate.setText(dateSdf.format(cal.time))
                }
            }
        }
        editEndDate.setOnClickListener {
            viewDatePicker(selectedMillis = selectedDueDateInMillis, startAt = startOfTodayInMillis, title = R.string.label_end_date) {
                val cal = Calendar.getInstance(Locale.ENGLISH)
                cal.timeInMillis = it
                editEndDate.setText(dateSdf.format(cal.time))
                selectedEndDateInMillis = it
                if (selectedDueDateInMillis > selectedEndDateInMillis) {
                    selectedDueDateInMillis = it
                    editDueDate.setText(dateSdf.format(cal.time))
                }
            }
        }

        editStartTime.setOnClickListener {
            viewTimePicker(selectedStartTimeInMillis, R.string.label_start_time) { selectedTime ->
                selectedStartTimeInMillis = selectedTime
                editStartTime.setText(timeSdf.format(selectedStartTimeInMillis))
                if (selectedEndTimeInMillis <= selectedStartTimeInMillis) {
                    if (isLateNight(selectedStartTimeInMillis)) {
                        selectedEndTimeInMillis = getEndOfDayMillis(selectedStartTimeInMillis)
                    } else {
                        selectedEndTimeInMillis = selectedStartTimeInMillis + (1 * 60 * 60 * 1000L)
                    }
                }

                editEndTime.setText(timeSdf.format(selectedEndTimeInMillis))
            }
        }

        editEndTime.setOnClickListener {
            viewTimePicker(selectedEndTimeInMillis, R.string.label_end_time) { selectedTime ->
                selectedEndTimeInMillis = selectedTime
                if (selectedEndTimeInMillis <= selectedStartTimeInMillis) {
                    selectedEndTimeInMillis = if (isLateNight(selectedEndTimeInMillis)) {
                        getEndOfDayMillis(selectedStartTimeInMillis)
                    } else {
                        selectedStartTimeInMillis + (1 * 60 * 60 * 1000L)
                    }
                }

                editEndTime.setText(timeSdf.format(selectedEndTimeInMillis))
            }
        }

        buttonCreate.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (selectedWorkSpaceIndex == -1) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@AddTaskActivity, getString(R.string.message_assign_workspace), Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                if (editTitle.text.toString().trim().isEmpty()) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@AddTaskActivity, getString(R.string.message_validation_title), Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val type = if (buttonOnetime.isSelected) TaskType.ONE_TIME.ordinal else TaskType.DAILY_TASK.ordinal
                if (task != null) {
                    cancelReminder(taskId = task?.id ?: 0)
                    val newTask = Task(
                        id = task?.id,
                        title = editTitle.text.toString().trim(),
                        body = editBody.text.toString().trim(),
                        startDate = selectedDueDateInMillis,
                        endDate = selectedEndDateInMillis,
                        dailyStartTime = selectedStartTimeInMillis,
                        dailyEndTime = selectedEndTimeInMillis,
                        workspaceId = workspaces?.get(selectedWorkSpaceIndex)?.id ?: 0,
                        taskType = type,
                        isComplete = false
                    )
                    taskDao.update(newTask)
                    scheduleDailyReminders(newTask)
                } else {
                    val newTask = Task(
                        title = editTitle.text.toString().trim(),
                        body = editBody.text.toString().trim(),
                        startDate = selectedDueDateInMillis,
                        endDate = selectedEndDateInMillis,
                        dailyStartTime = selectedStartTimeInMillis,
                        dailyEndTime = selectedEndTimeInMillis,
                        workspaceId = workspaces?.get(selectedWorkSpaceIndex)?.id ?: 0,
                        taskType = type,
                        isComplete = false
                    )
                    taskDao.insert(newTask)
                    scheduleDailyReminders(newTask)
                }
                if (intent?.getBooleanExtra("isCallAfter", false) == true) {
                    go(PlannerActivity::class.java, finish = true)
                } else {
                    finish()
                }
            }
        }
        editWorkspace.setOnClickListener {
            editWorkspace.showDropDown()
        }
    }

    private fun isLateNight(startTimeInMillis: Long): Boolean {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH).apply {
            timeInMillis = startTimeInMillis
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour >= 23
    }

    private fun getEndOfDayMillis(startTimeInMillis: Long): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH).apply {
            timeInMillis = startTimeInMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    private fun viewDatePicker(selectedMillis: Long, startAt: Long, title: Int, function: (Long) -> Unit) {
        val constraints = CalendarConstraints.Builder().setValidator(DateValidatorPointForward.from(startAt)).build()
        val datePicker = MaterialDatePicker.Builder.datePicker().setTheme(R.style.ThemeOverlay_App_DatePicker).setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR).setSelection(
            selectedMillis
        ).setTitleText(getString(title)).setCalendarConstraints(constraints).setPositiveButtonText(getString(R.string.button_choose)).setNegativeButtonText(getString(R.string.button_cancel)).build()
        datePicker.addOnPositiveButtonClickListener {
            function.invoke(it)
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
        datePicker.dialog?.window?.applyMaterialConfig()
    }

    private fun viewTimePicker(selectedMillis: Long, title: Int, function: (Long) -> Unit) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH).apply {
            timeInMillis = selectedMillis
        }
        val timePicker = MaterialTimePicker.Builder().setTheme(R.style.TimePicker).setHour(calendar.get(Calendar.HOUR_OF_DAY)).setMinute(calendar.get(Calendar.MINUTE)).setTitleText(getString(title)).setPositiveButtonText(getString(R.string.button_choose)).setNegativeButtonText(getString(R.string.button_cancel)).build()

        timePicker.addOnPositiveButtonClickListener {
            val newHour: Int = timePicker.hour
            val newMinute: Int = timePicker.minute
            val selectedTimeInMillis = (newHour * 60 * 60 * 1000L) + (newMinute * 60 * 1000L)
            function.invoke(selectedTimeInMillis)
        }
        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }

    override fun ActivityAddTaskBinding.initView() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        onBackPressedDispatcher.addCallback { if (intent?.getBooleanExtra("isCallAfter", false) == true) {
            go(PlannerActivity::class.java, finish = true)
        } else {
            finish()
        } }
    }
}