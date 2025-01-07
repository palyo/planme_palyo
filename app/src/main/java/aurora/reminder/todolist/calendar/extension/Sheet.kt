package aurora.reminder.todolist.calendar.extension

import android.app.*
import coder.apps.space.library.extension.*
import com.google.android.material.bottomsheet.*
import aurora.reminder.todolist.calendar.databinding.*

fun Activity.viewCreateWorkspace(listener: (String) -> Unit) {
    val dialog = BottomSheetDialog(this, coder.apps.space.library.R.style.Theme_Space_BottomSheetDialogTheme)
    val bindDialog: LayoutSheetCreateWorkspaceBinding = LayoutSheetCreateWorkspaceBinding.inflate(layoutInflater)
    dialog.setContentView(bindDialog.root)
    dialog.window?.apply {
        applyDialogConfig()
        setDimAmount(.24f)
    }

    with(bindDialog) {
        buttonContinue.setOnClickListener {
            listener.invoke(editTitle.text.toString().trim())
            dialog.dismiss()
        }
    }

    if (!isFinishing || !isDestroyed) {
        dialog.show()
    }
}

fun Activity.viewPermissionSheet(icon: Int, title: Int, body: Int, listener: () -> Unit) {
    val dialog = BottomSheetDialog(this, coder.apps.space.library.R.style.Theme_Space_BottomSheetDialogTheme)
    val bindDialog: LayoutSheetPermissionBinding = LayoutSheetPermissionBinding.inflate(layoutInflater)
    dialog.setContentView(bindDialog.root)
    dialog.window?.apply {
        applyDialogConfig()
        setDimAmount(.24f)
    }

    with(bindDialog) {
        iconPermission.setImageDrawable(getDrawableCompat(icon))
        textPermission.text = getString(title)
        textPermissionBody.text = getString(body)
        buttonContinue.setOnClickListener {
            listener.invoke()
            dialog.dismiss()
        }
    }

    if (!isFinishing || !isDestroyed) {
        dialog.show()
    }
}