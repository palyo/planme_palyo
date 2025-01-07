package aurora.reminder.todolist.calendar.fragment

import android.content.*
import android.os.*
import coder.apps.space.library.base.*
import coder.apps.space.library.extension.*
import aurora.reminder.todolist.calendar.*
import aurora.reminder.todolist.calendar.activity.main.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.module.*

class SettingFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {
    override fun create() {
        arguments?.apply {}
    }

    override fun FragmentSettingsBinding.initView() {

    }

    override fun FragmentSettingsBinding.initListeners() {
        activity?.apply context@{
            buttonLanguage.setOnClickListener { go(AppLanguageActivity::class.java, listOf(IS_SETTINGS to true), finish = true) }
            updateThemeUi()
            lightMode.setOnClickListener {
                tinyDB?.putInt(THEME, 1)
                updateThemeUi()
                themeToggleMode()
            }
            darkMode.setOnClickListener {
                tinyDB?.putInt(THEME, 2)
                updateThemeUi()
                themeToggleMode()
            }
            systemDefault.setOnClickListener {
                tinyDB?.putInt(THEME, 3)
                updateThemeUi()
                themeToggleMode()
            }

            buttonShare.setOnClickListener {
                val app = getString(R.string.app_name)
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        "android.intent.extra.TEXT", "$app\n\nOpen this Link on Play Store\n\nhttps://play.google.com/store/apps/details?id=${activity?.packageName}"
                    )
                    startActivity(Intent.createChooser(this, "Share Application"))
                }
            }
            buttonPrivacy.setOnClickListener {
                App.isOpenInter = true
                activity?.launchUrl(getPolicyLink())
            }
        }
    }

    override fun FragmentSettingsBinding.viewCreated() {
    }

    private fun FragmentSettingsBinding.updateThemeUi() {
        lightMode.isSelected = tinyDB?.getInt(THEME, 3) == 1
        darkMode.isSelected = tinyDB?.getInt(THEME, 3) == 2
        systemDefault.isSelected = tinyDB?.getInt(THEME, 3) == 3
    }

    companion object {
        fun newInstance() = SettingFragment().apply {
            arguments = Bundle().apply { }
        }
    }
}