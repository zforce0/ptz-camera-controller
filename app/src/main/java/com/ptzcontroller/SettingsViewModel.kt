package com.ptzcontroller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ptzcontroller.utils.PreferenceManager

class SettingsViewModel : ViewModel() {

    // Setting values
    private val _darkMode = MutableLiveData<Boolean>()
    val darkMode: LiveData<Boolean> = _darkMode
    
    private val _keepScreenOn = MutableLiveData<Boolean>()
    val keepScreenOn: LiveData<Boolean> = _keepScreenOn
    
    private val _streamQuality = MutableLiveData<Int>()
    val streamQuality: LiveData<Int> = _streamQuality
    
    private val _showStatusOverlay = MutableLiveData<Boolean>()
    val showStatusOverlay: LiveData<Boolean> = _showStatusOverlay
    
    private val _autoReconnect = MutableLiveData<Boolean>()
    val autoReconnect: LiveData<Boolean> = _autoReconnect
    
    private val _bluetoothFallback = MutableLiveData<Boolean>()
    val bluetoothFallback: LiveData<Boolean> = _bluetoothFallback
    
    private val _connectionCheckInterval = MutableLiveData<Int>()
    val connectionCheckInterval: LiveData<Int> = _connectionCheckInterval
    
    private val _controlSensitivity = MutableLiveData<Int>()
    val controlSensitivity: LiveData<Int> = _controlSensitivity
    
    private val _wifiTimeout = MutableLiveData<Int>()
    val wifiTimeout: LiveData<Int> = _wifiTimeout
    
    /**
     * Initialize settings from PreferenceManager
     * @param preferenceManager PreferenceManager instance
     */
    fun loadSettings(preferenceManager: PreferenceManager) {
        _darkMode.value = preferenceManager.getDarkMode()
        _keepScreenOn.value = preferenceManager.getKeepScreenOn()
        _streamQuality.value = preferenceManager.getStreamQuality()
        _showStatusOverlay.value = preferenceManager.getShowStatusOverlay()
        _autoReconnect.value = preferenceManager.getAutoReconnect()
        _bluetoothFallback.value = preferenceManager.getBluetoothFallback()
        _connectionCheckInterval.value = preferenceManager.getConnectionCheckInterval()
        _controlSensitivity.value = preferenceManager.getControlSensitivity()
        _wifiTimeout.value = preferenceManager.getWiFiTimeout()
    }
}