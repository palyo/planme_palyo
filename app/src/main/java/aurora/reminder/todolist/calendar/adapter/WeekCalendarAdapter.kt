package aurora.reminder.todolist.calendar.adapter

import android.view.*
import androidx.recyclerview.widget.*
import aurora.reminder.todolist.calendar.databinding.*
import java.text.*
import java.util.*

class WeekCalendarAdapter(var listener: (Calendar) -> Unit) :
    RecyclerView.Adapter<WeekCalendarAdapter.WeekViewHolder>() {
    private var allWeeks: MutableList<MutableList<Calendar>> = mutableListOf()
    private var selectedDate: Calendar? = null
    private var selectedPosition: Int = -1
    private val dateFormat = SimpleDateFormat("dd", Locale.ENGLISH)
    private val dayFormat = SimpleDateFormat("EEE", Locale.ENGLISH)

    fun updateData(data: MutableList<MutableList<Calendar>>) {
        this.allWeeks = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        return WeekViewHolder(
            LayoutRowItemWeekDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        holder.bind(allWeeks[position], position, listener)
    }

    override fun getItemCount(): Int = allWeeks.size

    inner class WeekViewHolder(var binding: LayoutRowItemWeekDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(weekDates: MutableList<Calendar>, weekPosition: Int, listener: (Calendar) -> Unit) {
            if (weekDates.size == 7) {
                binding.apply {
                    day1.text = dayFormat.format(weekDates[0].time)
                    day2.text = dayFormat.format(weekDates[1].time)
                    day3.text = dayFormat.format(weekDates[2].time)
                    day4.text = dayFormat.format(weekDates[3].time)
                    day5.text = dayFormat.format(weekDates[4].time)
                    day6.text = dayFormat.format(weekDates[5].time)
                    day7.text = dayFormat.format(weekDates[6].time)

                    date1.text = dateFormat.format(weekDates[0].time)
                    date2.text = dateFormat.format(weekDates[1].time)
                    date3.text = dateFormat.format(weekDates[2].time)
                    date4.text = dateFormat.format(weekDates[3].time)
                    date5.text = dateFormat.format(weekDates[4].time)
                    date6.text = dateFormat.format(weekDates[5].time)
                    date7.text = dateFormat.format(weekDates[6].time)

                    resetDayHighlights()
                    selectedDate?.let { selected ->
                        weekDates.forEachIndexed { index, calendar ->
                            if (isSameDate(selected, calendar)) {
                                selectDay(index)
                            }
                        }
                    }

                    layoutDay1.setOnClickListener { updateSelection(weekDates[0], weekPosition, 0, listener) }
                    layoutDay2.setOnClickListener { updateSelection(weekDates[1], weekPosition, 1, listener) }
                    layoutDay3.setOnClickListener { updateSelection(weekDates[2], weekPosition, 2, listener) }
                    layoutDay4.setOnClickListener { updateSelection(weekDates[3], weekPosition, 3, listener) }
                    layoutDay5.setOnClickListener { updateSelection(weekDates[4], weekPosition, 4, listener) }
                    layoutDay6.setOnClickListener { updateSelection(weekDates[5], weekPosition, 5, listener) }
                    layoutDay7.setOnClickListener { updateSelection(weekDates[6], weekPosition, 6, listener) }
                }
            }
        }

        private fun LayoutRowItemWeekDayBinding.resetDayHighlights() {
            layoutDay1.isSelected = false
            layoutDay2.isSelected = false
            layoutDay3.isSelected = false
            layoutDay4.isSelected = false
            layoutDay5.isSelected = false
            layoutDay6.isSelected = false
            layoutDay7.isSelected = false
        }

        private fun LayoutRowItemWeekDayBinding.selectDay(dayIndex: Int) {
            when (dayIndex) {
                0 -> layoutDay1.isSelected = true
                1 -> layoutDay2.isSelected = true
                2 -> layoutDay3.isSelected = true
                3 -> layoutDay4.isSelected = true
                4 -> layoutDay5.isSelected = true
                5 -> layoutDay6.isSelected = true
                6 -> layoutDay7.isSelected = true
            }
        }

        private fun updateSelection(date: Calendar, weekPosition: Int, dayIndex: Int, listener: (Calendar) -> Unit) {
            val previousSelectedPosition = selectedPosition

            selectedDate = date.clone() as Calendar
            selectedPosition = weekPosition * 7 + dayIndex

            if (previousSelectedPosition != -1) {
                notifyItemChanged(previousSelectedPosition / 7)
            }
            notifyItemChanged(weekPosition)
            listener.invoke(date)
        }
    }

    private fun isSameDate(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
    }

    fun updateSelectedDate(newDate: Calendar) {
        // Find the week position and day index of the new date
        val newPosition = allWeeks.indexOfFirst { week ->
            week.any { isSameDate(newDate, it) }
        }

        if (newPosition != -1) {
            val dayIndex = allWeeks[newPosition].indexOfFirst { isSameDate(newDate, it) }
            if (dayIndex != -1) {
                val previousSelectedPosition = selectedPosition
                selectedDate = newDate.clone() as Calendar
                selectedPosition = newPosition * 7 + dayIndex
                // Update only the affected items
                if (previousSelectedPosition != -1) {
                    notifyItemChanged(previousSelectedPosition / 7)
                }
                notifyItemChanged(newPosition)
            }
        }
    }
}


