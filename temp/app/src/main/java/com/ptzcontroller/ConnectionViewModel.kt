package com.ptzcontroller.ui.connection

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptzcontroller.data.repository.ConnectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

class ConnectionViewModel(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

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

    // Server status
    private val _serverStatus = MutableLiveData<com.example.ptzcameracontroller.data.network.ServerStatus?>()
    val serverStatus: LiveData<com.example.ptzcameracontroller.data.network.ServerStatus?> = _serverStatus

    init {
        // Initialize server status collection
        viewModelScope.launch {
            connectionRepository.serverStatus.collectLatest { status ->
                _serverStatus.postValue(status)
            }
        }
    }

    // Method to connect to WiFi
    suspend fun connectToWiFi(ip: String, port: Int) {
        // Update status to connecting
        _wifiConnectionStatus.postValue(ConnectionStatus.CONNECTING)

        try {
            // Initialize WiFi with IP and port
            connectionRepository.initializeWiFi(ip, port)
            
            // Try to connect
            val success = connectionRepository.connectToWiFi()

            if (success) {
                _wifiConnectionStatus.postValue(ConnectionStatus.CONNECTED)
            } else {
                _wifiConnectionStatus.postValue(ConnectionStatus.ERROR)
            }
        } catch (e: Exception) {
            _wifiConnectionStatus.postValue(ConnectionStatus.ERROR)
        }
    }

    // Method to disconnect from WiFi
    suspend fun disconnectFromWiFi() {
        try {
            connectionRepository.disconnectFromWiFi()
            _wifiConnectionStatus.postValue(ConnectionStatus.DISCONNECTED)
        } catch (e: Exception) {
            _wifiConnectionStatus.postValue(ConnectionStatus.ERROR)
        }
    }

    // Method to get paired Bluetooth devices
    suspend fun getPairedDevices(): List<BluetoothDeviceInfo> {
        return withContext(Dispatchers.IO) {
            val devices = connectionRepository.getPairedBluetoothDevices()
            _bluetoothDevices.postValue(devices)
            devices
        }
    }

    // Method to connect to Bluetooth device
    suspend fun connectToBluetooth(deviceInfo: BluetoothDeviceInfo) {
        // Update status to connecting
        _bluetoothConnectionStatus.postValue(ConnectionStatus.CONNECTING)

        try {
            val success = connectionRepository.connectToBluetooth(deviceInfo)

            if (success) {
                _bluetoothConnectionStatus.postValue(ConnectionStatus.CONNECTED)
            } else {
                _bluetoothConnectionStatus.postValue(ConnectionStatus.ERROR)
            }
        } catch (e: Exception) {
            _bluetoothConnectionStatus.postValue(ConnectionStatus.ERROR)
        }
    }

    // Method to disconnect from Bluetooth
    suspend fun disconnectFromBluetooth() {
        try {
            connectionRepository.disconnectFromBluetooth()
            _bluetoothConnectionStatus.postValue(ConnectionStatus.DISCONNECTED)
        } catch (e: Exception) {
            _bluetoothConnectionStatus.postValue(ConnectionStatus.ERROR)
        }
    }

    // Method to refresh connection status
    suspend fun refreshConnectionStatus() {
        // Check if WiFi is connected
        if (connectionRepository.isWiFiConnected()) {
            _wifiConnectionStatus.postValue(ConnectionStatus.CONNECTED)
        } else {
            _wifiConnectionStatus.postValue(ConnectionStatus.DISCONNECTED)
        }

        // Check if Bluetooth is connected
        if (connectionRepository.isBluetoothConnected()) {
            _bluetoothConnectionStatus.postValue(ConnectionStatus.CONNECTED)
        } else {
            _bluetoothConnectionStatus.postValue(ConnectionStatus.DISCONNECTED)
        }

        // Update server status
        val status = connectionRepository.getServerStatus()
        _serverStatus.postValue(status)
    }

    // Method to get RTSP stream URL
    suspend fun getStreamUrl(): String? {
        return connectionRepository.getStreamUrl()
    }

    // Method to parse QR code content
    fun parseQRCodeContent(qrContent: String): Pair<String, Int>? {
        return connectionRepository.parseQRCodeContent(qrContent)
    }
}