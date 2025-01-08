package aurora.reminder.todolist.calendar.adapter

import android.view.*
import androidx.core.content.*
import androidx.recyclerview.widget.*
import aurora.reminder.todolist.calendar.*
import aurora.reminder.todolist.calendar.databinding.*
import java.util.*

class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    private var weeks: MutableList<MutableList<Calendar>> = mutableListOf()
    private var selectedDate: Calendar? = null
    private var currentMonth: Int = -1
    private var onDateClickListener: ((Calendar) -> Unit)? = null

    fun setData(newWeeks: MutableList<MutableList<Calendar>>, month: Int) {
        weeks = newWeeks
        currentMonth = month
        notifyDataSetChanged()
    }

    fun setOnDateClickListener(listener: (Calendar) -> Unit) {
        onDateClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = LayoutRowItemCalendarWeekBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(weeks[position])
    }

    override fun getItemCount(): Int = weeks.size

    inner class CalendarViewHolder(
        private val binding: LayoutRowItemCalendarWeekBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dayViews = mutableListOf(
            binding.sundayText,
            binding.mondayText,
            binding.tuesdayText,
            binding.wednesdayText,
            binding.thursdayText,
            binding.fridayText,
            binding.saturdayText
        )

        fun bind(week: MutableList<Calendar>) {
            week.forEachIndexed { index, date ->
                dayViews[index].apply {
                    text = date.get(Calendar.DAY_OF_MONTH).toString()
                    val isSelected = selectedDate?.let {
                        it.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) &&
                                it.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                    } ?: false
                    val isCurrentMonth = date.get(Calendar.MONTH) == currentMonth
                    setTextColor(if (isToday(calendar = date)) ContextCompat.getColor(binding.root.context, R.color.colorAccent) else if (isCurrentMonth) ContextCompat.getColor(binding.root.context, R.color.colorText) else ContextCompat.getColor(binding.root.context, R.color.colorTextOpacity24))
                    background = if (isToday(calendar = date)) ContextCompat.getDrawable(context, R.drawable.ic_bg_rounded_12sdp) else if (isSelected) {
                        ContextCompat.getDrawable(context, R.drawable.ic_bg_rounded_12sdp)
                    } else null

                    setOnClickListener {
                        selectedDate = date
                        onDateClickListener?.invoke(date)
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    fun isToday(calendar: Calendar): Boolean {
        val today = Calendar.getInstance(Locale.ENGLISH)
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    fun attachSwipeListener(recyclerView: RecyclerView, onMonthChange: (Int) -> Unit) {
        val gestureDetector = GestureDetector(recyclerView.context, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null || e2 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (Math.abs(diffX) > Math.abs(diffY)) { // Horizontal swipe
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onMonthChange(-1)
                        } else {
                            onMonthChange(1)
                        }
                        return true
                    }
                }
                return false
            }
        })

        recyclerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }
}