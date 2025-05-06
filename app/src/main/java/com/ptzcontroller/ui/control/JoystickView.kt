package com.ptzcontroller.ui.control

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ptzcontroller.R
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint()
    private val handlePaint = Paint()
    private val borderPaint = Paint()

    private var centerX = 0f
    private var centerY = 0f
    private var baseRadius = 0f
    private var handleRadius = 0f

    private var handleX = 0f
    private var handleY = 0f

    private var moveListener: ((angle: Float, strength: Float) -> Unit)? = null

    var sensitivity: Float = 1.0f

    init {
        backgroundPaint.color = context.getColor(R.color.joystick_background)
        backgroundPaint.style = Paint.Style.FILL

        handlePaint.color = context.getColor(R.color.joystick_handle)
        handlePaint.style = Paint.Style.FILL

        borderPaint.color = context.getColor(R.color.control_pad_border)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 4f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        baseRadius = min(centerX, centerY) * 0.8f
        handleRadius = baseRadius * 0.3f
        handleX = centerX
        handleY = centerY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerX, centerY, baseRadius, backgroundPaint)
        canvas.drawCircle(centerX, centerY, baseRadius, borderPaint)
        canvas.drawCircle(handleX, handleY, handleRadius, handlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - centerX
                val dy = event.y - centerY
                val distance = hypot(dx, dy)
                val angle = (atan2(dy, dx) * 180 / Math.PI).toFloat()
                val strength = (distance / baseRadius).coerceAtMost(1.0f)

                if (distance > baseRadius) {
                    handleX = centerX + dx / distance * baseRadius
                    handleY = centerY + dy / distance * baseRadius
                } else {
                    handleX = event.x
                    handleY = event.y
                }

                moveListener?.invoke(angle, strength * sensitivity)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                handleX = centerX
                handleY = centerY
                moveListener?.invoke(0f, 0f)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setSensitivity(value: Float) {
        sensitivity = value
    }

    fun setOnMoveListener(listener: (angle: Float, strength: Float) -> Unit) {
        moveListener = listener
    }
}