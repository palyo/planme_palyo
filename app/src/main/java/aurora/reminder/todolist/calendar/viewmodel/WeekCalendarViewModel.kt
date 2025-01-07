package aurora.reminder.todolist.calendar.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.*
import java.util.*

class WeekCalendarViewModel : ViewModel() {

    private val _weeks = MutableLiveData<MutableList<MutableList<Calendar>>>()
    val weeks: LiveData<MutableList<MutableList<Calendar>>> = _weeks

    private val _currentWeekPosition = MutableLiveData<Int>()
    val currentWeekPosition: LiveData<Int> = _currentWeekPosition

    init {
        preloadWeeks()
    }

    fun preloadWeeks() {
        viewModelScope.launch(Dispatchers.Default) {
            val allWeeks = getWeeksFrom2020To2030()
            val position = getCurrentWeekPosition(allWeeks)

            withContext(Dispatchers.Main) {
                _weeks.value = allWeeks
                _currentWeekPosition.value = position
            }
        }
    }

    private fun getWeeksFrom2020To2030(): MutableList<MutableList<Calendar>> {
        val startCalendar = Calendar.getInstance(Locale.ENGLISH).apply {
            set(Calendar.YEAR, 2019)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 29)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            firstDayOfWeek = Calendar.SUNDAY
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
        }

        val endCalendar = Calendar.getInstance(Locale.ENGLISH).apply {
            set(Calendar.YEAR, 2031)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 4)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val allWeeks = mutableListOf<MutableList<Calendar>>()

        while (startCalendar.before(endCalendar)) {
            val weekDates = mutableListOf<Calendar>()
            repeat(7) {
                weekDates.add(startCalendar.clone() as Calendar)
                startCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            allWeeks.add(weekDates)
        }

        return allWeeks
    }

    private fun getCurrentWeekPosition(weeks: List<List<Calendar>>): Int {
        for ((index, week) in weeks.withIndex()) {
            if (week.any { isToday(it) }) {
                return index
            }
        }
        return -1
    }

    fun findWeekPosition(calendar: Calendar): Int {
        for ((index, week) in _weeks.value?.withIndex()!!) {
            if (week.any { isSame(it,calendar) }) {
                return index
            }
        }
        return -1
    }

    private fun isSame(calendar: Calendar, calendarSelected: Calendar): Boolean {
        return calendar.get(Calendar.YEAR) == calendarSelected.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == calendarSelected.get(Calendar.DAY_OF_YEAR)
    }

    private fun isToday(calendar: Calendar): Boolean {
        val today = Calendar.getInstance(Locale.ENGLISH)
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }
}
