package com.ptzcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

/**
 * Dummy implementation of the camera control fragment
 * Used for compilation purposes while the real implementation is being developed
 */
class DummyCameraControlFragment : Fragment() {
    
    private lateinit var connectionManager: ConnectionManager
    
    // UI components
    private lateinit var panUpButton: Button
    private lateinit var panDownButton: Button
    private lateinit var panLeftButton: Button
    private lateinit var panRightButton: Button
    private lateinit var zoomSeekBar: SeekBar
    private lateinit var rgbModeRadio: RadioButton
    private lateinit var irModeRadio: RadioButton
    private lateinit var statusText: TextView
    
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
    ): View {
        // Create a simple layout with controls
        val rootView = LinearLayout(requireContext())
        rootView.orientation = LinearLayout.VERTICAL
        rootView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        rootView.setPadding(16, 16, 16, 16)
        
        // Status text
        statusText = TextView(requireContext())
        statusText.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        statusText.text = getString(R.string.dummy_text)
        statusText.textSize = 16f
        statusText.setPadding(0, 0, 0, 16)
        rootView.addView(statusText)
        
        // Pan/Tilt Control
        val panTiltTitle = TextView(requireContext())
        panTiltTitle.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        panTiltTitle.text = getString(R.string.pan_tilt_control)
        panTiltTitle.textSize = 18f
        panTiltTitle.setPadding(0, 16, 0, 16)
        rootView.addView(panTiltTitle)
        
        // Pan/Tilt buttons
        val panTiltLayout = LinearLayout(requireContext())
        panTiltLayout.orientation = LinearLayout.VERTICAL
        panTiltLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Top row - Up button
        val topRow = LinearLayout(requireContext())
        topRow.orientation = LinearLayout.HORIZONTAL
        topRow.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        topRow.gravity = android.view.Gravity.CENTER
        
        panUpButton = Button(requireContext())
        panUpButton.text = "▲"
        panUpButton.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        panUpButton.setOnClickListener {
            connectionManager.sendTiltCommand(-50)
        }
        topRow.addView(panUpButton)
        panTiltLayout.addView(topRow)
        
        // Middle row - Left, Right buttons
        val middleRow = LinearLayout(requireContext())
        middleRow.orientation = LinearLayout.HORIZONTAL
        middleRow.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        middleRow.gravity = android.view.Gravity.CENTER
        
        panLeftButton = Button(requireContext())
        panLeftButton.text = "◀"
        panLeftButton.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val leftParams = panLeftButton.layoutParams as LinearLayout.LayoutParams
        leftParams.marginEnd = 48
        panLeftButton.layoutParams = leftParams
        panLeftButton.setOnClickListener {
            connectionManager.sendPanCommand(-50)
        }
        middleRow.addView(panLeftButton)
        
        panRightButton = Button(requireContext())
        panRightButton.text = "▶"
        panRightButton.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val rightParams = panRightButton.layoutParams as LinearLayout.LayoutParams
        rightParams.marginStart = 48
        panRightButton.layoutParams = rightParams
        panRightButton.setOnClickListener {
            connectionManager.sendPanCommand(50)
        }
        middleRow.addView(panRightButton)
        
        panTiltLayout.addView(middleRow)
        
        // Bottom row - Down button
        val bottomRow = LinearLayout(requireContext())
        bottomRow.orientation = LinearLayout.HORIZONTAL
        bottomRow.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        bottomRow.gravity = android.view.Gravity.CENTER
        
        panDownButton = Button(requireContext())
        panDownButton.text = "▼"
        panDownButton.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        panDownButton.setOnClickListener {
            connectionManager.sendTiltCommand(50)
        }
        bottomRow.addView(panDownButton)
        
        panTiltLayout.addView(bottomRow)
        rootView.addView(panTiltLayout)
        
        // Zoom Control
        val zoomTitle = TextView(requireContext())
        zoomTitle.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        zoomTitle.text = getString(R.string.zoom_control)
        zoomTitle.textSize = 18f
        zoomTitle.setPadding(0, 24, 0, 8)
        rootView.addView(zoomTitle)
        
        zoomSeekBar = SeekBar(requireContext())
        zoomSeekBar.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        zoomSeekBar.max = 100
        zoomSeekBar.progress = 0
        zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    connectionManager.sendZoomCommand(progress)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        rootView.addView(zoomSeekBar)
        
        // Camera Mode
        val modeTitle = TextView(requireContext())
        modeTitle.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        modeTitle.text = getString(R.string.camera_mode)
        modeTitle.textSize = 18f
        modeTitle.setPadding(0, 24, 0, 8)
        rootView.addView(modeTitle)
        
        val radioGroup = RadioGroup(requireContext())
        radioGroup.orientation = RadioGroup.HORIZONTAL
        radioGroup.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        rgbModeRadio = RadioButton(requireContext())
        rgbModeRadio.text = getString(R.string.rgb_mode)
        rgbModeRadio.layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            RadioGroup.LayoutParams.WRAP_CONTENT
        )
        rgbModeRadio.isChecked = true
        rgbModeRadio.setOnClickListener {
            connectionManager.sendCameraModeCommand(0)
        }
        radioGroup.addView(rgbModeRadio)
        
        irModeRadio = RadioButton(requireContext())
        irModeRadio.text = getString(R.string.ir_mode)
        irModeRadio.layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            RadioGroup.LayoutParams.WRAP_CONTENT
        )
        val irParams = irModeRadio.layoutParams as RadioGroup.LayoutParams
        irParams.marginStart = 16
        irModeRadio.layoutParams = irParams
        irModeRadio.setOnClickListener {
            connectionManager.sendCameraModeCommand(1)
        }
        radioGroup.addView(irModeRadio)
        
        rootView.addView(radioGroup)
        
        return rootView
    }
    
    override fun onResume() {
        super.onResume()
        updateControlsState()
    }
    
    private fun updateControlsState() {
        val enabled = connectionManager.isConnected()
        
        panUpButton.isEnabled = enabled
        panDownButton.isEnabled = enabled
        panLeftButton.isEnabled = enabled
        panRightButton.isEnabled = enabled
        zoomSeekBar.isEnabled = enabled
        rgbModeRadio.isEnabled = enabled
        irModeRadio.isEnabled = enabled
        
        statusText.text = if (enabled) {
            "Connected to: ${connectionManager.getConnectedDeviceName()}"
        } else {
            getString(R.string.status_not_connected)
        }
    }
    
    companion object {
        @JvmStatic
        fun newInstance(connectionManager: ConnectionManager): DummyCameraControlFragment {
            val fragment = DummyCameraControlFragment()
            val args = Bundle()
            args.putSerializable("connectionManager", connectionManager)
            fragment.arguments = args
            return fragment
        }
    }
}