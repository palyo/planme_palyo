package aurora.reminder.todolist.calendar.activity.main

import android.graphics.drawable.*
import android.os.*
import android.view.*
import androidx.appcompat.app.*
import aurora.reminder.todolist.calendar.databinding.*

class PromptActivity : AppCompatActivity() {
    private var binding: ActivityPromptBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromptBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        @Suppress("DEPRECATION")
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            statusBarColor = getColor(coder.apps.space.library.R.color.colorBlack34)
            setBackgroundDrawable(ColorDrawable(getColor(coder.apps.space.library.R.color.colorBlack34)))
            setDimAmount(0.0F)
        }
        binding?.apply {
            buttonContinue.setOnClickListener {
                finish()
            }
        }
    }
}