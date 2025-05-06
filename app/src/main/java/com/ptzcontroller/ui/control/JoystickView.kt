
package com.ptzcontroller.ui.control

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
    private val paint = Paint()
    private var centerX = 0f
    private var centerY = 0f
    private var baseRadius = 0f
    private var hatRadius = 0f
    private var hatX = 0f
    private var hatY = 0f
    private var moveListener: ((angle: Int, strength: Int) -> Unit)? = null
    private var releaseListener: (() -> Unit)? = null
    
    init {
        paint.isAntiAlias = true
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        baseRadius = min(w, h) / 3f
        hatRadius = baseRadius / 2f
        hatX = centerX
        hatY = centerY
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw base
        paint.color = Color.GRAY
        canvas.drawCircle(centerX, centerY, baseRadius, paint)
        // Draw hat
        paint.color = Color.BLUE
        canvas.drawCircle(hatX, hatY, hatRadius, paint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val dx = event.x - centerX
                val dy = event.y - centerY
                val distance = hypot(dx, dy)
                
                if (distance < baseRadius) {
                    hatX = event.x
                    hatY = event.y
                } else {
                    val angle = atan2(dy, dx)
                    hatX = centerX + (baseRadius * kotlin.math.cos(angle))
                    hatY = centerY + (baseRadius * kotlin.math.sin(angle))
                }
                
                val angleInDegrees = (Math.toDegrees(atan2(dy, dx)) + 360) % 360
                val strength = ((distance / baseRadius) * 100).coerceIn(0f, 100f)
                moveListener?.invoke(angleInDegrees.toInt(), strength.toInt())
                
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                hatX = centerX
                hatY = centerY
                releaseListener?.invoke()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    fun setOnMoveListener(listener: (angle: Int, strength: Int) -> Unit) {
        moveListener = listener
    }
    
    fun setOnReleaseListener(listener: () -> Unit) {
        releaseListener = listener
    }
}
