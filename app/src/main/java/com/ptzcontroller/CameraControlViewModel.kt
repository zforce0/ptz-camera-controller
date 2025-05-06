package com.ptzcontroller.ui.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptzcontroller.data.repository.CameraControlRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraControlViewModel(
    private val cameraControlRepository: CameraControlRepository
) : ViewModel() {

    // Pan position (-100 to 100, 0 is center)
    private val _panPosition = MutableLiveData<Int>().apply {
        value = 0
    }
    val panPosition: LiveData<Int> = _panPosition

    // Tilt position (-100 to 100, 0 is center)
    private val _tiltPosition = MutableLiveData<Int>().apply {
        value = 0
    }
    val tiltPosition: LiveData<Int> = _tiltPosition

    // Zoom level (0 to 100, 0 is wide angle)
    private val _zoomLevel = MutableLiveData<Int>().apply {
        value = 0
    }
    val zoomLevel: LiveData<Int> = _zoomLevel

    // Camera mode (RGB or IR)
    private val _cameraMode = MutableLiveData<CameraMode>().apply {
        value = CameraMode.RGB
    }
    val cameraMode: LiveData<CameraMode> = _cameraMode

    // Connection status
    private val _isConnected = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isConnected: LiveData<Boolean> = _isConnected

    // Connection mode (WiFi or Bluetooth)
    private val _isUsingBluetooth = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isUsingBluetooth: LiveData<Boolean> = _isUsingBluetooth
    
    // Connection status text
    private val _connectionStatus = MutableLiveData<String>().apply {
        value = "Disconnected"
    }
    val connectionStatus: LiveData<String> = _connectionStatus

    /**
     * Set pan and tilt position
     * @param pan Pan value (-100 to 100)
     * @param tilt Tilt value (-100 to 100)
     */
    fun setPanTilt(pan: Int, tilt: Int) {
        // Update local values
        _panPosition.value = pan
        _tiltPosition.value = tilt

        // Send to repository
        viewModelScope.launch {
            cameraControlRepository.sendPanTilt(pan, tilt)
        }
    }

    /**
     * Set zoom level
     * @param level Zoom level (0 to 100)
     */
    fun setZoom(level: Int) {
        // Update local value
        _zoomLevel.value = level

        // Send to repository
        viewModelScope.launch {
            cameraControlRepository.sendZoom(level)
        }
    }

    /**
     * Set camera mode
     * @param mode Camera mode (RGB or IR)
     */
    fun setCameraMode(mode: CameraMode) {
        // Update local value
        _cameraMode.value = mode

        // Send to repository
        viewModelScope.launch {
            cameraControlRepository.sendCameraMode(mode)
        }
    }

    /**
     * Save preset position
     * @param presetNumber Preset number (1-255)
     * @return true if successful, false otherwise
     */
    suspend fun savePreset(presetNumber: Int): Boolean = withContext(Dispatchers.IO) {
        cameraControlRepository.savePreset(presetNumber)
    }

    /**
     * Go to preset position
     * @param presetNumber Preset number (1-255)
     * @return true if successful, false otherwise
     */
    suspend fun gotoPreset(presetNumber: Int): Boolean = withContext(Dispatchers.IO) {
        cameraControlRepository.gotoPreset(presetNumber)
    }

    /**
     * Check connection status
     */
    fun checkConnectionStatus() {
        viewModelScope.launch {
            val connected = cameraControlRepository.isConnected()
            _isConnected.postValue(connected)

            val usingBluetooth = cameraControlRepository.isUsingBluetoothFallback()
            _isUsingBluetooth.postValue(usingBluetooth)
        }
    }

    /**
     * Ping server to maintain connection
     * @return true if connected, false otherwise
     */
    suspend fun pingServer(): Boolean = withContext(Dispatchers.IO) {
        val connected = cameraControlRepository.ping()
        _isConnected.postValue(connected)
        connected
    }
    
    /**
     * Handle joystick movement
     * Converts angle and strength to pan/tilt values
     * @param angle Angle in degrees (0-360, where 0 is up)
     * @param strength Strength (0-100)
     */
    fun handleJoystickMove(angle: Int, strength: Int) {
        // Convert angle and strength to pan and tilt values (-100 to 100)
        // For pan: 0° = 0, 90° = 100, 270° = -100
        // For tilt: 0° = -100, 180° = 100
        
        val panValue = when (angle) {
            in 0..90 -> (angle * strength) / 90
            in 91..180 -> (2 * 90 - angle) * strength / 90
            in 181..270 -> -((angle - 180) * strength) / 90
            else -> -((360 - angle) * strength) / 90
        }
        
        val tiltValue = when (angle) {
            in 0..180 -> -((180 - angle) * strength) / 90
            else -> ((angle - 180) * strength) / 90
        }
        
        // Clip values to -100 to 100 range
        val clippedPan = panValue.coerceIn(-100, 100)
        val clippedTilt = tiltValue.coerceIn(-100, 100)
        
        // Update the model and send to camera
        setPanTilt(clippedPan, clippedTilt)
    }
    
    /**
     * Handle joystick release (stop all movement)
     */
    fun handleJoystickRelease() {
        setPanTilt(0, 0)
    }
    
    /**
     * Set zoom level for slider
     */
    fun setZoomLevel(level: Int) {
        setZoom(level)
    }
    
    /**
     * Zoom in (increment zoom level)
     */
    fun zoomIn() {
        val currentZoom = _zoomLevel.value ?: 0
        val newZoom = (currentZoom + 10).coerceAtMost(100)
        setZoom(newZoom)
    }
    
    /**
     * Zoom out (decrement zoom level)
     */
    fun zoomOut() {
        val currentZoom = _zoomLevel.value ?: 0
        val newZoom = (currentZoom - 10).coerceAtLeast(0)
        setZoom(newZoom)
    }
}