package com.ptzcontroller.ui.control

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.min
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * Custom joystick control for camera movement
 */
class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paint for drawing
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }
    
    private val stickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }
    
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    
    // Joystick dimensions
    private var centerX = 0f
    private var centerY = 0f
    private var baseRadius = 0f
    private var stickRadius = 0f
    
    // Joystick position
    private var stickX = 0f
    private var stickY = 0f
    
    // Movement parameters
    private var angle = 0
    private var strength = 0
    private var sensitivity = 1.0f
    
    // Callbacks
    private var moveListener: ((angle: Int, strength: Int) -> Unit)? = null
    private var releaseListener: (() -> Unit)? = null
    
    // Enabled state
    private var _isEnabled = true

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Calculate dimensions based on view size
        val minDimension = min(w, h)
        centerX = w / 2f
        centerY = h / 2f
        baseRadius = minDimension / 3f
        stickRadius = baseRadius / 2f
        
        // Initialize stick position to center
        resetStickPosition()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw base
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint)
        canvas.drawCircle(centerX, centerY, baseRadius, borderPaint)
        
        // Draw stick
        canvas.drawCircle(stickX, stickY, stickRadius, stickPaint)
        canvas.drawCircle(stickX, stickY, stickRadius, borderPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!_isEnabled) {
            return false
        }
        
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Calculate distance from center
                val dx = event.x - centerX
                val dy = event.y - centerY
                val distance = hypot(dx, dy)
                
                if (distance <= baseRadius) {
                    // Stick is within base
                    stickX = event.x
                    stickY = event.y
                } else {
                    // Stick is outside base, constrain to edge
                    val ratio = baseRadius / distance
                    stickX = centerX + dx * ratio
                    stickY = centerY + dy * ratio
                }
                
                // Calculate angle and strength
                calculateAngleAndStrength()
                
                // Call move listener
                moveListener?.invoke(angle, strength)
                
                invalidate()
                return true
            }
            
            MotionEvent.ACTION_UP -> {
                // Reset stick position
                resetStickPosition()
                
                // Call release listener
                releaseListener?.invoke()
                
                invalidate()
                return true
            }
        }
        
        return super.onTouchEvent(event)
    }

    /**
     * Calculate angle and strength based on stick position
     */
    private fun calculateAngleAndStrength() {
        val dx = stickX - centerX
        val dy = stickY - centerY
        
        // Calculate angle in degrees (0-360, where 0 is up, 90 is right)
        angle = ((atan2(dx, -dy) * 180 / PI) + 360).roundToInt() % 360
        
        // Calculate strength (0-100)
        val distance = hypot(dx, dy)
        strength = ((distance / baseRadius) * 100 * sensitivity).roundToInt().coerceIn(0, 100)
    }

    /**
     * Reset stick position to center
     */
    private fun resetStickPosition() {
        stickX = centerX
        stickY = centerY
        angle = 0
        strength = 0
    }

    /**
     * Set move listener
     * @param listener Callback for joystick movement with angle and strength
     */
    fun setOnMoveListener(listener: (angle: Int, strength: Int) -> Unit) {
        moveListener = listener
    }

    /**
     * Set release listener
     * @param listener Callback for joystick release
     */
    fun setOnReleaseListener(listener: () -> Unit) {
        releaseListener = listener
    }

    /**
     * Set sensitivity
     * @param value Sensitivity multiplier (default 1.0)
     */
    fun setSensitivity(value: Float) {
        sensitivity = value.coerceIn(0.1f, 5.0f)
    }

    /**
     * Get the current angle in degrees (0-360)
     */
    fun getAngle() = angle

    /**
     * Get the current strength (0-100)
     */
    fun getStrength() = strength

    /**
     * Set enabled state
     */
    override fun setEnabled(enabled: Boolean) {
        _isEnabled = enabled
        super.setEnabled(enabled)
    }

    /**
     * Check if enabled
     */
    override fun isEnabled() = _isEnabled
}