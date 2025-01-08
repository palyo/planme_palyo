package aurora.reminder.todolist.calendar.activity.main

import android.Manifest
import android.annotation.*
import android.content.*
import android.net.*
import android.os.*
import android.provider.*
import android.text.*
import android.text.method.*
import android.text.style.*
import android.view.*
import android.widget.*
import androidx.activity.result.*
import androidx.activity.result.contract.*
import aurora.reminder.todolist.calendar.R
import aurora.reminder.todolist.calendar.activity.*
import aurora.reminder.todolist.calendar.calldorado.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.module.*
import coder.apps.space.library.base.*
import coder.apps.space.library.extension.*
import coder.apps.space.library.helper.*

class AppPermissionActivity : BaseActivity<ActivityAppPermissionBinding>(ActivityAppPermissionBinding::inflate) {
    private val maxDeniedCount = 2
    var permissionFlow = 0
    private var isContinues = false
    private var settingOverLay: HandleSettingPreview? = null
    private val permissions = arrayOf(Manifest.permission.READ_PHONE_STATE)
    private var displayOverLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (!isContinues) {
            checkNGo()
            return@registerForActivityResult
        }
        if (hasOverlayPermission()) {
            settingOverLay?.cancelPollingImeSettings()
            initPermissions()
        } else {
            settingOverLay?.cancelPollingImeSettings()
        }
    }
    private val appSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (!isContinues) {
            checkNGo()
            return@registerForActivityResult
        }
        settingOverLay?.cancelPollingImeSettings()
        initPermissions()
    }
    private val phoneStateLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.containsValue(false)) {
            incrementPermissionsDeniedCount("phone_state")
            if (isContinues) {
                if (!hasPermissions(this.permissions)) {
                    viewPermissionSheet(
                        R.drawable.ic_vector_permission,
                        R.string.title_permission_required,
                        R.string.body_permission_required
                    ) {
                        val phoneDeniedCount = getPermissionsDeniedCount("phone_state")
                        if (phoneDeniedCount < maxDeniedCount && !hasPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE))) {
                            requestPhoneState()
                            return@viewPermissionSheet
                        }
                        val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", packageName, null)
                        }
                        appSettingsLauncher.launch(appSettingsIntent)
                    }
                    return@registerForActivityResult
                } else {
                    initPermissions()
                }
            }
        } else {
            if (!isContinues) {
                checkNGo()
                return@registerForActivityResult
            }
            if (hasPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE))) {
                initPermissions()
            }
        }
    }

    private fun requestPhoneState() {
        phoneStateLauncher.launch(arrayOf(Manifest.permission.READ_PHONE_STATE))
    }

    override fun ActivityAppPermissionBinding.initView() {
        updateStatusBarColor(coder.apps.space.library.R.color.colorPrimary)
        updateNavigationBarColor(coder.apps.space.library.R.color.colorPrimary)
        settingOverLay = HandleSettingPreview(this@AppPermissionActivity)
    }

    override fun ActivityAppPermissionBinding.initListeners() {
        val baseText = getString(R.string.message_policy)
        val spannableString = SpannableString(baseText)
        val linkText = "Privacy Policy"
        val startIndex = baseText.indexOf(linkText)
        val endIndex = startIndex + linkText.length
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                launchUrl(getPolicyLink())
            }
        }
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textPolicy.text = spannableString
        textPolicy.movementMethod = LinkMovementMethod.getInstance()
        buttonContinue.setOnClickListener {
            isContinues = true
            initPermissions()
        }

        if (hasPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE))) {
            textPhoneState.beGone()
            bodyPhoneState.beGone()
        }

        if (hasOverlayPermission()) {
            textOverlay.beGone()
            bodyOverlay.beGone()
        }
    }

    override fun ActivityAppPermissionBinding.initExtra() {}

    private fun initPermissions() {
        binding?.apply {
            if (!isTermsAgree.isChecked) {
                Toast.makeText(this@AppPermissionActivity, "Please agree with our terms and privacy", Toast.LENGTH_SHORT).show()
                return
            }
        }
        val phoneDeniedCount = getPermissionsDeniedCount("phone_state")
        if (phoneDeniedCount < maxDeniedCount && !hasPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE))) {
            requestPhoneState()
            return
        }

        if (!hasPermissions(permissions)) {
            viewPermissionSheet(
                R.drawable.ic_vector_permission,
                R.string.title_permission_required,
                R.string.body_permission_required
            ) {
                val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                appSettingsLauncher.launch(appSettingsIntent)
            }
            return
        }

        if (!hasOverlayPermission()) {
            permissionFlow = 1
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                settingOverLay?.startPollingImeSettings()
                displayOverLauncher.launch(this)
                Handler(mainLooper).postDelayed({
                    startActivity(Intent(this@AppPermissionActivity, PromptActivity::class.java))
                }, 500L)
            }
            return
        }

        eulaAccepted()
        if (tinyDB?.getBoolean(IS_LANGUAGE_ENABLED, true) == true) {
            go(AppLanguageActivity::class.java, finish = true)
        } else {
            go(PlannerActivity::class.java, finish = true)
        }
    }

    private fun checkNGo() {
        if (hasPermissions(permissions) && hasOverlayPermission()) {
            eulaAccepted()
            if (tinyDB?.getBoolean(IS_LANGUAGE_ENABLED, true) == true) {
                go(AppLanguageActivity::class.java, finish = true)
            } else {
                go(PlannerActivity::class.java, finish = true)
            }
        }
    }

    @SuppressLint("WrongConstant")
    fun invokeSetupWizardOfThisIme() {
        settingOverLay?.cancelPollingImeSettings()
        Intent(this@AppPermissionActivity, AppPermissionActivity::class.java).apply {
            flags = 606076928
            startActivity(this)
        }
    }

    class HandleSettingPreview internal constructor(activity: AppPermissionActivity) : LeakGuardHandlerWrapper<AppPermissionActivity>(activity) {
        fun cancelPollingImeSettings() {
            removeMessages(0)
        }

        override fun handleMessage(message: Message) {
            val ownerInstance = ownerInstance
            if (ownerInstance != null && message.what == 0) {
                if (ownerInstance.permissionFlow == 1 && ownerInstance.hasOverlayPermission()) {
                    ownerInstance.invokeSetupWizardOfThisIme()
                } else {
                    startPollingImeSettings()
                }
            }
        }

        fun startPollingImeSettings() {
            sendMessageDelayed(obtainMessage(0), 200L)
        }
    }
}