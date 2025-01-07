package aurora.reminder.todolist.calendar.viewmodel

import androidx.lifecycle.*
import java.util.*

class CalendarViewModel : ViewModel() {
    private val _currentMonth = MutableLiveData<Calendar>()
    val currentMonth: LiveData<Calendar> = _currentMonth
    private val _weeks = MutableLiveData<MutableList<MutableList<Calendar>>>()
    val weeks: LiveData<MutableList<MutableList<Calendar>>> = _weeks

    init {
        setCurrentMonth(Calendar.getInstance(Locale.ENGLISH))
    }

    fun setCurrentMonth(calendar: Calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        _currentMonth.value = calendar
        _weeks.value = generateWeeksForMonth(calendar)
    }

    fun navigateToNextMonth() {
        _currentMonth.value?.let { calendar ->
            calendar.add(Calendar.MONTH, 1)
            setCurrentMonth(calendar.clone() as Calendar)
        }
    }

    fun navigateToPreviousMonth() {
        _currentMonth.value?.let { calendar ->
            calendar.add(Calendar.MONTH, -1)
            setCurrentMonth(calendar.clone() as Calendar)
        }
    }

    private fun generateWeeksForMonth(calendar: Calendar): MutableList<MutableList<Calendar>> {
        val weeks = mutableListOf<MutableList<Calendar>>()
        val monthCalendar = calendar.clone() as Calendar

        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        while (monthCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            monthCalendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        while (true) {
            val week = (0..6).map {
                val day = monthCalendar.clone() as Calendar
                monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
                day
            }.toMutableList()
            weeks.add(week)

            if (monthCalendar.get(Calendar.MONTH) != calendar.get(Calendar.MONTH) && monthCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                break
            }
        }

        return weeks
    }
}