package com.example.ptzcameracontroller.data.repository

import android.bluetooth.BluetoothDevice
import android.util.Log
import com.example.ptzcameracontroller.data.network.BluetoothClient
import com.example.ptzcameracontroller.data.network.NetworkClient
import com.example.ptzcameracontroller.ui.control.CameraMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Repository for camera control operations
 * Handles communication with the server via WiFi and Bluetooth
 */
class CameraControlRepository {
    
    companion object {
        private const val TAG = "CameraControlRepository"
    }
    
    // Network client for WiFi communication
    private var networkClient: NetworkClient? = null
    
    // Bluetooth client for Bluetooth communication
    private var bluetoothClient: BluetoothClient? = null
    
    // Connection status
    private val isWiFiConnected = AtomicBoolean(false)
    private val isBluetoothConnected = AtomicBoolean(false)
    
    // Current connection mode (primary = WiFi, fallback = Bluetooth)
    private var usingBluetoothFallback = false
    
    /**
     * Initialize WiFi connection
     * @param ipAddress IP address of the server
     * @param port Port number of the server
     */
    fun initializeWiFi(ipAddress: String, port: Int) {
        networkClient = NetworkClient(ipAddress, port)
    }
    
    /**
     * Initialize Bluetooth connection
     * @param device Bluetooth device to connect to
     */
    fun initializeBluetooth(device: BluetoothDevice) {
        bluetoothClient = BluetoothClient(device)
    }
    
    /**
     * Connect to the server via WiFi
     * @return true if connected successfully, false otherwise
     */
    suspend fun connectWiFi(): Boolean = withContext(Dispatchers.IO) {
        try {
            val connected = networkClient?.pingServer() ?: false
            isWiFiConnected.set(connected)
            if (connected) {
                usingBluetoothFallback = false
            }
            connected
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to WiFi: ${e.message}")
            isWiFiConnected.set(false)
            false
        }
    }
    
