
package com.ptzcontroller.ui.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ptzcontroller.CameraControlRepository
import com.ptzcontroller.CameraMode

class CameraControlViewModel(
    private val repository: CameraControlRepository
) : ViewModel() {
    private val _connectionStatus = MutableLiveData<String>()
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _zoomLevel = MutableLiveData<Int>()
    val zoomLevel: LiveData<Int> = _zoomLevel

    private val _cameraMode = MutableLiveData<CameraMode>()
    val cameraMode: LiveData<CameraMode> = _cameraMode

    fun handleJoystickMove(angle: Int, strength: Int) {
        repository.sendPanTiltCommand(angle, strength)
    }

    fun handleJoystickRelease() {
        repository.stopPanTilt()
    }

    fun zoomIn() {
        repository.zoomIn()
        _zoomLevel.value = (_zoomLevel.value ?: 0) + 1
    }

    fun zoomOut() {
        repository.zoomOut()
        _zoomLevel.value = (_zoomLevel.value ?: 0) - 1
    }

    fun setZoomLevel(level: Int) {
        repository.setZoom(level)
        _zoomLevel.value = level
    }

    fun setCameraMode(mode: CameraMode) {
        repository.setCameraMode(mode)
        _cameraMode.value = mode
    }

    fun savePreset(number: Int) {
        repository.savePreset(number)
    }

    fun gotoPreset(number: Int) {
        repository.gotoPreset(number)
    }

    fun checkConnectionStatus() {
        _connectionStatus.value = if (repository.isConnected()) "Connected" else "Disconnected"
    }
}
