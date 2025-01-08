package aurora.reminder.todolist.calendar.activity.main

import androidx.activity.*
import androidx.recyclerview.widget.*
import aurora.reminder.todolist.calendar.activity.*
import aurora.reminder.todolist.calendar.adapter.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.module.*
import coder.apps.space.library.R
import coder.apps.space.library.base.*
import coder.apps.space.library.extension.*
import coder.apps.space.library.helper.*

class AppLanguageActivity : BaseActivity<ActivityAppLanguageBinding>(ActivityAppLanguageBinding::inflate) {
    private var language: String = "en"
    private var isChangeLanguage: Boolean = false
    override fun ActivityAppLanguageBinding.initExtra() {
        updateNavigationBarColor(R.color.colorTransparent)
        language = currentLanguage ?: "en"
        initAdapter()
        viewNativeMedium(adNative)
    }

    private fun ActivityAppLanguageBinding.initAdapter() {
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@AppLanguageActivity, 1)
            adapter = LanguageAdapter(this@AppLanguageActivity) {
                language = it
                isChangeLanguage = true
            }
        }
    }

    override fun ActivityAppLanguageBinding.initListeners() {
        buttonGo.setOnClickListener {
            currentLanguage = language
            val fromSettings = intent?.getBooleanExtra(IS_SETTINGS, false)
            if (fromSettings == true) {
                go(PlannerActivity::class.java, listOf(CURRENT_TAB to 2), finish = true)
            } else {
                tinyDB?.putBoolean(IS_LANGUAGE_ENABLED, false)
                go(PlannerActivity::class.java, listOf(CURRENT_TAB to 0), finish = true)
            }
        }
    }

    override fun ActivityAppLanguageBinding.initView() {
        updateStatusBarColor(R.color.colorPrimary)
        onBackPressedDispatcher.addCallback(this@AppLanguageActivity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fromSettings = intent?.getBooleanExtra(IS_SETTINGS, false)
                if (fromSettings == true) {
                    go(PlannerActivity::class.java, listOf(CURRENT_TAB to 2), finish = true)
                } else {
                    finish()
                }
            }
        })
    }
}