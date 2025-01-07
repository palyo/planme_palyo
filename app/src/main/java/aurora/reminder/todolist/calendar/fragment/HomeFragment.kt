package aurora.reminder.todolist.calendar.fragment

import android.animation.*
import android.os.*
import android.view.animation.*
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import coder.apps.space.library.base.*
import coder.apps.space.library.extension.*
import aurora.reminder.todolist.calendar.*
import aurora.reminder.todolist.calendar.activity.*
import aurora.reminder.todolist.calendar.adapter.*
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.model.*
import aurora.reminder.todolist.calendar.view.*
import aurora.reminder.todolist.calendar.viewmodel.*
import java.util.*

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private var bounceAnim: ObjectAnimator? = null
    private var currentDate: Long = 0L
    private var taskAdapter: TaskAdapter? = null
    private var taskViewModel: TaskViewModel? = null
    private var systemWorkspaceTaskAdapter: WorkspaceTaskAdapter? = null
    private var personalWorkspaceTaskAdapter: WorkspaceTaskAdapter? = null
    private var weekCalendarAdapter: WeekCalendarAdapter? = null
    private var weekCalendarViewModel: WeekCalendarViewModel? = null
    private var workspaceTaskViewModel: WorkspaceTaskViewModel? = null
    private var currentMonth: Calendar = Calendar.getInstance(Locale.ENGLISH)
    override fun create() {
        arguments?.apply {}
    }

    override fun FragmentHomeBinding.initView() {
        activity?.apply context@{
            workspaceTaskViewModel = ViewModelProvider(
                this,
                WorkspaceTaskViewModelFactory(workspaceTaskDao = workspaceTaskDao)
            )[WorkspaceTaskViewModel::class.java]
            taskViewModel = ViewModelProvider(
                this,
                TaskViewModelFactory(taskDao = taskDao)
            )[TaskViewModel::class.java]
            val app = application as App
            weekCalendarViewModel = app.weekCalendarViewModel
        }
    }

    override fun FragmentHomeBinding.initListeners() {
        activity?.apply context@{
            cardCompletedTask.setOnClickListener { go(CompletedTaskActivity::class.java) }
            buttonFirstTask.setOnClickListener { go(AddTaskActivity::class.java) }
            textCurrentDate.setOnClickListener {
                showDatePicker(currentMonth) { calendar ->
                    taskViewModel?.changeDateForTasks(calendar.timeInMillis)
                    textCurrentDate.text = calendar.timeInMillis.getCurrentDate()
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
        }
    }

    private fun initBounceAnimation() {
        binding?.layoutBubble?.beVisible()
        bounceAnim = ObjectAnimator.ofFloat(binding?.layoutBubble, "translationY", 0f, -50f, 0f).apply {
            duration = 2500
            repeatCount = ValueAnimator.INFINITE
            interpolator = BounceInterpolator()
        }
        bounceAnim?.start()
    }

    fun stopBounceAnimation() {
        binding?.layoutBubble?.beGone()
        bounceAnim?.cancel()
    }

    private fun showDatePicker(calendar: Calendar, listener: (Calendar) -> Unit) {
        DatePickerBottomSheet.newInstance(calendar).apply {
            setOnDateSelectedListener(listener)
        }.show(childFragmentManager, DatePickerBottomSheet.TAG)
    }

    override fun FragmentHomeBinding.viewCreated() {
        setupAdapter()
        binding?.setupData()
    }

    private fun FragmentHomeBinding.setupData() {
        activity?.apply context@{
            weekCalendarViewModel?.weeks?.observe(viewLifecycleOwner) { weeks ->
                activity?.runOnUiThread {
                    weekCalendarAdapter?.updateData(weeks)
                    weekCalendarAdapter?.updateSelectedDate(Calendar.getInstance(Locale.ENGLISH))
                }
            }

            weekCalendarViewModel?.currentWeekPosition?.observe(viewLifecycleOwner) { position ->
                activity?.runOnUiThread {
                    binding?.recyclerView?.scrollToPosition(position)
                }
            }
            workspaceTaskViewModel?.systemWorkspaceWithTask?.observe(viewLifecycleOwner) { workspaceWithTasks ->
                val systemWorkspaceTasks = mutableListOf<Any>()
                workspaceWithTasks?.forEach {
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
                activity?.runOnUiThread {
                    systemWorkspaceTaskAdapter?.updateData(systemWorkspaceTasks)
                }
            }

            workspaceTaskViewModel?.personalWorkspaceWithTask?.observe(viewLifecycleOwner) { workspaceWithTasks ->
                val personalWorkspaceTasks = mutableListOf<Any>()
                workspaceWithTasks?.forEach {
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
                activity?.runOnUiThread {
                    personalWorkspaceTaskAdapter?.updateData(personalWorkspaceTasks)
                }
            }

            textCurrentDate.text = fetchStartAndEndOfDay(Date().time).first.getCurrentDate()
            currentDate = fetchStartAndEndOfDay(Date().time).first
            taskViewModel?.taskActivities?.observe(viewLifecycleOwner) { tasks ->
                activity?.runOnUiThread {
                    if (tasks.size == 0) initBounceAnimation() else stopBounceAnimation()
                    labelTodayTask.beVisibleIf(tasks.size > 0)
                    layoutFirstTask.beVisibleIf(tasks.size == 0)
                    tasks.sortBy { it.task.dailyStartTime }
                    val completedTask = tasks.filter { it.activities.find { activity -> activity.completionDate == currentDate }?.isCompleted == true }.size
                    taskAdapter?.updateData(tasks)
                    bodyDailyTask.text = getString(R.string.body_daily_task_done, completedTask, tasks.size)
                    progressDailyTask.max = tasks.size
                    progressDailyTask.setProgress(completedTask, true)
                }
            }
            taskViewModel?.tasks?.observe(viewLifecycleOwner) { tasks ->
                activity?.runOnUiThread {
                    val completedTask = tasks.count { task ->
                        when (task.task.taskType) {
                            TaskType.DAILY_TASK.ordinal -> {
                                val isCompleted = task.activities.size == (daysBetween(task.task.startDate, task.task.endDate) + 1).toInt()
                                return@count isCompleted
                            }

                            TaskType.ONE_TIME.ordinal -> task.activities.any { it.isCompleted }
                            else -> false
                        }
                    }
                    bodyCompleted.text = getString(R.string.body_completed_task, completedTask, tasks.size)
                    progressCompleted.max = tasks.size
                    progressCompleted.setProgress(completedTask, true)
                }
            }
            taskViewModel?.changeDateForTasks(Date().time)
        }
    }

    private fun FragmentHomeBinding.setupAdapter() {
        activity?.apply context@{
            recyclerView.apply {
                weekCalendarAdapter = WeekCalendarAdapter {
                    taskViewModel?.changeDateForTasks(it.timeInMillis)
                    workspaceTaskViewModel?.changeDateForTasks(it.timeInMillis)
                    taskAdapter?.updateSelectedDate(it.timeInMillis)
                    systemWorkspaceTaskAdapter?.updateSelectedDate(it.timeInMillis)
                    personalWorkspaceTaskAdapter?.updateSelectedDate(it.timeInMillis)
                    currentDate = it.timeInMillis
                    textCurrentDate.text = it.timeInMillis.getCurrentDate()
                }
                layoutManager = LinearLayoutManager(this@context, LinearLayoutManager.HORIZONTAL, false)
                adapter = weekCalendarAdapter
                PagerSnapHelper().attachToRecyclerView(this)
            }
            recyclerViewWorkspaceSystem.apply {
                systemWorkspaceTaskAdapter = WorkspaceTaskAdapter(this@context, true)
                layoutManager = LinearLayoutManager(this@context, LinearLayoutManager.VERTICAL, false)
                adapter = systemWorkspaceTaskAdapter
            }

            recyclerViewWorkspaceUser.apply {
                personalWorkspaceTaskAdapter = WorkspaceTaskAdapter(this@context, false)
                layoutManager = LinearLayoutManager(this@context, LinearLayoutManager.VERTICAL, false)
                adapter = personalWorkspaceTaskAdapter
            }

            recyclerViewTask.apply {
                taskAdapter = TaskAdapter(this@context) {
                    go(AddTaskActivity::class.java, listOf("task" to it))
                }
                layoutManager = LinearLayoutManager(this@context, LinearLayoutManager.VERTICAL, false)
                adapter = taskAdapter
            }
        }
    }

    companion object {
        fun newInstance() = HomeFragment().apply {
            arguments = Bundle().apply { }
        }
    }
}