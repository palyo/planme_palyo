package aurora.reminder.todolist.calendar.calldorado

import android.content.*
import coder.apps.space.library.*
import com.calldorado.*

fun Context.initCalldorado() {
    Calldorado.start(this)
    Calldorado.setAdsDoNotSellMyData(this, !Calldorado.isCcpaAccepted(this))
    val colorMap = HashMap<Calldorado.ColorElement?, Int?>()
    colorMap[Calldorado.ColorElement.AftercallBgColor] = getColor(R.color.colorPrimary)
    colorMap[Calldorado.ColorElement.AftercallStatusBarColor] = getColor(R.color.colorBlack)
    colorMap[Calldorado.ColorElement.AftercallAdSeparatorColor] = getColor(R.color.colorIcon)
    colorMap[Calldorado.ColorElement.CardBgColor] = getColor(R.color.colorCardBackground)
    colorMap[Calldorado.ColorElement.CardTextColor] = getColor(R.color.colorText)
    colorMap[Calldorado.ColorElement.CardSecondaryColor] = getColor(R.color.colorTextOpacity)
    colorMap[Calldorado.ColorElement.NavigationColor] = getColor(R.color.colorAccent)
    colorMap[Calldorado.ColorElement.TabIconButtonTextColor] = getColor(R.color.colorAccentTool)
    colorMap[Calldorado.ColorElement.SelectedTabIconColor] = getColor(R.color.colorAccentTool)
    colorMap[Calldorado.ColorElement.MainTextColor] = getColor(R.color.colorText)
    colorMap[Calldorado.ColorElement.DarkAccentColor] = getColor(R.color.colorAccent)
    Calldorado.setCustomColors(this, colorMap)
}

fun Context.eulaAccepted() {
    val conditionsMap: HashMap<Calldorado.Condition, Boolean> = HashMap()
    conditionsMap[Calldorado.Condition.EULA] = true
    conditionsMap[Calldorado.Condition.PRIVACY_POLICY] = true
    Calldorado.acceptConditions(this, conditionsMap)
}