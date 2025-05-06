package com.ptzcontroller.data.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.ptzcontroller.data.network.NetworkClient
import com.ptzcontroller.data.network.BluetoothClient
import com.ptzcontroller.data.network.ServerStatus
import com.ptzcontroller.ui.connection.ConnectionStatus
import com.ptzcontroller.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Repository for managing camera connection
 */
class ConnectionRepository(private val context: Context) {
    
    private val preferenceManager = PreferenceManager(context)
    
    // Connection status flow for UI updates
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus
    
    // Server status flow for UI updates
    private val _serverStatus = MutableStateFlow(ServerStatus.OFFLINE)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus
    
    // Network clients
    private var networkClient: NetworkClient? = null
    private var bluetoothClient: BluetoothClient? = null

    /**
     * Connect to the camera server via WiFi
     * @param ipAddress Server IP address
     * @param port Server port
     * @return Connection result
     */
    suspend fun connectWiFi(ipAddress: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        _connectionStatus.value = ConnectionStatus.CONNECTING
        
        // Update preferences
        preferenceManager.setWiFiIpAddress(ipAddress)
        preferenceManager.setWiFiPort(port)
        
        // Create network client
        networkClient = NetworkClient(ipAddress, port, preferenceManager.getWiFiTimeout())
        
        // Test connection
        val result = networkClient?.testConnection() ?: false
        
        if (result) {
            // Send a ping command to verify server supports our API
            val command = JSONObject().apply {
                put("command", "ping")
            }
            
            val response = networkClient?.sendCommand(command)
            
            if (response != null) {
                _connectionStatus.value = ConnectionStatus.CONNECTED
                
                // Check server features
                val features = response.optJSONArray("features")
                _serverStatus.value = if (features != null && features.length() > 0) {
                    ServerStatus.ONLINE
                } else {
                    ServerStatus.LIMITED
                }
            } else {
                _connectionStatus.value = ConnectionStatus.FAILED
                _serverStatus.value = ServerStatus.OFFLINE
            }
        } else {
            _connectionStatus.value = ConnectionStatus.FAILED
            _serverStatus.value = ServerStatus.OFFLINE
            
            // If Bluetooth fallback is enabled, try Bluetooth
            if (preferenceManager.getBluetoothFallback()) {
                val btAddress = preferenceManager.getBluetoothDeviceAddress()
                if (btAddress != null) {
                    return@withContext connectBluetooth(btAddress)
                }
            }
        }
        
        return@withContext result
    }
    
    /**
     * Connect to the camera server via Bluetooth
     * @param deviceAddress Bluetooth device address
     * @return Connection result
     */
    suspend fun connectBluetooth(deviceAddress: String): Boolean = withContext(Dispatchers.IO) {
        _connectionStatus.value = ConnectionStatus.CONNECTING
        
        // Update preferences
        preferenceManager.setBluetoothDeviceAddress(deviceAddress)
        
        // Create Bluetooth client
        if (bluetoothClient == null) {
            bluetoothClient = BluetoothClient(context)
        }
        
        val btClient = bluetoothClient ?: return@withContext false
        
        if (!btClient.isBluetoothEnabled()) {
            _connectionStatus.value = ConnectionStatus.FAILED
            return@withContext false
        }
        
        // Find the device
        val device = btClient.getPairedDevices().find { it.address == deviceAddress }
        
        if (device == null) {
            _connectionStatus.value = ConnectionStatus.FAILED
            return@withContext false
        }
        
        // Connect to the device
        val result = btClient.connect(device)
        
        if (result) {
            // Send a ping command to verify server supports our API
            val command = JSONObject().apply {
                put("command", "ping")
            }
            
            val response = btClient.sendCommand(command)
            
            if (response != null) {
                _connectionStatus.value = ConnectionStatus.CONNECTED
                
                // Check server features
                val features = response.optJSONArray("features")
                _serverStatus.value = if (features != null && features.length() > 0) {
                    ServerStatus.ONLINE
                } else {
                    ServerStatus.LIMITED
                }
            } else {
                _connectionStatus.value = ConnectionStatus.FAILED
                _serverStatus.value = ServerStatus.OFFLINE
                btClient.disconnect()
            }
        } else {
            _connectionStatus.value = ConnectionStatus.FAILED
            _serverStatus.value = ServerStatus.OFFLINE
        }
        
        return@withContext result
    }
    
    /**
     * Scan for Bluetooth devices
     * @return List of Bluetooth devices
     */
    suspend fun scanBluetoothDevices(): List<BluetoothDevice> = withContext(Dispatchers.IO) {
        if (bluetoothClient == null) {
            bluetoothClient = BluetoothClient(context)
        }
        
        val btClient = bluetoothClient ?: return@withContext emptyList()
        
        if (!btClient.isBluetoothEnabled()) {
            return@withContext emptyList()
        }
        
        return@withContext btClient.scanDevices()
    }
    
    /**
     * Get list of paired Bluetooth devices
     * @return List of paired Bluetooth devices
     */
    suspend fun getPairedBluetoothDevices(): List<BluetoothDevice> = withContext(Dispatchers.IO) {
        if (bluetoothClient == null) {
            bluetoothClient = BluetoothClient(context)
        }
        
        val btClient = bluetoothClient ?: return@withContext emptyList()
        
        if (!btClient.isBluetoothEnabled()) {
            return@withContext emptyList()
        }
        
        return@withContext btClient.getPairedDevices()
    }
    
    /**
     * Check if Bluetooth is enabled
     * @return true if Bluetooth is enabled
     */
    suspend fun isBluetoothEnabled(): Boolean = withContext(Dispatchers.IO) {
        if (bluetoothClient == null) {
            bluetoothClient = BluetoothClient(context)
        }
        
        return@withContext bluetoothClient?.isBluetoothEnabled() ?: false
    }
    
    /**
     * Check server status
     * @return Server status
     */
    suspend fun checkServerStatus(): ServerStatus = withContext(Dispatchers.IO) {
        val isConnected = when (_connectionStatus.value) {
            ConnectionStatus.CONNECTED -> true
            else -> false
        }
        
        if (!isConnected) {
            // Try to connect using saved preferences
            val useWifi = preferenceManager.getWiFiIpAddress().isNotEmpty()
            
            if (useWifi) {
                val ipAddress = preferenceManager.getWiFiIpAddress()
                val port = preferenceManager.getWiFiPort()
                connectWiFi(ipAddress, port)
            } else if (preferenceManager.getBluetoothFallback()) {
                val btAddress = preferenceManager.getBluetoothDeviceAddress()
                if (btAddress != null) {
                    connectBluetooth(btAddress)
                }
            }
        }
        
        return@withContext _serverStatus.value
    }
    
    /**
     * Disconnect from the server
     */
    suspend fun disconnect(): Boolean = withContext(Dispatchers.IO) {
        networkClient?.disconnect()
        bluetoothClient?.disconnect()
        
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _serverStatus.value = ServerStatus.OFFLINE
        
        return@withContext true
    }
}