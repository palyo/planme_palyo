package aurora.reminder.todolist.calendar

import android.app.*
import android.content.*
import android.os.*
import androidx.multidex.*
import aurora.reminder.todolist.calendar.activity.LauncherActivity
import aurora.reminder.todolist.calendar.activity.main.*
import aurora.reminder.todolist.calendar.calldorado.*
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.viewmodel.*
import com.calldorado.ui.aftercall.*

class App : MultiDexApplication(), Application.ActivityLifecycleCallbacks {
    lateinit var weekCalendarViewModel: WeekCalendarViewModel

    companion object {
        private var instance: App? = null
        private var appContext: Context? = null
        var currentActivity: Activity? = null
        var classes: MutableList<Class<*>> = mutableListOf()
        var isOpenInter = false
        fun getInstance(): App = instance ?: throw IllegalStateException("Application is not created yet!")
        fun getAppContext(): Context = appContext ?: throw IllegalStateException("Application is not created yet!")
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        appContext = applicationContext
        PlannerDatabase.getDatabase(applicationContext)
        initCalldorado()
        weekCalendarViewModel = WeekCalendarViewModel()
        weekCalendarViewModel.preloadWeeks()
        createNotificationChannel()
        setAvoidMultipleClass(
            mutableListOf(
                LauncherActivity::class.java,
                AppPermissionActivity::class.java,
                NewPermissionActivity::class.java,
                PromptActivity::class.java,
                CallerIdActivity::class.java
            )
        )
        registerActivityLifecycleCallbacks(this)
    }

    open fun setAvoidMultipleClass(aClass: MutableList<Class<*>>) {
        classes.addAll(aClass)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_channel",
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for task reminders"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}