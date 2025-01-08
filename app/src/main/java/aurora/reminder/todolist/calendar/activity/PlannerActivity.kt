package aurora.reminder.todolist.calendar.activity

import android.os.*
import android.view.*
import android.widget.*
import androidx.activity.*
import androidx.activity.result.contract.*
import androidx.core.view.*
import androidx.fragment.app.*
import androidx.lifecycle.*
import aurora.reminder.todolist.calendar.R
import aurora.reminder.todolist.calendar.database.*
import aurora.reminder.todolist.calendar.database.table.*
import aurora.reminder.todolist.calendar.databinding.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.fragment.*
import aurora.reminder.todolist.calendar.module.*
import coder.apps.space.library.base.*
import coder.apps.space.library.extension.*
import kotlinx.coroutines.*

class PlannerActivity : BaseActivity<ActivityPlannerBinding>(ActivityPlannerBinding::inflate) {
    private var doubleBackToExitPressedOnce = false
    private val notificationLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions -> }
    override fun ActivityPlannerBinding.initExtra() {
        HomeFragment.newInstance().updateFragment()
        setupBottomView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS))) {
            notificationLauncher.launch(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS))
        }

        viewNativeBanner(adBanner)
        adBanner.beGone()
    }

    private fun ActivityPlannerBinding.setupBottomView() {
        openDefaultNavigation()
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_home -> {
                    HomeFragment.newInstance().updateFragment()
                    return@setOnItemSelectedListener true
                }

                R.id.action_calendar -> {
                    CalendarFragment.newInstance().updateFragment()
                    return@setOnItemSelectedListener true
                }

                R.id.action_settings -> {
                    SettingFragment.newInstance().updateFragment()
                    return@setOnItemSelectedListener true
                }
            }
            return@setOnItemSelectedListener false
        }
    }

    private fun ActivityPlannerBinding.openDefaultNavigation() {
        when (intent?.getIntExtra(CURRENT_TAB, 0)) {
            2 -> {
                bottomNavigationView.selectedItemId = R.id.action_settings
                SettingFragment.newInstance().updateFragment()
            }

            1 -> {
                bottomNavigationView.selectedItemId = R.id.action_calendar
                CalendarFragment.newInstance().updateFragment()
            }

            else -> {
                bottomNavigationView.selectedItemId = R.id.action_home
                HomeFragment.newInstance().updateFragment()
            }
        }
    }

    override fun ActivityPlannerBinding.initListeners() {
        buttonCreate.setOnClickListener {
            layoutBottomCreateControl.beVisibleIf(!layoutBottomCreateControl.isVisible)
        }
        buttonCreateTask.setOnClickListener {
            layoutBottomCreateControl.beGone()
            go(AddTaskActivity::class.java)
        }
        buttonCreateWorkspace.setOnClickListener {
            layoutBottomCreateControl.beGone()
            viewCreateWorkspace {
                if (it.isEmpty()) {
                    Toast.makeText(this@PlannerActivity, "Workspace title cannot be empty. Try again!", Toast.LENGTH_SHORT).show()
                    return@viewCreateWorkspace
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    workspaceDao.insert(
                        Workspace(title = it, isSystemSpace = false),
                    )
                }
            }
        }
    }

    override fun ActivityPlannerBinding.initView() {
        updateNavigationBarColor(R.color.colorTransparent)
        onBackPressedDispatcher.addCallback {
            val currentFragment = supportFragmentManager.primaryNavigationFragment
            if (currentFragment !is HomeFragment) {
                bottomNavigationView.selectedItemId = R.id.action_home
                HomeFragment.newInstance().updateFragment()
                return@addCallback
            }
            if (doubleBackToExitPressedOnce) {
                finishAffinity()
                return@addCallback
            }

            this@PlannerActivity.doubleBackToExitPressedOnce = true
            Toast.makeText(this@PlannerActivity, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

            delayed(2000) { doubleBackToExitPressedOnce = false }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchedView = currentFocus
            if (touchedView != null && isTouchOutsideView(touchedView, event)) {
                if (touchedView.id != R.id.button_create) {
                    binding?.layoutBottomCreateControl?.beGone()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun isTouchOutsideView(view: View, event: MotionEvent): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        return x < location[0] || x > location[0] + view.width || y < location[1] || y > location[1] + view.height
    }

    private fun Fragment.updateFragment() {
        try {
            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            val currentFragment = fragmentManager.primaryNavigationFragment
            currentFragment?.let { transaction.hide(it) }
            val existingFragment = fragmentManager.findFragmentByTag(tag)
            if (existingFragment != null) {
                transaction.show(existingFragment)
                transaction.setPrimaryNavigationFragment(existingFragment)
            } else {
                transaction.add(R.id.fragment_container, this, javaClass.simpleName)
                transaction.setPrimaryNavigationFragment(this)
            }

            transaction.setReorderingAllowed(true)
            transaction.commitNowAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("SELECTED_ITEM_KEY", binding?.bottomNavigationView?.selectedItemId ?: 0)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val selectedItem = savedInstanceState.getInt("SELECTED_ITEM_KEY", 0)
        binding?.bottomNavigationView?.selectedItemId = selectedItem
    }
}