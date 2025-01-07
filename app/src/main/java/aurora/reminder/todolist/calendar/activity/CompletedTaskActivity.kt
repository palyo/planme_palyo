package aurora.reminder.todolist.calendar.activity

import androidx.activity.*
import androidx.recyclerview.widget.*
import coder.apps.space.library.base.*
import coder.apps.space.library.extension.*
import kotlinx.coroutines.*
import aurora.reminder.todolist.calendar.adapter.*
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.databinding.*

class CompletedTaskActivity : BaseActivity<ActivityCompletedTaskBinding>(ActivityCompletedTaskBinding::inflate) {
    private var taskAdapter: CompletedTaskAdapter? = null
    override fun ActivityCompletedTaskBinding.initExtra() {
        setupAdapter()
        setupData()
    }

    private fun setupData() {
        CoroutineScope(Dispatchers.IO).launch {
            val completed = taskActivityDao.findTaskWithActivities()
            val updatedTasks = completed.filter { it.activities.size > 0 }.toMutableList()
            runOnUiThread {
                taskAdapter?.updateData(updatedTasks)
            }
        }
    }

    private fun ActivityCompletedTaskBinding.setupAdapter() {
        recyclerViewTask.apply {
            taskAdapter = CompletedTaskAdapter(this@CompletedTaskActivity) {
                go(TrackTaskActivity::class.java, listOf("task" to it.task))
            }
            layoutManager = LinearLayoutManager(this@CompletedTaskActivity, LinearLayoutManager.VERTICAL, false)
            adapter = taskAdapter
        }
    }

    override fun ActivityCompletedTaskBinding.initListeners() {

    }

    override fun ActivityCompletedTaskBinding.initView() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        onBackPressedDispatcher.addCallback { finish() }
    }
}