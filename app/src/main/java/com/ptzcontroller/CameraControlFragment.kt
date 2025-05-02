package com.ptzcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment

class CameraControlFragment : Fragment(), View.OnClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener {

    private lateinit var connectionManager: ConnectionManager
    
    // Control buttons
    private lateinit var btnPanLeft: Button
    private lateinit var btnPanRight: Button
    private lateinit var btnTiltUp: Button
    private lateinit var btnTiltDown: Button
    private lateinit var btnZoomIn: Button
    private lateinit var btnZoomOut: Button
    private lateinit var toggleCameraMode: ToggleButton
    private lateinit var seekBarSpeed: SeekBar
    
    private var currentSpeed: Int = 50 // Default speed 50%
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            connectionManager = it.getSerializable("connectionManager") as ConnectionManager
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera_control, container, false)
        
        // Initialize buttons
        btnPanLeft = view.findViewById(R.id.btn_pan_left)
        btnPanRight = view.findViewById(R.id.btn_pan_right)
        btnTiltUp = view.findViewById(R.id.btn_tilt_up)
        btnTiltDown = view.findViewById(R.id.btn_tilt_down)
        btnZoomIn = view.findViewById(R.id.btn_zoom_in)
        btnZoomOut = view.findViewById(R.id.btn_zoom_out)
        toggleCameraMode = view.findViewById(R.id.toggle_camera_mode)
        seekBarSpeed = view.findViewById(R.id.seekbar_speed)
        
        // Set touch listeners for continuous movement
        btnPanLeft.setOnTouchListener(this)
        btnPanRight.setOnTouchListener(this)
        btnTiltUp.setOnTouchListener(this)
        btnTiltDown.setOnTouchListener(this)
        btnZoomIn.setOnTouchListener(this)
        btnZoomOut.setOnTouchListener(this)
        
        // Set click listener for mode toggle
        toggleCameraMode.setOnClickListener(this)
        
        // Set seek bar listener
        seekBarSpeed.setOnSeekBarChangeListener(this)
        
        return view
    }
    
    override fun onClick(v: View) {
        when (v.id) {
            R.id.toggle_camera_mode -> {
                val newMode = if (toggleCameraMode.isChecked) "ir" else "rgb"
                switchCameraMode(newMode)
            }
        }
    }
    
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (v.id) {
                    R.id.btn_pan_left -> sendCommand("pan", -currentSpeed)
                    R.id.btn_pan_right -> sendCommand("pan", currentSpeed)
                    R.id.btn_tilt_up -> sendCommand("tilt", currentSpeed)
                    R.id.btn_tilt_down -> sendCommand("tilt", -currentSpeed)
                    R.id.btn_zoom_in -> sendCommand("zoom", currentSpeed)
                    R.id.btn_zoom_out -> sendCommand("zoom", -currentSpeed)
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                when (v.id) {
                    R.id.btn_pan_left, R.id.btn_pan_right -> sendCommand("pan", 0)
                    R.id.btn_tilt_up, R.id.btn_tilt_down -> sendCommand("tilt", 0)
                    R.id.btn_zoom_in, R.id.btn_zoom_out -> sendCommand("zoom", 0)
                }
                return true
            }
        }
        return false
    }
    
    private fun sendCommand(type: String, value: Int) {
        if (!connectionManager.isConnected()) {
            Toast.makeText(context, "Not connected to camera", Toast.LENGTH_SHORT).show()
            return
        }
        
        val command = CameraCommand(type, value)
        connectionManager.sendCommand(command)
    }
    
    private fun switchCameraMode(mode: String) {
        if (!connectionManager.isConnected()) {
            Toast.makeText(context, "Not connected to camera", Toast.LENGTH_SHORT).show()
            return
        }
        
        val command = CameraCommand("mode", if (mode == "rgb") 0 else 1)
        connectionManager.sendCommand(command)
        
        Toast.makeText(
            context, 
            "Switching to ${if (mode == "rgb") "RGB" else "IR/Thermal"} mode", 
            Toast.LENGTH_SHORT
        ).show()
    }
    
    // SeekBar listener methods
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        currentSpeed = progress
    }
    
    override fun onStartTrackingTouch(seekBar: SeekBar) {
        // Not needed
    }
    
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        // Not needed
    }
}
