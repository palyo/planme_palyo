package aurora.reminder.todolist.calendar.adapter

import android.content.*
import android.view.*
import androidx.recyclerview.widget.*
import aurora.reminder.todolist.calendar.database.table.*
import aurora.reminder.todolist.calendar.databinding.*
import java.text.*
import java.util.*

class TrackTaskAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var data: MutableList<TaskActivity> = mutableListOf()
    private val dateSdf = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)
    private val timeSdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TaskHolder(LayoutRowItemTrackTaskBinding.inflate(inflater, parent, false))
    }

    fun updateData(data: MutableList<TaskActivity>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TaskHolder -> holder.bind(data[position])
        }
    }

    override fun getItemCount(): Int = data.size

    inner class TaskHolder(var binding: LayoutRowItemTrackTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(taskActivity: TaskActivity) {
            binding.apply {
                taskDate.text = dateSdf.format(taskActivity.completionDate)
                taskTime.text = if (taskActivity.completionTime == 0L) "-" else timeSdf.format(taskActivity.completionTime)
                taskStatus.text = if (taskActivity.isCompleted) "Completed" else "Not Performed"
            }
        }
    }
}


