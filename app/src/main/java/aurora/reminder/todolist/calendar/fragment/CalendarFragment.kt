package aurora.reminder.todolist.calendar.fragment

import android.os.*
import androidx.core.content.*
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import coder.apps.space.library.base.*
import aurora.reminder.todolist.calendar.*
import aurora.reminder.todolist.calendar.adapter.*
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.database.relations.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.model.*
import aurora.reminder.todolist.calendar.utils.*
import aurora.reminder.todolist.calendar.view.*
import aurora.reminder.todolist.calendar.viewmodel.*
import java.util.*

class CalendarFragment : BaseFragment<FragmentCalendarBinding>(FragmentCalendarBinding::inflate) {
    private var currentDate: Long = 0L
    private var taskOptionsPopup: TimelineTaskOptionsPopup? = null
    private var weekCalendarAdapter: WeekCalendarAdapter? = null
    private var weekCalendarViewModel: WeekCalendarViewModel? = null
    private var taskViewModel: TaskViewModel? = null
    private var tasks: MutableList<TaskWithActivities>? = null
    private var isScrolled: Boolean = false
    private var currentMonth: Calendar = Calendar.getInstance(Locale.ENGLISH)

    override fun create() {
        arguments?.apply {}
    }

    override fun FragmentCalendarBinding.initView() {
        activity?.apply context@{
            taskViewModel = ViewModelProvider(
                this,
                TaskViewModelFactory(taskDao = taskDao)
            )[TaskViewModel::class.java]
        }
    }

    override fun FragmentCalendarBinding.initListeners() {
        activity?.apply context@{
            taskOptionsPopup = TimelineTaskOptionsPopup(this)
            timelineContainer.setTaskClickListener(object : TimelineTaskClickListener {
                override fun onTaskClick(task: TimelineTask, x: Float, y: Float) {
                    taskOptionsPopup?.show(tasks, currentDate, task, timelineContainer, x, y)
                }
            })
            textCurrentDate.setOnClickListener {
                showDatePicker(currentMonth) { calendar ->
                    taskViewModel?.changeDateForTasks(calendar.timeInMillis)
                    textCurrentDate.text = calendar.timeInMillis.getCurrentDate()
                    currentDate = calendar.timeInMillis
                    val position = weekCalendarViewModel?.findWeekPosition(calendar)
                    if (position != null && position != -1) {
                        binding?.recyclerView?.scrollToPosition(position)
                        weekCalendarAdapter?.updateSelectedDate(calendar)
                    }
                    currentMonth = calendar.apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            }
            buttonZoomIn.setOnClickListener {
                timelineContainer.zoomIn()
                timelineHoursView.zoomIn()
            }

            buttonZoomOut.setOnClickListener {
                timelineContainer.zoomOut()
                timelineHoursView.zoomOut()
            }
        }
    }

    private fun showDatePicker(calendar: Calendar, listener: (Calendar) -> Unit) {
        DatePickerBottomSheet.newInstance(calendar).apply {
            setOnDateSelectedListener(listener)
        }.show(childFragmentManager, DatePickerBottomSheet.TAG)
    }

    override fun FragmentCalendarBinding.viewCreated() {
        activity?.apply context@{
            setupAdapter()
        }
    }

    private fun FragmentCalendarBinding.setupAdapter() {
        activity?.apply context@{
            recyclerView.apply {
                weekCalendarAdapter = WeekCalendarAdapter {
                    currentDate = it.timeInMillis
                    taskViewModel?.changeDateForTasks(it.timeInMillis)
                    textCurrentDate.text = it.timeInMillis.getCurrentDate()
                }
                layoutManager = LinearLayoutManager(this@context, LinearLayoutManager.HORIZONTAL, false)
                adapter = weekCalendarAdapter
                PagerSnapHelper().attachToRecyclerView(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding?.setupData()
    }

    private fun FragmentCalendarBinding.setupData() {
        activity?.apply context@{
            val app = application as App
            weekCalendarViewModel = app.weekCalendarViewModel
            weekCalendarViewModel?.weeks?.observe(this@context) { weeks ->
                activity?.runOnUiThread {
                    weekCalendarAdapter?.updateData(weeks)
                    weekCalendarAdapter?.updateSelectedDate(Calendar.getInstance(Locale.ENGLISH))
                }
            }

            weekCalendarViewModel?.currentWeekPosition?.observe(this@context) { position ->
                activity?.runOnUiThread {
                    binding?.recyclerView?.scrollToPosition(position)
                }
            }

            textCurrentDate.text = fetchStartAndEndOfDay(Date().time).first.getCurrentDate()
            currentDate = fetchStartAndEndOfDay(Date().time).first
            taskViewModel?.taskActivities?.observe(viewLifecycleOwner) { tasks ->
                activity?.runOnUiThread {
                    this@CalendarFragment.tasks = tasks
                    val newTasks = mutableListOf<TimelineTask>()
                    tasks.sortBy { it.task.dailyStartTime }

                    tasks.forEach { task ->
                        val existedTaskActivity = task.activities.find {
                            (it.taskType == TaskType.DAILY_TASK.ordinal && task.task.id == it.taskId && it.completionDate == currentDate) ||
                                    (it.taskType == TaskType.ONE_TIME.ordinal && task.task.id == it.taskId)
                        }
                        newTasks.add(
                            TimelineUtils.createTask(
                                id = task.task.id ?: 0,
                                title = task.task.title,
                                body = task.task.body,
                                timestamp = getString(R.string.task_duration_schedule, calculateDuration(task.task.dailyStartTime, task.task.dailyEndTime), createDateRangeString(task.task.startDate, task.task.endDate)),
                                startTime = task.task.dailyStartTime.convertMillisTo24HourTime(),
                                endTime = task.task.dailyEndTime.convertMillisTo24HourTime(),
                                color = ContextCompat.getColor(this@context, R.color.colorInverseCardBackground),
                                isCompleted = existedTaskActivity?.isCompleted == true
                            )
                        )
                    }
                    timelineContainer.setTasks(newTasks)
                    textTaskToday.text = getString(R.string.label_total_task_today, tasks.size)
                    if (!isScrolled) scrollToCurrentTime()
                }
            }

            taskViewModel?.changeDateForTasks(Date().time)
        }
    }

    private fun FragmentCalendarBinding.scrollToCurrentTime() {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val hourWidth = resources.getDimension(com.intuit.sdp.R.dimen._84sdp)
        val sideMargin = hourWidth / 2
        val minutePosition = (currentMinute / 60f) * hourWidth
        val scrollPosition = ((currentHour * hourWidth) + minutePosition + sideMargin - (horizontalScroll.width / 2)).toInt()
        horizontalScroll.post {
            isScrolled = true
            horizontalScroll.smoothScrollTo(scrollPosition.coerceAtLeast(0), 0)
        }
    }

    companion object {
        fun newInstance() = CalendarFragment().apply {
            arguments = Bundle().apply { }
        }
    }
}