    /**
     * Connect to the server via Bluetooth
     * @return true if connected successfully, false otherwise
     */
    suspend fun connectBluetooth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val connected = bluetoothClient?.connect() ?: false
            isBluetoothConnected.set(connected)
            connected
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to Bluetooth: ${e.message}")
            isBluetoothConnected.set(false)
            false
        }
    }
    
    /**
     * Disconnect from WiFi
     */
    suspend fun disconnectWiFi() = withContext(Dispatchers.IO) {
        isWiFiConnected.set(false)
    }
    
    /**
     * Disconnect from Bluetooth
     */
    suspend fun disconnectBluetooth() = withContext(Dispatchers.IO) {
        bluetoothClient?.close()
        isBluetoothConnected.set(false)
    }
    
    /**
     * Switch to Bluetooth fallback mode
     * @return true if switched successfully, false otherwise
     */
    suspend fun switchToBluetoothFallback(): Boolean = withContext(Dispatchers.IO) {
        if (!isBluetoothConnected.get()) {
            val connected = connectBluetooth()
            if (!connected) {
                return@withContext false
            }
        }
        
        usingBluetoothFallback = true
        true
    }
    
    /**
     * Switch back to WiFi primary mode
     * @return true if switched successfully, false otherwise
     */
    suspend fun switchToWiFiPrimary(): Boolean = withContext(Dispatchers.IO) {
        if (!isWiFiConnected.get()) {
            val connected = connectWiFi()
            if (!connected) {
                return@withContext false
            }
        }
        
        usingBluetoothFallback = false
        true
    }
    
    /**
     * Check if using Bluetooth fallback
     * @return true if using Bluetooth fallback, false if using WiFi
     */
    fun isUsingBluetoothFallback(): Boolean {
        return usingBluetoothFallback
    }
    
    /**
     * Send pan/tilt command
     * @param pan Pan value (-100 to 100)
     * @param tilt Tilt value (-100 to 100)
     * @return true if successful, false otherwise
     */
    suspend fun sendPanTilt(pan: Int, tilt: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            if (usingBluetoothFallback) {
                // Send via Bluetooth
                bluetoothClient?.sendPanTilt(pan, tilt) ?: false
            } else {
                // Send via WiFi
                networkClient?.sendPanTilt(pan, tilt) ?: false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending pan/tilt command: ${e.message}")
            false
        }
    }
    
    /**
     * Send zoom command
     * @param zoomLevel Zoom level (0-100)
     * @return true if successful, false otherwise
     */
    suspend fun sendZoom(zoomLevel: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            if (usingBluetoothFallback) {
                // Send via Bluetooth
                bluetoothClient?.sendZoom(zoomLevel) ?: false
            } else {
                // Send via WiFi
                networkClient?.sendZoom(zoomLevel) ?: false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending zoom command: ${e.message}")
            false
        }
    }
    
    /**
     * Send camera mode command
     * @param mode Camera mode (RGB or IR)
     * @return true if successful, false otherwise
     */
    suspend fun sendCameraMode(mode: CameraMode): Boolean = withContext(Dispatchers.IO) {
        try {
            if (usingBluetoothFallback) {
                // Send via Bluetooth
                bluetoothClient?.sendCameraMode(mode) ?: false
            } else {
                // Send via WiFi
                networkClient?.sendCameraMode(mode) ?: false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending camera mode command: ${e.message}")
            false
        }
    }
    
    /**
     * Send save preset command
     * @param presetNumber Preset number (1-255)
     * @return true if successful, false otherwise
     */
    suspend fun savePreset(presetNumber: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            if (usingBluetoothFallback) {
                // Send via Bluetooth
                bluetoothClient?.sendSavePreset(presetNumber) ?: false
            } else {
                // Send via WiFi
                networkClient?.sendSavePreset(presetNumber) ?: false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending save preset command: ${e.message}")
            false
        }
    }
    
    /**
     * Send go to preset command
     * @param presetNumber Preset number (1-255)
     * @return true if successful, false otherwise
     */
    suspend fun gotoPreset(presetNumber: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            if (usingBluetoothFallback) {
                // Send via Bluetooth
                bluetoothClient?.sendGotoPreset(presetNumber) ?: false
            } else {
                // Send via WiFi
                networkClient?.sendGotoPreset(presetNumber) ?: false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending goto preset command: ${e.message}")
            false
        }
    }
    
    /**
     * Get the stream URL
     * @return RTSP URL as string, or null if error
     */
    suspend fun getStreamUrl(): String? = withContext(Dispatchers.IO) {
        try {
            // Stream URL is only available via WiFi
            networkClient?.getStreamUrl()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting stream URL: ${e.message}")
            null
        }
    }
    
    /**
     * Check connection status
     * @return true if connected (either WiFi or Bluetooth), false otherwise
     */
    fun isConnected(): Boolean {
        return isWiFiConnected.get() || isBluetoothConnected.get()
    }
    
    /**
     * Check WiFi connection status
     * @return true if connected via WiFi, false otherwise
     */
    fun isWiFiConnected(): Boolean {
        return isWiFiConnected.get()
    }
    
    /**
     * Check Bluetooth connection status
     * @return true if connected via Bluetooth, false otherwise
     */
    fun isBluetoothConnected(): Boolean {
        return isBluetoothConnected.get()
    }
    
    /**
     * Ping the server to check connection
     * @return true if connected, false otherwise
     */
    suspend fun ping(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (usingBluetoothFallback) {
                // Ping via Bluetooth
                val connected = bluetoothClient?.ping() ?: false
                isBluetoothConnected.set(connected)
                connected
            } else {
                // Ping via WiFi
                val connected = networkClient?.pingServer() ?: false
                isWiFiConnected.set(connected)
                connected
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pinging server: ${e.message}")
            false
        }
    }
}