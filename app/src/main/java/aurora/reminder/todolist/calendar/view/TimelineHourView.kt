package aurora.reminder.todolist.calendar.view

import android.content.*
import android.graphics.*
import android.util.*
import android.view.*
import androidx.core.content.*
import androidx.core.content.res.*
import aurora.reminder.todolist.calendar.*
import aurora.reminder.todolist.calendar.utils.*

class TimelineHourView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val textHourPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var hourWidth = context.resources.getDimension(com.intuit.sdp.R.dimen._84sdp)
    private var padding = context.resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
    var sideMargin = hourWidth / 2
    private val ZOOM_FACTOR = 1.2f
    private var MIN_HOUR_WIDTH = 60f
    private var MAX_HOUR_WIDTH = 300f

    init {
        MIN_HOUR_WIDTH = context.resources.getDimension(com.intuit.sdp.R.dimen._84sdp)
        MAX_HOUR_WIDTH = context.resources.getDimension(com.intuit.sdp.R.dimen._220sdp)
        setupPaint()
    }

    fun zoomIn() {
        hourWidth = (hourWidth * ZOOM_FACTOR).coerceIn(MIN_HOUR_WIDTH, MAX_HOUR_WIDTH)
        sideMargin = hourWidth / 2
        requestLayout()
        invalidate()
    }

    fun zoomOut() {
        hourWidth = (hourWidth / ZOOM_FACTOR).coerceIn(MIN_HOUR_WIDTH, MAX_HOUR_WIDTH)
        sideMargin = hourWidth / 2
        requestLayout()
        invalidate()
    }

    private fun setupPaint() {
        textHourPaint.apply {
            textSize = context.resources.getDimension(com.intuit.ssp.R.dimen._8ssp)
            color = ContextCompat.getColor(context, R.color.colorText)
            typeface = ResourcesCompat.getFont(context, coder.apps.space.library.R.font.semi_bold)
            textAlign = Paint.Align.CENTER
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = (textHourPaint.textSize + padding * 2).toInt()
        val width = (24 * hourWidth + sideMargin * 2).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawHours(canvas)
    }

    private fun drawHours(canvas: Canvas) {
        for (hour in 0..24) {
            val x = sideMargin + hour * hourWidth
            val timeText = TextUtils.formatTime(TimelineTime(hour, 0))
            canvas.drawText(
                timeText,
                x,
                height - padding,
                textHourPaint
            )
        }
    }
}
