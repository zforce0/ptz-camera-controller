package com.ptzcontroller.ui.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ptzcontroller.data.repository.CameraControlRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * ViewModel for camera control
 */
class CameraControlViewModel(
    private val repository: CameraControlRepository
) : ViewModel() {

    private val _connectionStatus = MutableLiveData<String>()
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _zoomLevel = MutableLiveData<Int>()
    val zoomLevel: LiveData<Int> = _zoomLevel

    private val _cameraMode = MutableLiveData<CameraMode>()
    val cameraMode: LiveData<CameraMode> = _cameraMode

    private var panSpeed = 0
    private var tiltSpeed = 0
    private var controlJob: Job? = null
    private val controlScope = CoroutineScope(Dispatchers.Default)

    init {
        _connectionStatus.value = "Disconnected"
        _zoomLevel.value = 0
        _cameraMode.value = CameraMode.RGB
        checkConnectionStatus()
    }

    /**
     * Check connection status with the server
     */
    fun checkConnectionStatus() {
        controlScope.launch {
            val isConnected = repository.checkConnection()
            _connectionStatus.postValue(if (isConnected) "Connected" else "Disconnected")
        }
    }

    /**
     * Handle joystick movement
     * @param angle Angle in degrees (0-360, where 0 is up, 90 is right)
     * @param strength Strength (0-100)
     */
    fun handleJoystickMove(angle: Int, strength: Int) {
        // Cancel any previous control job
        controlJob?.cancel()

        // Convert angle to radians
        val radians = angle * PI / 180.0

        // Calculate pan and tilt speeds
        // Pan is left-right (-63 to 63), tilt is up-down (-63 to 63)
        val normalizedStrength = strength / 100.0
        panSpeed = (normalizedStrength * 63.0 * sin(radians)).toInt()
        tiltSpeed = (-normalizedStrength * 63.0 * cos(radians)).toInt()

        // Start continuous control
        controlJob = controlScope.launch {
            while (true) {
                repository.controlPanTilt(panSpeed, tiltSpeed)
                kotlinx.coroutines.delay(100) // Update every 100ms
            }
        }
    }

    /**
     * Handle joystick release
     */
    fun handleJoystickRelease() {
        controlJob?.cancel()
        controlScope.launch {
            repository.stopMovement()
            panSpeed = 0
            tiltSpeed = 0
        }
    }

    /**
     * Zoom in camera
     */
    fun zoomIn() {
        controlScope.launch {
            repository.zoomIn()
            // Update zoom level if available
            val newZoomLevel = _zoomLevel.value?.plus(10)?.coerceAtMost(100)
            if (newZoomLevel != null) {
                _zoomLevel.postValue(newZoomLevel)
            }
        }
    }

    /**
     * Zoom out camera
     */
    fun zoomOut() {
        controlScope.launch {
            repository.zoomOut()
            // Update zoom level if available
            val newZoomLevel = _zoomLevel.value?.minus(10)?.coerceAtLeast(0)
            if (newZoomLevel != null) {
                _zoomLevel.postValue(newZoomLevel)
            }
        }
    }

    /**
     * Set zoom level
     * @param level Zoom level (0-100)
     */
    fun setZoomLevel(level: Int) {
        val newLevel = level.coerceIn(0, 100)
        controlScope.launch {
            repository.setZoom(newLevel)
            _zoomLevel.postValue(newLevel)
        }
    }

    /**
     * Set camera mode (RGB or IR)
     * @param mode Camera mode
     */
    fun setCameraMode(mode: CameraMode) {
        controlScope.launch {
            val success = repository.setCameraMode(mode)
            if (success) {
                _cameraMode.postValue(mode)
            }
        }
    }

    /**
     * Save preset position
     * @param presetNumber Preset number (1-255)
     */
    fun savePreset(presetNumber: Int) {
        controlScope.launch {
            repository.savePreset(presetNumber)
        }
    }

    /**
     * Go to preset position
     * @param presetNumber Preset number (1-255)
     */
    fun gotoPreset(presetNumber: Int) {
        controlScope.launch {
            repository.gotoPreset(presetNumber)
        }
    }

    override fun onCleared() {
        super.onCleared()
        controlJob?.cancel()
    }
}

/**
 * Factory for creating CameraControlViewModel
 */
class CameraControlViewModelFactory(
    private val repository: CameraControlRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraControlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraControlViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}