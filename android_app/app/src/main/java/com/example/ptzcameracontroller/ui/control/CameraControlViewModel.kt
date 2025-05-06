package com.example.ptzcameracontroller.ui.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ptzcameracontroller.data.repository.CameraControlRepository
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
}