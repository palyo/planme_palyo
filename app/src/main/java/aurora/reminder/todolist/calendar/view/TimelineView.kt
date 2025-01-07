package aurora.reminder.todolist.calendar.view

import android.content.*
import android.graphics.*
import android.util.*
import android.view.*
import androidx.core.content.*
import androidx.core.content.res.*
import aurora.reminder.todolist.calendar.*
import aurora.reminder.todolist.calendar.extension.*
import aurora.reminder.todolist.calendar.model.*
import aurora.reminder.todolist.calendar.utils.*
import kotlin.math.*

class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textHourPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val extraTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tasks = mutableListOf<TimelineTask>()
    var hourWidth = 150f
    private var taskHeight = 100f
    var timelineHeight = 56f
    private var padding = 16f
    private var taskRadius = 16f
    private var taskSpacing = 16f
    var sideMargin = hourWidth / 2
    private val timeIndicator = TimeIndicator(this)
    private val timeUpdateHandler = TimeUpdateHandler(this)
    private val ZOOM_FACTOR = 1.2f
    private var MIN_HOUR_WIDTH = 60f
    private var MAX_HOUR_WIDTH = 300f

    init {
        MIN_HOUR_WIDTH = context.resources.getDimension(com.intuit.sdp.R.dimen._84sdp)
        MAX_HOUR_WIDTH = context.resources.getDimension(com.intuit.sdp.R.dimen._220sdp)
        hourWidth = context.resources.getDimension(com.intuit.sdp.R.dimen._84sdp)
        timelineHeight = context.resources.getDimension(com.intuit.sdp.R.dimen._1sdp)
        taskHeight = context.resources.getDimension(com.intuit.sdp.R.dimen._48sdp)
        padding = context.resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
        taskRadius = context.resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
        taskSpacing = context.resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
        sideMargin = hourWidth / 2
        setupPaints()
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        timeUpdateHandler.startUpdates()
    }

    override fun onDetachedFromWindow() {
        timeUpdateHandler.stopUpdates()
        super.onDetachedFromWindow()
    }

    private fun setupPaints() {
        textHourPaint.apply {
            textSize = context.resources.getDimension(com.intuit.ssp.R.dimen._8ssp)
            color = ContextCompat.getColor(context, R.color.colorText)
            typeface = ResourcesCompat.getFont(context, coder.apps.space.library.R.font.semi_bold)
            textAlign = Paint.Align.CENTER
        }

        textPaint.apply {
            textSize = context.resources.getDimension(com.intuit.ssp.R.dimen._10ssp)
            color = ContextCompat.getColor(context, R.color.colorInverseText)
            typeface = ResourcesCompat.getFont(context, coder.apps.space.library.R.font.semi_bold)
            textAlign = Paint.Align.CENTER
        }

        subtitlePaint.apply {
            textSize = context.resources.getDimension(com.intuit.ssp.R.dimen._7ssp)
            color = ContextCompat.getColor(context, R.color.colorInverseText)
            alpha = 180
            typeface = ResourcesCompat.getFont(context, coder.apps.space.library.R.font.regular)
            textAlign = Paint.Align.LEFT
        }
        extraTextPaint.apply {
            textSize = context.resources.getDimension(com.intuit.ssp.R.dimen._7ssp)
            color = ContextCompat.getColor(context, R.color.colorInverseText)
            alpha = 160
            typeface = ResourcesCompat.getFont(context, coder.apps.space.library.R.font.regular)
            textAlign = Paint.Align.LEFT
        }
        linePaint.apply {
            color = ContextCompat.getColor(context, R.color.colorIconOpacity12)
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(context.resources.getDimension(com.intuit.sdp.R.dimen._8sdp), context.resources.getDimension(com.intuit.sdp.R.dimen._8sdp)), 0f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        // Calculate minimum width for 24 hours
        val minWidth = (24 * hourWidth + sideMargin * 2).toInt()
        // Calculate content height with proper spacing
        val contentHeight = (
                (timelineHeight +
                        padding * 6 +
                        (taskHeight + taskSpacing) *
                        max(tasks.size, 1) +
                        taskSpacing * 3) + taskHeight
                ).toInt()
        // Use the larger of content height or parent height
        val minHeight = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> parentHeight
            else -> max(contentHeight, parentHeight)
        }

        setMeasuredDimension(minWidth, minHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTimeline(canvas)
        drawTasks(canvas)
        timeIndicator.draw(canvas)
    }

    private fun drawTimeline(canvas: Canvas) {
        for (hour in 0..24) {
            val x = sideMargin + hour * hourWidth
            canvas.drawLine(
                x,
                timelineHeight,
                x,
                height.toFloat(),
                linePaint
            )
        }
    }

    private fun drawTasks(canvas: Canvas) {
        tasks.forEachIndexed { index, task ->
            val startX = sideMargin + task.startTime.toPosition(hourWidth)
            val endX = sideMargin + task.endTime.toPosition(hourWidth)
            val top = timelineHeight + taskSpacing + index * (taskHeight + taskSpacing)
            val width = endX - startX
            // Draw task rectangle
            if (task.isCompleted) {
                paint.color = context.getColorCompat(R.color.colorCardBackground)
                textPaint.color = context.getColorCompat(R.color.colorText)
                subtitlePaint.color = context.getColorCompat(R.color.colorText)
                extraTextPaint.color = context.getColorCompat(R.color.colorText)

                textPaint.flags = (Paint.ANTI_ALIAS_FLAG or Paint.STRIKE_THRU_TEXT_FLAG) and Paint.UNDERLINE_TEXT_FLAG.inv()
                subtitlePaint.flags = (Paint.ANTI_ALIAS_FLAG or Paint.STRIKE_THRU_TEXT_FLAG) and Paint.UNDERLINE_TEXT_FLAG.inv()
                extraTextPaint.flags = (Paint.ANTI_ALIAS_FLAG or Paint.STRIKE_THRU_TEXT_FLAG) and Paint.UNDERLINE_TEXT_FLAG.inv()

                subtitlePaint.alpha = 180
                extraTextPaint.alpha = 160
            } else {
                paint.color = task.color
                textPaint.color = context.getColorCompat(R.color.colorInverseText)
                subtitlePaint.color = context.getColorCompat(R.color.colorInverseText)
                extraTextPaint.color = context.getColorCompat(R.color.colorInverseText)

                textPaint.flags = (Paint.ANTI_ALIAS_FLAG or Paint.STRIKE_THRU_TEXT_FLAG.inv()) and Paint.UNDERLINE_TEXT_FLAG.inv()
                subtitlePaint.flags = (Paint.ANTI_ALIAS_FLAG or Paint.STRIKE_THRU_TEXT_FLAG.inv()) and Paint.UNDERLINE_TEXT_FLAG.inv()
                extraTextPaint.flags = (Paint.ANTI_ALIAS_FLAG or Paint.STRIKE_THRU_TEXT_FLAG.inv()) and Paint.UNDERLINE_TEXT_FLAG.inv()

                subtitlePaint.alpha = 180
                extraTextPaint.alpha = 160
            }
            val rect = RectF(startX, top, endX, top + taskHeight)
            canvas.drawRoundRect(rect, taskRadius, taskRadius, paint)
            // Calculate text positions for vertical centering
            val textHeight = textPaint.textSize + (padding / 3) + subtitlePaint.textSize + extraTextPaint.textSize + (padding / 3)
            val startTextY = top + (taskHeight - textHeight) / 2
            // Draw task title
            textPaint.apply {
                textAlign = Paint.Align.LEFT
            }
            val titleY = startTextY + textPaint.textSize
            val ellipsizedTitle = TextUtils.ellipsizeText(
                task.title,
                textPaint,
                width - padding * 2
            )
            canvas.drawText(
                ellipsizedTitle,
                startX + padding,
                titleY,
                textPaint
            )
            // Draw task subtitle
            val subtitleY = titleY + (padding / 3) + subtitlePaint.textSize
            val ellipsizedSubtitle = TextUtils.ellipsizeText(
                task.body,
                subtitlePaint,
                width - padding * 2
            )
            if (task.body.isNotEmpty()) {
                canvas.drawText(
                    ellipsizedSubtitle,
                    startX + padding,
                    subtitleY,
                    subtitlePaint
                )
            }
            val extraTextY = subtitleY + (padding / 4) + extraTextPaint.textSize
            val ellipsizedExtraText = TextUtils.ellipsizeText(
                task.timestamp ?: "",
                extraTextPaint,
                width - padding * 2
            )
            if (task.timestamp.isNotEmpty()) {
                canvas.drawText(
                    ellipsizedExtraText,
                    startX + padding,
                    extraTextY,
                    extraTextPaint
                )
            }
        }
    }

    fun setTasks(newTasks: MutableList<TimelineTask>) {
        tasks.clear()
        tasks.addAll(newTasks)
        requestLayout()
        invalidate()
    }

    private var taskClickListener: TimelineTaskClickListener? = null
    private var touchedTask: TimelineTask? = null

    fun setTaskClickListener(listener: TimelineTaskClickListener) {
        taskClickListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchedTask = findTouchedTask(event.x, event.y)
                return touchedTask != null
            }

            MotionEvent.ACTION_UP -> {
                touchedTask?.let { task ->
                    if (findTouchedTask(event.x, event.y) == task) {
                        taskClickListener?.onTaskClick(task, event.x, event.y)
                    }
                }
                touchedTask = null
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findTouchedTask(x: Float, y: Float): TimelineTask? {
        tasks.forEachIndexed { index, task ->
            val startX = sideMargin + task.startTime.toPosition(hourWidth)
            val endX = sideMargin + task.endTime.toPosition(hourWidth)
            val top = timelineHeight + taskSpacing + index * (taskHeight + taskSpacing)
            val bottom = top + taskHeight

            if (x in startX..endX && y in top..bottom) {
                return task
            }
        }
        return null
    }
}