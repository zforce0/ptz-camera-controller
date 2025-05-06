package com.example.ptzcameracontroller.ui.connection

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Connection status enum
enum class ConnectionStatus(val description: String) {
    CONNECTED("Connected"),
    CONNECTING("Connecting..."),
    DISCONNECTED("Disconnected"),
    ERROR("Error")
}

// Bluetooth device info class for the RecyclerView
data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val device: BluetoothDevice
)

class ConnectionViewModel : ViewModel() {

    // Connection repository - will be injected later
    private lateinit var connectionRepository: ConnectionRepository

    // WiFi connection status
    private val _wifiConnectionStatus = MutableLiveData<ConnectionStatus>().apply {
        value = ConnectionStatus.DISCONNECTED
    }
    val wifiConnectionStatus: LiveData<ConnectionStatus> = _wifiConnectionStatus

    // Bluetooth connection status
    private val _bluetoothConnectionStatus = MutableLiveData<ConnectionStatus>().apply {
        value = ConnectionStatus.DISCONNECTED
    }
    val bluetoothConnectionStatus: LiveData<ConnectionStatus> = _bluetoothConnectionStatus

    // Bluetooth devices list
    private val _bluetoothDevices = MutableLiveData<List<BluetoothDeviceInfo>>().apply {
        value = emptyList()
    }
    val bluetoothDevices: LiveData<List<BluetoothDeviceInfo>> = _bluetoothDevices

    // Method to connect to WiFi
    fun connectToWiFi(ip: String, port: Int) {
        // Update status to connecting
        _wifiConnectionStatus.value = ConnectionStatus.CONNECTING

        // Simulate connection attempt
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Will be implemented when the repository is available
                // val success = connectionRepository.connectToWiFi(ip, port)
                val success = true  // Simulate success for now

                if (success) {
                    _wifiConnectionStatus.postValue(ConnectionStatus.CONNECTED)
                } else {
                    _wifiConnectionStatus.postValue(ConnectionStatus.ERROR)
                }
            } catch (e: Exception) {
                _wifiConnectionStatus.postValue(ConnectionStatus.ERROR)
            }
        }
    }

    // Method to disconnect from WiFi
    fun disconnectFromWiFi() {
        viewModelScope.launch(Dispatchers.IO) {
            // Will be implemented when the repository is available
            // connectionRepository.disconnectFromWiFi()
            _wifiConnectionStatus.postValue(ConnectionStatus.DISCONNECTED)
        }
    }

    // Method to connect to Bluetooth device
    fun connectToBluetooth(deviceInfo: BluetoothDeviceInfo) {
        // Update status to connecting
        _bluetoothConnectionStatus.value = ConnectionStatus.CONNECTING

        // Simulate connection attempt
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Will be implemented when the repository is available
                // val success = connectionRepository.connectToBluetooth(deviceInfo.device)
                val success = true  // Simulate success for now

                if (success) {
                    _bluetoothConnectionStatus.postValue(ConnectionStatus.CONNECTED)
                } else {
                    _bluetoothConnectionStatus.postValue(ConnectionStatus.ERROR)
                }
            } catch (e: Exception) {
                _bluetoothConnectionStatus.postValue(ConnectionStatus.ERROR)
            }
        }
    }

    // Method to disconnect from Bluetooth
    fun disconnectFromBluetooth() {
        viewModelScope.launch(Dispatchers.IO) {
            // Will be implemented when the repository is available
            // connectionRepository.disconnectFromBluetooth()
            _bluetoothConnectionStatus.postValue(ConnectionStatus.DISCONNECTED)
        }
    }

    // Method to add a Bluetooth device to the list
    fun addBluetoothDevice(deviceInfo: BluetoothDeviceInfo) {
        val currentDevices = _bluetoothDevices.value ?: emptyList()
        
        // Check if device already exists in the list
        if (currentDevices.none { it.address == deviceInfo.address }) {
            _bluetoothDevices.postValue(currentDevices + deviceInfo)
        }
    }

    // Method to clear the Bluetooth devices list
    fun clearBluetoothDevices() {
        _bluetoothDevices.postValue(emptyList())
    }
}