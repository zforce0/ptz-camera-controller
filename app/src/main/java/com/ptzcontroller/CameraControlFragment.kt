package com.ptzcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ptzcontroller.databinding.FragmentCameraControlBinding
import com.ptzcontroller.ui.control.JoystickView //Updated import
import com.ptzcontroller.utils.PreferenceManager

class CameraControlFragment : Fragment() {

    private var _binding: FragmentCameraControlBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CameraControlViewModel
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraControlBinding.inflate(inflater, container, false)

        // Initialize preference manager
        preferenceManager = PreferenceManager(requireContext())

        // Initialize ViewModel
        val cameraControlRepository = CameraControlRepository(requireContext())
        val factory = CameraControlViewModelFactory(cameraControlRepository)
        viewModel = ViewModelProvider(this, factory)[CameraControlViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the joystick
        setupJoystick()

        // Set up zoom control
        setupZoomControl()

        // Set up camera mode toggles
        setupCameraModeToggles()

        // Set up preset buttons
        setupPresetButtons()

        // Observe the ViewModel
        observeViewModel()
    }

    private fun setupJoystick() {
        // Set up joystick sensitivity from preferences
        val sensitivity = when (preferenceManager.getControlSensitivity()) {
            0 -> 0.5f  // Low sensitivity
            1 -> 1.0f  // Medium sensitivity
            2 -> 2.0f  // High sensitivity
            else -> 1.0f
        }

        binding.joystickView.setSensitivity(sensitivity)

        binding.joystickView.setOnMoveListener { angle, strength ->
            // Convert joystick angle and strength to pan/tilt values
            // Angle is in degrees (0-360, where 0 is up, 90 is right, etc.)
            // Strength is 0-100
            viewModel.handleJoystickMove(angle, strength)
        }

        binding.joystickView.setOnReleaseListener {
            // Stop movement when joystick is released
            viewModel.handleJoystickRelease()
        }
    }

    private fun setupZoomControl() {
        // Zoom in button
        binding.btnZoomIn.setOnClickListener {
            viewModel.zoomIn()
        }

        // Zoom out button
        binding.btnZoomOut.setOnClickListener {
            viewModel.zoomOut()
        }

        // Zoom slider
        binding.zoomSlider.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setZoomLevel(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                // Not used
            }

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                // Not used
            }
        })
    }

    private fun setupCameraModeToggles() {
        // RGB/IR toggle
        binding.rgbModeButton.setOnClickListener {
            viewModel.setCameraMode(CameraMode.RGB)
            updateCameraModeUI(CameraMode.RGB)
        }

        binding.irModeButton.setOnClickListener {
            viewModel.setCameraMode(CameraMode.IR)
            updateCameraModeUI(CameraMode.IR)
        }
    }

    private fun updateCameraModeUI(mode: CameraMode) {
        when (mode) {
            CameraMode.RGB -> {
                binding.rgbModeButton.isSelected = true
                binding.irModeButton.isSelected = false
            }
            CameraMode.IR -> {
                binding.rgbModeButton.isSelected = false
                binding.irModeButton.isSelected = true
            }
        }

        val modeText = if (mode == CameraMode.RGB) "RGB" else "IR"
        Toast.makeText(context, getString(R.string.msg_camera_mode_changed, modeText), Toast.LENGTH_SHORT).show()
    }

    private fun setupPresetButtons() {
        // Save preset
        binding.btnSavePreset.setOnClickListener {
            val presetNumber = binding.presetNumberInput.text.toString().toIntOrNull()
            if (presetNumber != null && presetNumber in 1..255) {
                viewModel.savePreset(presetNumber)
                Toast.makeText(context, getString(R.string.msg_preset_saved, presetNumber), Toast.LENGTH_SHORT).show()
            } else {
                binding.presetNumberInput.error = "Enter a number between 1 and 255"
            }
        }

        // Go to preset
        binding.btnGotoPreset.setOnClickListener {
            val presetNumber = binding.presetNumberInput.text.toString().toIntOrNull()
            if (presetNumber != null && presetNumber in 1..255) {
                viewModel.gotoPreset(presetNumber)
                Toast.makeText(context, getString(R.string.msg_going_to_preset, presetNumber), Toast.LENGTH_SHORT).show()
            } else {
                binding.presetNumberInput.error = "Enter a number between 1 and 255"
            }
        }
    }

    private fun observeViewModel() {
        // Observe connection status
        viewModel.connectionStatus.observe(viewLifecycleOwner) { status ->
            binding.connectionStatusText.text = status

            // Update UI based on connection status
            val isConnected = status == "Connected"
            binding.joystickView.isEnabled = isConnected
            binding.zoomSlider.isEnabled = isConnected
            binding.btnZoomIn.isEnabled = isConnected
            binding.btnZoomOut.isEnabled = isConnected
            binding.rgbModeButton.isEnabled = isConnected
            binding.irModeButton.isEnabled = isConnected
            binding.btnSavePreset.isEnabled = isConnected
            binding.btnGotoPreset.isEnabled = isConnected
        }

        // Observe zoom level
        viewModel.zoomLevel.observe(viewLifecycleOwner) { zoomLevel ->
            binding.zoomSlider.progress = zoomLevel
        }

        // Observe camera mode
        viewModel.cameraMode.observe(viewLifecycleOwner) { mode ->
            updateCameraModeUI(mode)
        }
    }

    override fun onResume() {
        super.onResume()
        // Check connection status
        viewModel.checkConnectionStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}