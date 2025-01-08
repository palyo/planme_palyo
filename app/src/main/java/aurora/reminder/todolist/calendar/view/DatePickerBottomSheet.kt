package aurora.reminder.todolist.calendar.view

import android.os.*
import android.view.*
import androidx.fragment.app.*
import androidx.recyclerview.widget.*
import aurora.reminder.todolist.calendar.adapter.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.viewmodel.*
import com.google.android.material.bottomsheet.*
import java.text.*
import java.util.*

class DatePickerBottomSheet : BottomSheetDialogFragment() {
    private var currentMonth: Int = -1
    private lateinit var binding: BottomSheetDatePickerBinding
    private val calendarViewModel: CalendarViewModel by viewModels()
    private var onDateSelectedListener: ((Calendar) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetDatePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val initialMonth = arguments?.getSerializable("initial_month") as? Calendar
        initialMonth?.let {
            calendarViewModel.setCurrentMonth(it)
        }
        binding.setupCalendarView()
        binding.setupMonthNavigation()
        binding.observeViewModel()
    }

    private fun BottomSheetDatePickerBinding.setupCalendarView() {
        val calendarAdapter = CalendarAdapter().apply {
            setOnDateClickListener { date ->
                onDateSelectedListener?.invoke(date)
                dismiss()
            }
            /*attachSwipeListener(recyclerView = calendarView.calendarRecyclerView) {
                if (it == -1) {
                    calendarViewModel.navigateToPreviousMonth()
                } else {
                    calendarViewModel.navigateToNextMonth()
                }
            }*/
        }

        calendarView.calendarRecyclerView.apply {
            adapter = calendarAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun BottomSheetDatePickerBinding.setupMonthNavigation() {
        buttonPrev.setOnClickListener {
            calendarViewModel.navigateToPreviousMonth()
        }

        buttonNext.setOnClickListener {
            calendarViewModel.navigateToNextMonth()
        }
    }

    private fun BottomSheetDatePickerBinding.observeViewModel() {
        calendarViewModel.currentMonth.observe(viewLifecycleOwner) { calendar ->
            currentMonth = calendar.get(Calendar.MONTH)
            updateMonthYearText(calendar)
        }

        calendarViewModel.weeks.observe(viewLifecycleOwner) { weeks ->
            (calendarView.calendarRecyclerView.adapter as? CalendarAdapter)?.setData(weeks, currentMonth)
        }
    }

    private fun BottomSheetDatePickerBinding.updateMonthYearText(calendar: Calendar) {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        textMonth.text = monthFormat.format(calendar.time)
    }

    fun setOnDateSelectedListener(listener: (Calendar) -> Unit) {
        onDateSelectedListener = listener
    }

    companion object {
        const val TAG = "DatePickerBottomSheet"

        fun newInstance(initialMonth: Calendar): DatePickerBottomSheet {
            val fragment = DatePickerBottomSheet()
            val args = Bundle().apply {
                putSerializable("initial_month", initialMonth)
            }
            fragment.arguments = args
            return fragment
        }
    }
}