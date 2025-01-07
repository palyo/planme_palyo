package aurora.reminder.todolist.calendar.calldorado

import android.content.*
import aurora.reminder.todolist.calendar.calldorado.*
import com.calldorado.Calldorado.setAftercallCustomView

class SetupFragmentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.PHONE_STATE") {
            setAftercallCustomView(context, AfterCallCustomView(context))
        }
    }
}
