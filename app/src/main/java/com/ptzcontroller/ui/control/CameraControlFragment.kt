package com.ptzcontroller.ui.control

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ptzcontroller.data.repository.CameraControlRepository
import com.ptzcontroller.CameraMode
import com.ptzcontroller.databinding.FragmentCameraControlBinding
import com.ptzcontroller.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class CameraControlFragment : Fragment() {

    private var _binding: FragmentCameraControlBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var cameraControlRepository: CameraControlRepository
    private lateinit var preferenceManager: PreferenceManager
    
    // Current control values
    private var currentPanSpeed = 0
    private var currentTiltSpeed = 0
    private var currentZoomLevel = 0
    private var currentCameraMode = CameraMode.RGB
    
    // Control sensitivity (0-100)
    private var controlSensitivity = 50
    private var zoomSensitivity = 50
    
    // Connection status check job
    private var connectionCheckJob: Job? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraControlBinding.inflate(inflater, container, false)
        
        // Initialize repositories
        cameraControlRepository = CameraControlRepository(requireContext())
        preferenceManager = PreferenceManager(requireContext())
        
        // Load control sensitivity
        controlSensitivity = preferenceManager.getControlSensitivity()
        zoomSensitivity = preferenceManager.getZoomSensitivity()
        
        setupControlInterface()
        
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * Set up the camera control interface
     */
    private fun setupControlInterface() {
        // Set up joystick
        binding.joystickView.setOnMoveListener { angle, strength ->
            handleJoystickMove(angle, strength)
        }
        
        // Set up zoom controls
        binding.btnZoomIn.setOnClickListener {
            setZoomLevel(currentZoomLevel + 5)
        }
        
        binding.btnZoomOut.setOnClickListener {
            setZoomLevel(currentZoomLevel - 5)
        }
        
        // Set up camera mode switch
        binding.switchCameraMode.setOnCheckedChangeListener { _, isChecked ->
            setCameraMode(if (isChecked) CameraMode.IR else CameraMode.RGB)
        }
        
        // Set up preset buttons
        setupPresetButtons()
    }
    
    /**
     * Set up preset buttons
     */
    private fun setupPresetButtons() {
        binding.btnPreset1.setOnClickListener { handlePresetClick(1) }
        binding.btnPreset2.setOnClickListener { handlePresetClick(2) }
        binding.btnPreset3.setOnClickListener { handlePresetClick(3) }
        binding.btnPreset4.setOnClickListener { handlePresetClick(4) }
        
        binding.btnPreset1.setOnLongClickListener { savePreset(1); true }
        binding.btnPreset2.setOnLongClickListener { savePreset(2); true }
        binding.btnPreset3.setOnLongClickListener { savePreset(3); true }
        binding.btnPreset4.setOnLongClickListener { savePreset(4); true }
    }
    
    /**
     * Handle joystick movement
     * @param angle Angle in degrees (0-360, 0 is right, 90 is down)
     * @param strength Strength of movement (0-100)
     */
    private fun handleJoystickMove(angle: Int, strength: Int) {
        // Convert angle to radians
        val radians = Math.toRadians(angle.toDouble())
        
        // Calculate X (pan) and Y (tilt) components
        // Apply control sensitivity
        val sensitivityFactor = controlSensitivity / 50.0
        val maxSpeed = 100 * sensitivityFactor
        
        // Calculate pan and tilt speeds
        // Pan: positive is right, negative is left
        // Tilt: positive is down, negative is up
        val panSpeed = (cos(radians) * strength * maxSpeed / 100.0).toInt()
        val tiltSpeed = (sin(radians) * strength * maxSpeed / 100.0).toInt()
        
        // Only send commands if values changed
        if (panSpeed != currentPanSpeed || tiltSpeed != currentTiltSpeed) {
            currentPanSpeed = panSpeed
            currentTiltSpeed = tiltSpeed
            
            // Update the UI
            updateSpeedDisplay()
            
            // Send control commands to the camera
            sendPanTiltCommands()
        }
    }
    
    /**
     * Update the speed display in the UI
     */
    private fun updateSpeedDisplay() {
        binding.tvPanSpeed.text = "Pan: $currentPanSpeed"
        binding.tvTiltSpeed.text = "Tilt: $currentTiltSpeed"
        binding.tvZoomLevel.text = "Zoom: $currentZoomLevel%"
    }
    
    /**
     * Send pan and tilt control commands to the camera
     */
    private fun sendPanTiltCommands() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cameraControlRepository.setPanSpeed(currentPanSpeed)
                cameraControlRepository.setTiltSpeed(currentTiltSpeed)
            } catch (e: Exception) {
                Log.e("CameraControlFragment", "Error sending pan/tilt commands", e)
            }
        }
    }
    
    /**
     * Set the zoom level
     * @param level Zoom level (0-100)
     */
    private fun setZoomLevel(level: Int) {
        // Clamp value to valid range
        val clampedLevel = level.coerceIn(0, 100)
        
        // Only send command if value changed
        if (clampedLevel != currentZoomLevel) {
            currentZoomLevel = clampedLevel
            
            // Update the UI
            updateSpeedDisplay()
            
            // Send zoom command to the camera
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    cameraControlRepository.setZoomLevel(currentZoomLevel)
                } catch (e: Exception) {
                    Log.e("CameraControlFragment", "Error sending zoom command", e)
                }
            }
        }
    }
    
    /**
     * Set the camera mode (RGB or IR)
     * @param mode Camera mode
     */
    private fun setCameraMode(mode: CameraMode) {
        // Only send command if value changed
        if (mode != currentCameraMode) {
            currentCameraMode = mode
            
            // Update UI
            binding.switchCameraMode.isChecked = mode == CameraMode.IR
            
            // Send camera mode command
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    cameraControlRepository.setCameraMode(currentCameraMode)
                } catch (e: Exception) {
                    Log.e("CameraControlFragment", "Error setting camera mode", e)
                }
            }
        }
    }
    
    /**
     * Handle preset button click (go to preset)
     * @param presetNumber Preset number (1-255)
     */
    private fun handlePresetClick(presetNumber: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val success = cameraControlRepository.gotoPreset(presetNumber)
                if (success) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "Moving to preset $presetNumber", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraControlFragment", "Error going to preset", e)
            }
        }
    }
    
    /**
     * Save current position as preset
     * @param presetNumber Preset number (1-255)
     */
    private fun savePreset(presetNumber: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val success = cameraControlRepository.savePreset(presetNumber)
                if (success) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "Position saved as preset $presetNumber", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraControlFragment", "Error saving preset", e)
            }
        }
    }
    
    companion object {
        /**
         * Create a new instance of the fragment
         */
        fun newInstance(): CameraControlFragment {
            return CameraControlFragment()
        }
    }
}