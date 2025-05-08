
package com.ptzcontroller.ui.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptzcontroller.data.repository.CameraControlRepository
import com.ptzcontroller.ui.control.CameraControlViewModel
import kotlinx.coroutines.launch

class CameraControlViewModel(private val repository: CameraControlRepository) : ViewModel() {

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected

    fun sendPanTiltCommand(panSpeed: Int, tiltSpeed: Int) {
        viewModelScope.launch {
            repository.sendPanTiltCommand(panSpeed, tiltSpeed)
        }
    }

    fun stopPanTilt() {
        viewModelScope.launch {
            repository.stopPanTilt()
        }
    }

    fun zoomIn() {
        viewModelScope.launch {
            repository.zoomIn()
        }
    }

    fun zoomOut() {
        viewModelScope.launch {
            repository.zoomOut()
        }
    }

    fun setZoom(level: Int) {
        viewModelScope.launch {
            repository.setZoom(level)
        }
    }

    fun setCameraMode(isIRMode: Boolean) {
        viewModelScope.launch {
            repository.setCameraMode(isIRMode)
        }
    }

    fun savePreset(presetNumber: Int) {
        viewModelScope.launch {
            repository.savePreset(presetNumber)
        }
    }

    fun gotoPreset(presetNumber: Int) {
        viewModelScope.launch {
            repository.gotoPreset(presetNumber)
        }
    }

    fun checkConnectionStatus() {
        viewModelScope.launch {
            _isConnected.value = repository.isConnected()
        }
    }
}
