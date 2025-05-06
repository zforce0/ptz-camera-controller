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

/**
 * Custom joystick view for controlling PTZ camera pan and tilt movements
 */
class JoystickView : View {
    
    // Paint objects for drawing
    private val backgroundPaint = Paint()
    private val handlePaint = Paint()
    private val borderPaint = Paint()
    
    // Joystick position and size
    private var centerX = 0f
    private var centerY = 0f
    private var baseRadius = 0f
    private var handleRadius = 0f
    
    // Current handle position
    private var handleX = 0f
    private var handleY = 0f
    
    // Movement listener
    private var moveListener: ((angle: Float, strength: Float) -> Unit)? = null
    
    // Sensitivity multiplier
    var sensitivity: Float = 1.0f
    
    // Constructors
    constructor(context: Context) : super(context) {
        init(context)
    }
    
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }
    
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }
    
    // Initialize the view
    private fun init(context: Context) {
        // Set up background paint
        backgroundPaint.color = context.getColor(R.color.joystick_background)
        backgroundPaint.style = Paint.Style.FILL
        
        // Set up handle paint
        handlePaint.color = context.getColor(R.color.joystick_handle)
        handlePaint.style = Paint.Style.FILL
        
        // Set up border paint
        borderPaint.color = context.getColor(R.color.control_pad_border)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 4f
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Calculate the center of the view
        centerX = w / 2f
        centerY = h / 2f
        
        // Calculate radius of the base (the smaller of width/2 or height/2)
        baseRadius = min(centerX, centerY) * 0.8f
        
        // Handle is 30% the size of the base
        handleRadius = baseRadius * 0.3f
        
        // Initialize handle at center
        handleX = centerX
        handleY = centerY
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw the base circle
        canvas.drawCircle(centerX, centerY, baseRadius, backgroundPaint)
        canvas.drawCircle(centerX, centerY, baseRadius, borderPaint)
        
        // Draw the joystick handle
        canvas.drawCircle(handleX, handleY, handleRadius, handlePaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                // Calculate distance from center
                val dx = event.x - centerX
                val dy = event.y - centerY
                val distance = hypot(dx, dy)
                
                // Calculate angle in degrees (0-360)
                val angle = (atan2(dy, dx) * 180 / Math.PI).toFloat()
                
                // Calculate strength as distance / baseRadius, capped at 1.0
                val strength = (distance / baseRadius).coerceAtMost(1.0f)
                
                // Update handle position
                if (distance > baseRadius) {
                    // If outside the base radius, cap at the edge
                    val constrainedDx = dx / distance * baseRadius
                    val constrainedDy = dy / distance * baseRadius
                    handleX = centerX + constrainedDx
                    handleY = centerY + constrainedDy
                } else {
                    // If within the base radius, use actual position
                    handleX = event.x
                    handleY = event.y
                }
                
                // Notify listener
                moveListener?.invoke(angle, strength * sensitivity)
                
                // Redraw
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                // Return handle to center
                handleX = centerX
                handleY = centerY
                
                // Notify listener of zero movement
                moveListener?.invoke(0f, 0f)
                
                // Redraw
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    /**
     * Set a listener for joystick movements
     * 
     * @param listener Lambda that receives angle (0-360 degrees) and strength (0.0-1.0)
     */
    fun setOnMoveListener(listener: (angle: Float, strength: Float) -> Unit) {
        this.moveListener = listener
    }
}