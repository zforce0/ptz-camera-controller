package com.example.ptzcameracontroller.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    // App version
    private val _appVersion = MutableLiveData<String>()
    val appVersion: LiveData<String> = _appVersion

    // Device name
    private val _deviceName = MutableLiveData<String>()
    val deviceName: LiveData<String> = _deviceName

    // Setting repository - will be injected later
    private lateinit var settingsRepository: SettingsRepository

    init {
        // Load device information
        loadDeviceInfo()
    }

    private fun loadDeviceInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get device name
                val deviceName = android.os.Build.MODEL
                _deviceName.postValue(deviceName)

                // Will use BuildConfig for actual version
                _appVersion.postValue("1.0.0")
            } catch (e: Exception) {
                // Handle error
                _deviceName.postValue("Unknown Device")
                _appVersion.postValue("Unknown")
            }
        }
    }

    // Function to reset all settings to defaults
    fun resetSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            // Will be implemented when repository is available
            // settingsRepository.resetToDefaults()
        }
    }

    // Function to export logs for troubleshooting
    fun exportLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            // Will be implemented when repository is available
            // settingsRepository.exportLogs()
        }
    }
}