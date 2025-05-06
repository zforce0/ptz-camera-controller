package com.ptzcontroller.data.repository

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ptzcontroller.data.network.BluetoothClient
import com.ptzcontroller.data.network.NetworkClient
import com.ptzcontroller.data.network.ServerStatus
import com.ptzcontroller.ui.connection.BluetoothDeviceInfo
import com.ptzcontroller.ui.connection.ConnectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Repository for managing connections to the onboard server
 */
class ConnectionRepository(private val context: Context) {
    
    companion object {
        private const val TAG = "ConnectionRepository"
    }
    
    // Network client for WiFi communication
    private var networkClient: NetworkClient? = null
    
    // Bluetooth client for Bluetooth communication
    private var bluetoothClient: BluetoothClient? = null
    
    // Bluetooth adapter
    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    // Connection status
    private val _wifiStatus = MutableLiveData<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    val wifiStatus: LiveData<ConnectionStatus> = _wifiStatus
    
    private val _bluetoothStatus = MutableLiveData<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    val bluetoothStatus: LiveData<ConnectionStatus> = _bluetoothStatus
    
    // Bluetooth devices
    private val _bluetoothDevices = MutableLiveData<List<BluetoothDeviceInfo>>(emptyList())
    val bluetoothDevices: LiveData<List<BluetoothDeviceInfo>> = _bluetoothDevices
    
    // Server status
    private val _serverStatus = MutableStateFlow<ServerStatus?>(null)
    val serverStatus: StateFlow<ServerStatus?> = _serverStatus
    
    // Current connected device
    private var currentBluetoothDevice: BluetoothDevice? = null
    
    /**
     * Initialize WiFi connection
     * @param ipAddress IP address of the server
     * @param port Port number of the server
     */
    fun initializeWiFi(ipAddress: String, port: Int) {
        networkClient = NetworkClient(ipAddress, port)
        _wifiStatus.value = ConnectionStatus.DISCONNECTED
    }
    
    /**
     * Connect to the server via WiFi
     * @return true if connected successfully, false otherwise
     */
    suspend fun connectToWiFi(): Boolean = withContext(Dispatchers.IO) {
        try {
            _wifiStatus.postValue(ConnectionStatus.CONNECTING)
            
            val connected = networkClient?.pingServer() ?: false
            
            if (connected) {
                _wifiStatus.postValue(ConnectionStatus.CONNECTED)
                return@withContext true
            } else {
                _wifiStatus.postValue(ConnectionStatus.ERROR)
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to WiFi: ${e.message}")
            _wifiStatus.postValue(ConnectionStatus.ERROR)
            return@withContext false
        }
    }
    
    /**
     * Disconnect from WiFi
     */
    fun disconnectFromWiFi() {
        _wifiStatus.value = ConnectionStatus.DISCONNECTED
    }
    
    /**
     * Get paired Bluetooth devices
     * @return List of BluetoothDeviceInfo for paired devices
     */
    suspend fun getPairedBluetoothDevices(): List<BluetoothDeviceInfo> = withContext(Dispatchers.IO) {
        val pairedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
        
        val deviceInfoList = pairedDevices.map { device ->
            BluetoothDeviceInfo(
                name = device.name ?: "Unknown Device",
                address = device.address,
                device = device
            )
        }
        
        _bluetoothDevices.postValue(deviceInfoList)
        deviceInfoList
    }
    
    /**
     * Connect to a Bluetooth device
     * @param deviceInfo BluetoothDeviceInfo to connect to
     * @return true if connected successfully, false otherwise
     */
    suspend fun connectToBluetooth(deviceInfo: BluetoothDeviceInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            _bluetoothStatus.postValue(ConnectionStatus.CONNECTING)
            
            currentBluetoothDevice = deviceInfo.device
            bluetoothClient = BluetoothClient(deviceInfo.device)
            
            val connected = bluetoothClient?.connect() ?: false
            
            if (connected) {
                _bluetoothStatus.postValue(ConnectionStatus.CONNECTED)
                return@withContext true
            } else {
                _bluetoothStatus.postValue(ConnectionStatus.ERROR)
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to Bluetooth: ${e.message}")
            _bluetoothStatus.postValue(ConnectionStatus.ERROR)
            return@withContext false
        }
    }
    
    /**
     * Disconnect from Bluetooth
     */
    suspend fun disconnectFromBluetooth() = withContext(Dispatchers.IO) {
        bluetoothClient?.close()
        _bluetoothStatus.postValue(ConnectionStatus.DISCONNECTED)
    }
    
    /**
     * Get server status
     * Updates the _serverStatus StateFlow
     * @return ServerStatus or null if error
     */
    suspend fun getServerStatus(): ServerStatus? = withContext(Dispatchers.IO) {
        try {
            val status = networkClient?.getServerStatus()
            _serverStatus.value = status
            status
        } catch (e: Exception) {
            Log.e(TAG, "Error getting server status: ${e.message}")
            null
        }
    }
    
    /**
     * Check if Bluetooth is enabled
     * @return true if enabled, false otherwise
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Check if WiFi is connected
     * @return true if connected, false otherwise
     */
    fun isWiFiConnected(): Boolean {
        return _wifiStatus.value == ConnectionStatus.CONNECTED
    }
    
    /**
     * Check if Bluetooth is connected
     * @return true if connected, false otherwise
     */
    fun isBluetoothConnected(): Boolean {
        return _bluetoothStatus.value == ConnectionStatus.CONNECTED
    }
    
    /**
     * Get RTSP stream URL from the server
     * @return URL string or null if error
     */
    suspend fun getStreamUrl(): String? = withContext(Dispatchers.IO) {
        try {
            networkClient?.getStreamUrl()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting stream URL: ${e.message}")
            null
        }
    }
    
    /**
     * Parse a QR code content to extract connection info
     * @param qrContent Content of the scanned QR code
     * @return Pair<String, Int> with IP address and port, or null if invalid
     */
    fun parseQRCodeContent(qrContent: String): Pair<String, Int>? {
        try {
            // Expected format: {"wifi":"192.168.1.100:8000"} or similar
            if (qrContent.contains("wifi")) {
                val jsonObject = org.json.JSONObject(qrContent)
                val wifiString = jsonObject.optString("wifi")
                
                if (wifiString.isNotEmpty() && wifiString.contains(":")) {
                    val parts = wifiString.split(":")
                    if (parts.size == 2) {
                        val ip = parts[0]
                        val port = parts[1].toIntOrNull() ?: 8000
                        return Pair(ip, port)
                    }
                }
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing QR code: ${e.message}")
            return null
        }
    }
}