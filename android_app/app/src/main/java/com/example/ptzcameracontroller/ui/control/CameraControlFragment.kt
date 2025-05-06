package com.example.ptzcameracontroller.ui.control

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ptzcameracontroller.R
import com.example.ptzcameracontroller.data.repository.CameraControlRepository
import com.example.ptzcameracontroller.databinding.FragmentCameraControlBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CameraControlFragment : Fragment() {

    private var _binding: FragmentCameraControlBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: CameraControlViewModel
    
    // Coroutine for periodic connection checking
    private var connectionCheckJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraControlBinding.inflate(inflater, container, false)
        
        // Set up real ViewModel with repository
        val preferenceManager = PreferenceManager(requireContext())
        val lastIpAddress = preferenceManager.getLastIpAddress()
        val lastPort = preferenceManager.getLastPort()
        
        // Create the repository (create new instance to avoid issues with singleton pattern)
        val cameraControlRepository = CameraControlRepository()
        
        val factory = CameraControlViewModelFactory(cameraControlRepository)
        viewModel = ViewModelProvider(this, factory)[CameraControlViewModel::class.java]
        
        // Set up UI components
        setupJoystick()
        setupZoomSeekBar()
        setupCameraModeRadioButtons()
        setupPresetButtons()
        
        // Observe ViewModel
        observeViewModel()
        
        return binding.root
    }
    
    override fun onResume() {
        super.onResume()
        // Start connection check
        startConnectionCheck()
    }
    
    override fun onPause() {
        super.onPause()
        // Stop connection check
        stopConnectionCheck()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * Set up joystick for camera pan/tilt control
     */
    private fun setupJoystick() {
        binding.joystickView.setOnJoystickMoveListener { angle, strength ->
            // Convert polar coordinates (angle, strength) to Cartesian coordinates (x, y)
            // Angle is in degrees (0-360), strength is in percent (0-100)
            val radian = Math.toRadians(angle.toDouble())
            val x = (strength * Math.cos(radian)).toInt()
            val y = (strength * Math.sin(radian)).toInt()
            
            // Map to pan/tilt values (-100 to 100)
            // Pan is x-axis (left/right), inverted
            // Tilt is y-axis (up/down)
            val pan = -x
            val tilt = -y
            
            // Update ViewModel
            viewModel.setPanTilt(pan, tilt)
        }
    }
    
    /**
     * Set up zoom seek bar
     */
    private fun setupZoomSeekBar() {
        binding.seekBarZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Update ViewModel
                    viewModel.setZoom(progress)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not used
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not used
            }
        })
    }
    
    /**
     * Set up camera mode radio buttons
     */
    private fun setupCameraModeRadioButtons() {
        binding.radioGroupCameraMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radio_mode_rgb -> CameraMode.RGB
                R.id.radio_mode_ir -> CameraMode.IR
                else -> CameraMode.RGB
            }
            
            // Update ViewModel
            viewModel.setCameraMode(mode)
            
            // Show toast
            val modeString = when (mode) {
                CameraMode.RGB -> getString(R.string.mode_rgb)
                CameraMode.IR -> getString(R.string.mode_ir)
            }
            
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_camera_mode_changed, modeString),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Set up preset buttons
     */
    private fun setupPresetButtons() {
        // Save preset button
        binding.btnSavePreset.setOnClickListener {
            val presetNumberStr = binding.etPresetNumber.text.toString().trim()
            if (presetNumberStr.isEmpty()) {
                binding.etPresetNumber.error = "Enter preset number"
                return@setOnClickListener
            }
            
            val presetNumber = presetNumberStr.toIntOrNull()
            if (presetNumber == null || presetNumber < 1 || presetNumber > 255) {
                binding.etPresetNumber.error = "Enter valid preset (1-255)"
                return@setOnClickListener
            }
            
            // Save preset
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val success = viewModel.savePreset(presetNumber)
                    if (success) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.msg_preset_saved, presetNumber),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to save preset",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("CameraControlFragment", "Error saving preset", e)
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        
        // Go to preset button
        binding.btnGotoPreset.setOnClickListener {
            val presetNumberStr = binding.etPresetNumber.text.toString().trim()
            if (presetNumberStr.isEmpty()) {
                binding.etPresetNumber.error = "Enter preset number"
                return@setOnClickListener
            }
            
            val presetNumber = presetNumberStr.toIntOrNull()
            if (presetNumber == null || presetNumber < 1 || presetNumber > 255) {
                binding.etPresetNumber.error = "Enter valid preset (1-255)"
                return@setOnClickListener
            }
            
            // Go to preset
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val success = viewModel.gotoPreset(presetNumber)
                    if (success) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.msg_going_to_preset, presetNumber),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to go to preset",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("CameraControlFragment", "Error going to preset", e)
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    /**
     * Observe ViewModel
     */
    private fun observeViewModel() {
        // Observe pan position
        viewModel.panPosition.observe(viewLifecycleOwner) { pan ->
            binding.tvPanValue.text = pan.toString()
        }
        
        // Observe tilt position
        viewModel.tiltPosition.observe(viewLifecycleOwner) { tilt ->
            binding.tvTiltValue.text = tilt.toString()
        }
        
        // Observe zoom level
        viewModel.zoomLevel.observe(viewLifecycleOwner) { zoom ->
            binding.seekBarZoom.progress = zoom
            binding.tvZoomValue.text = zoom.toString()
        }
        
        // Observe camera mode
        viewModel.cameraMode.observe(viewLifecycleOwner) { mode ->
            val radioId = when (mode) {
                CameraMode.RGB -> R.id.radio_mode_rgb
                CameraMode.IR -> R.id.radio_mode_ir
            }
            
            if (binding.radioGroupCameraMode.checkedRadioButtonId != radioId) {
                binding.radioGroupCameraMode.check(radioId)
            }
        }
        
        // Observe connection status
        viewModel.isConnected.observe(viewLifecycleOwner) { connected ->
            updateConnectionStatus(connected)
        }
        
        // Observe connection mode
        viewModel.isUsingBluetooth.observe(viewLifecycleOwner) { usingBluetooth ->
            updateConnectionMode(usingBluetooth)
        }
    }
    
    /**
     * Update connection status UI
     * @param connected true if connected, false otherwise
     */
    private fun updateConnectionStatus(connected: Boolean) {
        val statusText = if (connected) "Connected" else "Disconnected"
        val statusColor = if (connected) {
            R.color.status_good
        } else {
            R.color.status_error
        }
        
        binding.tvConnectionStatus.text = statusText
        binding.tvConnectionStatus.setTextColor(requireContext().getColor(statusColor))
        
        // Enable/disable controls based on connection status
        binding.joystickView.isEnabled = connected
        binding.seekBarZoom.isEnabled = connected
        binding.radioModeRgb.isEnabled = connected
        binding.radioModeIr.isEnabled = connected
        binding.etPresetNumber.isEnabled = connected
        binding.btnSavePreset.isEnabled = connected
        binding.btnGotoPreset.isEnabled = connected
    }
    
    /**
     * Update connection mode UI
     * @param usingBluetooth true if using Bluetooth, false if using WiFi
     */
    private fun updateConnectionMode(usingBluetooth: Boolean) {
        val modeText = if (usingBluetooth) "Bluetooth" else "WiFi"
        val modeColor = if (usingBluetooth) {
            R.color.status_warning
        } else {
            R.color.status_good
        }
        
        binding.tvConnectionMode.text = modeText
        binding.tvConnectionMode.setTextColor(requireContext().getColor(modeColor))
    }
    
    /**
     * Start periodic connection check
     */
    private fun startConnectionCheck() {
        // First check immediately
        viewModel.checkConnectionStatus()
        
        // Then start periodic checking
        connectionCheckJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                while (isActive) {
                    delay(5000) // Check every 5 seconds
                    viewModel.pingServer()
                }
            } catch (e: Exception) {
                Log.e("CameraControlFragment", "Error in connection check", e)
            }
        }
    }
    
    /**
     * Stop periodic connection check
     */
    private fun stopConnectionCheck() {
        connectionCheckJob?.cancel()
        connectionCheckJob = null
    }
}