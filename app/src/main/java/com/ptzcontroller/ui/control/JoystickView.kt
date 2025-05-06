package com.ptzcontroller.ui.control

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Custom joystick view for camera control
 */
class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints for drawing
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.FILL
        alpha = 100
    }
    
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }
    
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    
    // Dimensions and positions
    private var centerX = 0f
    private var centerY = 0f
    private var baseRadius = 0f
    private var handleRadius = 0f
    
    // Current joystick position (relative to center)
    private var handlePosition = PointF(0f, 0f)
    
    // Is joystick being touched/moved
    private var isPressed = false
    
    // Listener for joystick movement
    private var onMoveListener: OnMoveListener? = null
    
    /**
     * Interface for joystick movement callbacks
     */
    interface OnMoveListener {
        /**
         * Called when joystick is moved
         * @param angle Angle in degrees (0-360, 0 is right, 90 is down)
         * @param strength Strength of movement (0-100)
         */
        fun onMove(angle: Int, strength: Int)
    }
    
    /**
     * Set listener for joystick movement
     */
    fun setOnMoveListener(listener: OnMoveListener) {
        onMoveListener = listener
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Calculate dimensions
        val minDimension = min(w, h)
        baseRadius = minDimension * 0.4f
        handleRadius = minDimension * 0.15f
        
        // Center position
        centerX = w / 2f
        centerY = h / 2f
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw base circle
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint)
        canvas.drawCircle(centerX, centerY, baseRadius, borderPaint)
        
        // Draw handle at current position
        val handleX = centerX + handlePosition.x
        val handleY = centerY + handlePosition.y
        canvas.drawCircle(handleX, handleY, handleRadius, handlePaint)
        canvas.drawCircle(handleX, handleY, handleRadius, borderPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if touch is within the base circle
                val touchX = event.x
                val touchY = event.y
                val distance = calculateDistance(touchX, touchY, centerX, centerY)
                
                if (distance <= baseRadius) {
                    isPressed = true
                    updateHandlePosition(touchX, touchY)
                    return true
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isPressed) {
                    updateHandlePosition(event.x, event.y)
                    return true
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isPressed) {
                    isPressed = false
                    // Reset handle position to center
                    handlePosition.set(0f, 0f)
                    invalidate()
                    
                    // Notify listener
                    onMoveListener?.onMove(0, 0)
                    return true
                }
            }
        }
        
        return super.onTouchEvent(event)
    }
    
    /**
     * Update handle position based on touch coordinates
     */
    private fun updateHandlePosition(touchX: Float, touchY: Float) {
        // Calculate distance from center
        val distance = calculateDistance(touchX, touchY, centerX, centerY)
        
        // Calculate direction vector
        val directionX = touchX - centerX
        val directionY = touchY - centerY
        
        // Normalize and scale to base radius
        val newX: Float
        val newY: Float
        
        if (distance > baseRadius) {
            // If beyond base radius, limit to the edge of the base
            val ratio = baseRadius / distance
            newX = directionX * ratio
            newY = directionY * ratio
        } else {
            // Within base radius, use actual position
            newX = directionX
            newY = directionY
        }
        
        // Update handle position
        handlePosition.set(newX, newY)
        invalidate()
        
        // Calculate angle and strength
        val angle = calculateAngle(newX, newY)
        val strength = ((distance / baseRadius) * 100).coerceIn(0f, 100f).toInt()
        
        // Notify listener
        onMoveListener?.onMove(angle, strength)
    }
    
    /**
     * Calculate distance between two points
     */
    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }
    
    /**
     * Calculate angle from x and y coordinates
     * @return Angle in degrees (0-360, 0 is right, 90 is down)
     */
    private fun calculateAngle(x: Float, y: Float): Int {
        val radians = kotlin.math.atan2(y, x)
        var degrees = Math.toDegrees(radians.toDouble()).toInt()
        
        // Convert to 0-360 range
        if (degrees < 0) {
            degrees += 360
        }
        
        return degrees
    }
}