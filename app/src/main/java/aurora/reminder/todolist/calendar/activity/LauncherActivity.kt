package aurora.reminder.todolist.calendar.activity

import android.*
import android.os.*
import androidx.lifecycle.*
import coder.apps.space.library.base.*
import coder.apps.space.library.extension.*
import kotlinx.coroutines.*
import aurora.reminder.todolist.calendar.activity.main.*
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.database.table.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.module.*
import java.util.concurrent.atomic.*

class LauncherActivity : BaseActivity<ActivityLauncherBinding>(ActivityLauncherBinding::inflate) {
    private var consentManager: ConsentManager? = null
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    override fun ActivityLauncherBinding.initExtra() {
        init { requestConsentForm() }
    }

    private fun requestConsentForm() {
        if (isNetworkAvailable()) {
            consentManager = ConsentManager.getInstance(this)
            consentManager?.gatherConsent(this) { consentError ->
                if (consentManager?.canRequestAds == true) {
                    try {
                        initializeMobileAdsSdk()
                    } catch (_: Exception) {
                    }
                }
            }
        } else {
            Handler(mainLooper).postDelayed({
                gotoDashboard()
            }, 3000)
        }
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        Handler(mainLooper).postDelayed({
            gotoDashboard()
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            workspaceDao.insertAll(
                mutableListOf(
                    Workspace(title = "Shared Workspace", isSystemSpace = true),
                    Workspace(title = "Private Workspace", isSystemSpace = true),
                    Workspace(title = "PlanMe Workspace", isSystemSpace = false)
                )
            )
        }
    }

    private fun gotoDashboard() {
        if (tinyDB?.getBoolean(IS_INTRO_ENABLED, true) == true && (!hasPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE)) || !hasOverlayPermission())) {
            go(NewPermissionActivity::class.java, finish = true)
        } else if (!hasPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE)) || !hasOverlayPermission()) {
            go(AppPermissionActivity::class.java, finish = true)
        } else if (tinyDB?.getBoolean(IS_LANGUAGE_ENABLED, true) == true) {
            go(AppLanguageActivity::class.java, finish = true)
        } else {
            go(PlannerActivity::class.java, finish = true)
        }
    }

    override fun ActivityLauncherBinding.initListeners() {}

    override fun ActivityLauncherBinding.initView() {}
}