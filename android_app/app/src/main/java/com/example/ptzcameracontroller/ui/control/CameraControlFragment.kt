package com.example.ptzcameracontroller.ui.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ptzcameracontroller.R
import com.example.ptzcameracontroller.databinding.FragmentCameraControlBinding
import com.example.ptzcameracontroller.utils.PreferenceManager

class CameraControlFragment : Fragment() {

    private var _binding: FragmentCameraControlBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: CameraControlViewModel
    private lateinit var preferenceManager: PreferenceManager
    
    // JoystickView will be created in a separate class
    private lateinit var joystickView: JoystickView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(CameraControlViewModel::class.java)
        _binding = FragmentCameraControlBinding.inflate(inflater, container, false)
        
        preferenceManager = PreferenceManager(requireContext())
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize joystick
        setupJoystick()
        
        // Initialize connection status
        setupConnectionStatus()
        
        // Initialize zoom controls
        setupZoomControls()
        
        // Initialize camera mode controls
        setupCameraModeControls()
        
        // Initialize preset position controls
        setupPresetControls()
    }
    
    private fun setupJoystick() {
        // Create and add the joystick view to the container
        joystickView = JoystickView(requireContext())
        binding.joystickContainer.addView(joystickView)
        
        // Set the sensitivity based on user preference
        val sensitivity = when (preferenceManager.getControlSensitivity()) {
            0 -> 0.5f // Low sensitivity
            1 -> 1.0f // Medium sensitivity
            2 -> 1.5f // High sensitivity
            else -> 1.0f
        }
        joystickView.sensitivity = sensitivity
        
        // Set up joystick movement listener
        joystickView.setOnMoveListener { angle, strength ->
            // Calculate pan and tilt values based on angle and strength
            // Pan is the X-axis movement (cos of angle) and tilt is the Y-axis movement (sin of angle)
            val pan = (Math.cos(Math.toRadians(angle.toDouble())) * strength * 100).toInt()
            val tilt = -(Math.sin(Math.toRadians(angle.toDouble())) * strength * 100).toInt()
            
            // Send pan/tilt values to the view model
            viewModel.setPanTilt(pan, tilt)
        }
    }
    
    private fun setupConnectionStatus() {
        // Observe connection status changes
        viewModel.connectionStatus.observe(viewLifecycleOwner) { status ->
            binding.connectionStatus.text = status.description
            binding.connectionStatus.setTextColor(
                resources.getColor(
                    when (status) {
                        ConnectionStatus.CONNECTED -> R.color.status_good
                        ConnectionStatus.CONNECTING -> R.color.status_warning
                        ConnectionStatus.DISCONNECTED -> R.color.status_idle
                    }, null
                )
            )
        }
        
        // Observe camera mode changes
        viewModel.cameraMode.observe(viewLifecycleOwner) { mode ->
            binding.cameraMode.text = when (mode) {
                CameraMode.RGB -> getString(R.string.mode_rgb)
                CameraMode.IR -> getString(R.string.mode_ir)
            }
            binding.cameraMode.setTextColor(
                resources.getColor(
                    when (mode) {
                        CameraMode.RGB -> R.color.mode_rgb
                        CameraMode.IR -> R.color.mode_ir
                    }, null
                )
            )
        }
    }
    
    private fun setupZoomControls() {
        // Set up zoom in button
        binding.btnZoomIn.setOnClickListener {
            viewModel.zoomIn()
        }
        
        // Set up zoom out button
        binding.btnZoomOut.setOnClickListener {
            viewModel.zoomOut()
        }
        
        // Set up zoom slider
        binding.zoomSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setZoom(progress)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Observe zoom changes
        viewModel.zoom.observe(viewLifecycleOwner) { zoom ->
            binding.zoomSlider.progress = zoom
        }
    }
    
    private fun setupCameraModeControls() {
        // Set up camera mode radio buttons
        binding.cameraModeGroup.setOnCheckedChangeListener { group, checkedId ->
            val mode = when (checkedId) {
                R.id.mode_rgb -> CameraMode.RGB
                R.id.mode_ir -> CameraMode.IR
                else -> CameraMode.RGB
            }
            viewModel.setCameraMode(mode)
            
            // Display toast notification
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_camera_mode_changed, mode.toString()),
                Toast.LENGTH_SHORT
            ).show()
        }
        
        // Initialize radio button state based on view model
        viewModel.cameraMode.observe(viewLifecycleOwner) { mode ->
            val buttonId = when (mode) {
                CameraMode.RGB -> R.id.mode_rgb
                CameraMode.IR -> R.id.mode_ir
            }
            binding.cameraModeGroup.check(buttonId)
        }
    }
    
    private fun setupPresetControls() {
        // Set up save preset button
        binding.btnSavePreset.setOnClickListener {
            val presetNumber = binding.presetNumber.text.toString().toIntOrNull()
            if (presetNumber != null && presetNumber in 1..255) {
                viewModel.savePreset(presetNumber)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_preset_saved, presetNumber),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid preset number (1-255)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        // Set up go to preset button
        binding.btnGotoPreset.setOnClickListener {
            val presetNumber = binding.presetNumber.text.toString().toIntOrNull()
            if (presetNumber != null && presetNumber in 1..255) {
                viewModel.gotoPreset(presetNumber)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_going_to_preset, presetNumber),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid preset number (1-255)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}