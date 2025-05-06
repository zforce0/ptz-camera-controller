package com.example.ptzcameracontroller.ui.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Camera modes
enum class CameraMode {
    RGB, IR
}

// Connection status
enum class ConnectionStatus(val description: String) {
    CONNECTED("Connected"),
    CONNECTING("Connecting..."),
    DISCONNECTED("Disconnected")
}

class CameraControlViewModel : ViewModel() {

    // Camera control repository - will be injected later
    private lateinit var cameraRepository: CameraControlRepository

    // Connection status
    private val _connectionStatus = MutableLiveData<ConnectionStatus>().apply {
        value = ConnectionStatus.DISCONNECTED
    }
    val connectionStatus: LiveData<ConnectionStatus> = _connectionStatus

    // Camera mode
    private val _cameraMode = MutableLiveData<CameraMode>().apply {
        value = CameraMode.RGB
    }
    val cameraMode: LiveData<CameraMode> = _cameraMode

    // Zoom level (0-100)
    private val _zoom = MutableLiveData<Int>().apply {
        value = 0
    }
    val zoom: LiveData<Int> = _zoom

    // Pan/tilt values (-100 to 100)
    private val _pan = MutableLiveData<Int>().apply {
        value = 0
    }
    val pan: LiveData<Int> = _pan

    private val _tilt = MutableLiveData<Int>().apply {
        value = 0
    }
    val tilt: LiveData<Int> = _tilt

    // Method to set pan/tilt values
    fun setPanTilt(pan: Int, tilt: Int) {
        _pan.value = pan
        _tilt.value = tilt
        
        // Send command to camera
        sendPanTiltCommand(pan, tilt)
    }

    // Method to set zoom level
    fun setZoom(zoomLevel: Int) {
        _zoom.value = zoomLevel
        
        // Send command to camera
        sendZoomCommand(zoomLevel)
    }

    // Method to zoom in (increase zoom level)
    fun zoomIn() {
        val currentZoom = _zoom.value ?: 0
        if (currentZoom < 100) {
            val newZoom = (currentZoom + 10).coerceAtMost(100)
            setZoom(newZoom)
        }
    }

    // Method to zoom out (decrease zoom level)
    fun zoomOut() {
        val currentZoom = _zoom.value ?: 0
        if (currentZoom > 0) {
            val newZoom = (currentZoom - 10).coerceAtLeast(0)
            setZoom(newZoom)
        }
    }

    // Method to set camera mode
    fun setCameraMode(mode: CameraMode) {
        _cameraMode.value = mode
        
        // Send command to camera
        sendCameraModeCommand(mode)
    }

    // Method to save a preset position
    fun savePreset(presetNumber: Int) {
        if (presetNumber in 1..255) {
            viewModelScope.launch(Dispatchers.IO) {
                // Send command to camera
                sendSavePresetCommand(presetNumber)
            }
        }
    }

    // Method to go to a preset position
    fun gotoPreset(presetNumber: Int) {
        if (presetNumber in 1..255) {
            viewModelScope.launch(Dispatchers.IO) {
                // Send command to camera
                sendGotoPresetCommand(presetNumber)
            }
        }
    }

    // Private methods to send commands to the camera

    private fun sendPanTiltCommand(pan: Int, tilt: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // Will be implemented when the repository is available
            // cameraRepository.sendPanTilt(pan, tilt)
        }
    }

    private fun sendZoomCommand(zoomLevel: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // Will be implemented when the repository is available
            // cameraRepository.sendZoom(zoomLevel)
        }
    }

    private fun sendCameraModeCommand(mode: CameraMode) {
        viewModelScope.launch(Dispatchers.IO) {
            // Will be implemented when the repository is available
            // cameraRepository.setCameraMode(mode)
        }
    }

    private fun sendSavePresetCommand(presetNumber: Int) {
        // Will be implemented when the repository is available
        // cameraRepository.savePreset(presetNumber)
    }

    private fun sendGotoPresetCommand(presetNumber: Int) {
        // Will be implemented when the repository is available
        // cameraRepository.gotoPreset(presetNumber)
    }
}