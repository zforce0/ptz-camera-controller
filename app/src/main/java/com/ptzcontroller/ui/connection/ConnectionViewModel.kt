package com.ptzcontroller.ui.connection

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ptzcontroller.data.model.BluetoothDeviceInfo
import com.ptzcontroller.data.network.ServerStatus
import com.ptzcontroller.data.repository.ConnectionRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel for connection screen
 */
class ConnectionViewModel(private val repository: ConnectionRepository) : ViewModel() {

    // Connection status
    private val _connectionStatus = MutableLiveData<ConnectionStatus>()
    val connectionStatus: LiveData<ConnectionStatus> = _connectionStatus

    // Server status
    private val _serverStatus = MutableLiveData<ServerStatus>()
    val serverStatus: LiveData<ServerStatus> = _serverStatus

    // WiFi connection
    private val _wifiConnectingState = MutableLiveData<WifiConnectingState>()
    val wifiConnectingState: LiveData<WifiConnectingState> = _wifiConnectingState

    // Bluetooth devices
    private val _pairedDevices = MutableLiveData<List<BluetoothDeviceInfo>>()
    val pairedDevices: LiveData<List<BluetoothDeviceInfo>> = _pairedDevices

    private val _discoveredDevices = MutableLiveData<List<BluetoothDeviceInfo>>()
    val discoveredDevices: LiveData<List<BluetoothDeviceInfo>> = _discoveredDevices

    // Scanning state
    private val _isScanning = MutableLiveData<Boolean>()
    val isScanning: LiveData<Boolean> = _isScanning

    // Bluetooth enabled state
    private val _isBluetoothEnabled = MutableLiveData<Boolean>()
    val isBluetoothEnabled: LiveData<Boolean> = _isBluetoothEnabled

    // Bluetooth connecting state
    private val _bluetoothConnectingState = MutableLiveData<BluetoothConnectingState>()
    val bluetoothConnectingState: LiveData<BluetoothConnectingState> = _bluetoothConnectingState

    init {
        // Start observing connection status
        viewModelScope.launch {
            repository.connectionStatus.collect { status ->
                _connectionStatus.postValue(status)
            }
        }

        // Start observing server status
        viewModelScope.launch {
            repository.serverStatus.collect { status ->
                _serverStatus.postValue(status)
            }
        }
    }

    /**
     * Check if Bluetooth is enabled
     */
    fun checkBluetoothEnabled() {
        viewModelScope.launch {
            val enabled = repository.isBluetoothEnabled()
            _isBluetoothEnabled.postValue(enabled)
        }
    }

    /**
     * Load paired Bluetooth devices
     */
    fun loadPairedDevices() {
        viewModelScope.launch {
            val devices = repository.getPairedBluetoothDevices()
            val deviceInfos = devices.map { BluetoothDeviceInfo.fromBluetoothDevice(it) }
            _pairedDevices.postValue(deviceInfos)
        }
    }

    /**
     * Scan for Bluetooth devices
     */
    fun scanForDevices() {
        viewModelScope.launch {
            _isScanning.postValue(true)
            val devices = repository.scanBluetoothDevices()
            val deviceInfos = devices.map { BluetoothDeviceInfo.fromBluetoothDevice(it) }
            _discoveredDevices.postValue(deviceInfos)
            _isScanning.postValue(false)
        }
    }

    /**
     * Connect to WiFi camera server
     */
    fun connectWifi(ipAddress: String, port: Int) {
        viewModelScope.launch {
            _wifiConnectingState.postValue(WifiConnectingState.Connecting(ipAddress, port))
            val result = repository.connectWiFi(ipAddress, port)
            _wifiConnectingState.postValue(
                if (result) {
                    WifiConnectingState.Success(ipAddress, port)
                } else {
                    WifiConnectingState.Failed(ipAddress, port)
                }
            )
        }
    }

    /**
     * Connect to Bluetooth device
     */
    fun connectBluetooth(deviceInfo: BluetoothDeviceInfo) {
        viewModelScope.launch {
            _bluetoothConnectingState.postValue(BluetoothConnectingState.Connecting(deviceInfo))
            val result = repository.connectBluetooth(deviceInfo.address)
            _bluetoothConnectingState.postValue(
                if (result) {
                    BluetoothConnectingState.Success(deviceInfo)
                } else {
                    BluetoothConnectingState.Failed(deviceInfo)
                }
            )
        }
    }

    /**
     * Disconnect from current connection
     */
    fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
        }
    }
}

/**
 * Factory for creating ConnectionViewModel
 */
class ConnectionViewModelFactory(private val repository: ConnectionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConnectionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Sealed class representing WiFi connecting state
 */
sealed class WifiConnectingState {
    data class Connecting(val ipAddress: String, val port: Int) : WifiConnectingState()
    data class Success(val ipAddress: String, val port: Int) : WifiConnectingState()
    data class Failed(val ipAddress: String, val port: Int) : WifiConnectingState()
}

/**
 * Sealed class representing Bluetooth connecting state
 */
sealed class BluetoothConnectingState {
    data class Connecting(val deviceInfo: BluetoothDeviceInfo) : BluetoothConnectingState()
    data class Success(val deviceInfo: BluetoothDeviceInfo) : BluetoothConnectingState()
    data class Failed(val deviceInfo: BluetoothDeviceInfo) : BluetoothConnectingState()
}