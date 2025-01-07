package aurora.reminder.todolist.calendar.extension

import android.content.*
import android.net.*
import androidx.annotation.*
import androidx.core.content.*

fun Context.getColorCompat(@ColorRes resId: Int) =
    ContextCompat.getColor(this, resId)

fun Context.getDrawableCompat(@DrawableRes resId: Int) =
    ContextCompat.getDrawable(this, resId)


fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo

    return activeNetwork != null && activeNetwork.isConnectedOrConnecting
}