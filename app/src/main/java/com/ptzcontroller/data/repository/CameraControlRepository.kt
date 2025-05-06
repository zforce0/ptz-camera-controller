package com.ptzcontroller.data.repository

import android.content.Context
import com.ptzcontroller.data.network.NetworkClient
import com.ptzcontroller.data.network.BluetoothClient
import com.ptzcontroller.CameraMode
import com.ptzcontroller.utils.PreferenceManager
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for camera control operations
 */
class CameraControlRepository(private val context: Context) {
    
    private val preferenceManager = PreferenceManager(context)
    private var networkClient: NetworkClient? = null
    private var bluetoothClient: BluetoothClient? = null
    
    /**
     * Initialize clients with current settings
     */
    private fun initClients() {
        // Create WiFi client
        if (networkClient == null) {
            val ipAddress = preferenceManager.getWiFiIpAddress()
            val port = preferenceManager.getWiFiPort()
            val timeout = preferenceManager.getWiFiTimeout()
            networkClient = NetworkClient(ipAddress, port, timeout)
        }
        
        // Create Bluetooth client if fallback enabled
        if (bluetoothClient == null && preferenceManager.getBluetoothFallback()) {
            bluetoothClient = BluetoothClient(context)
        }
    }
    
    /**
     * Check connection to the camera server
     * @return true if connection successful
     */
    suspend fun checkConnection(): Boolean = withContext(Dispatchers.IO) {
        initClients()
        
        // Try WiFi connection first
        val wifiSuccess = networkClient?.testConnection() ?: false
        
        // If WiFi fails and Bluetooth fallback is enabled, try Bluetooth
        if (!wifiSuccess && preferenceManager.getBluetoothFallback()) {
            val bluetoothAddress = preferenceManager.getBluetoothDeviceAddress()
            if (bluetoothAddress != null) {
                val btClient = bluetoothClient ?: return@withContext false
                if (!btClient.isConnected()) {
                    // Try to find device and connect
                    val device = btClient.getPairedDevices().find { it.address == bluetoothAddress }
                    if (device != null) {
                        return@withContext btClient.connect(device)
                    }
                } else {
                    return@withContext true
                }
            }
        }
        
        return@withContext wifiSuccess
    }
    
    /**
     * Control pan and tilt movement
     * @param panSpeed Pan speed (-63 to 63, negative is left)
     * @param tiltSpeed Tilt speed (-63 to 63, negative is up)
     */
    suspend fun controlPanTilt(panSpeed: Int, tiltSpeed: Int): Boolean = withContext(Dispatchers.IO) {
        val invertPan = preferenceManager.getInvertPan()
        val invertTilt = preferenceManager.getInvertTilt()
        
        // Apply inversion if needed
        val actualPanSpeed = if (invertPan) -panSpeed else panSpeed
        val actualTiltSpeed = if (invertTilt) -tiltSpeed else tiltSpeed
        
        // Construct command
        val command = JSONObject().apply {
            put("command", "move")
            put("pan_speed", actualPanSpeed)
            put("tilt_speed", actualTiltSpeed)
        }
        
        return@withContext sendCommand(command) != null
    }
    
    /**
     * Stop all movement
     */
    suspend fun stopMovement(): Boolean = withContext(Dispatchers.IO) {
        val command = JSONObject().apply {
            put("command", "stop")
        }
        
        return@withContext sendCommand(command) != null
    }
    
    /**
     * Zoom in
     */
    suspend fun zoomIn(): Boolean = withContext(Dispatchers.IO) {
        val command = JSONObject().apply {
            put("command", "zoom")
            put("direction", "in")
        }
        
        return@withContext sendCommand(command) != null
    }
    
    /**
     * Zoom out
     */
    suspend fun zoomOut(): Boolean = withContext(Dispatchers.IO) {
        val command = JSONObject().apply {
            put("command", "zoom")
            put("direction", "out")
        }
        
        return@withContext sendCommand(command) != null
    }
    
    /**
     * Set zoom level
     * @param level Zoom level (0-100)
     */
    suspend fun setZoom(level: Int): Boolean = withContext(Dispatchers.IO) {
        val normalizedLevel = level.coerceIn(0, 100)
        
        val command = JSONObject().apply {
            put("command", "absolute_zoom")
            put("level", normalizedLevel)
        }
        
        return@withContext sendCommand(command) != null
    }
    
    /**
     * Set zoom level (alias for setZoom)
     * @param level Zoom level (0-100)
     */
    suspend fun setZoomLevel(level: Int): Boolean = setZoom(level)
    
    /**
     * Set pan speed
     * @param speed Pan speed (-100 to 100)
     */
    suspend fun setPanSpeed(speed: Int): Boolean = withContext(Dispatchers.IO) {
        // Scale from -100..100 to -63..63 for Pelco-D protocol
        val scaledSpeed = (speed * 0.63).toInt().coerceIn(-63, 63)
        
        // Only set pan speed, maintain current tilt speed
        return@withContext controlPanTilt(scaledSpeed, 0)
    }
    
    /**
     * Set tilt speed
     * @param speed Tilt speed (-100 to 100)
     */
    suspend fun setTiltSpeed(speed: Int): Boolean = withContext(Dispatchers.IO) {
        // Scale from -100..100 to -63..63 for Pelco-D protocol
        val scaledSpeed = (speed * 0.63).toInt().coerceIn(-63, 63)
        
        // Only set tilt speed, maintain current pan speed
        return@withContext controlPanTilt(0, scaledSpeed)
    }
    
    /**
     * Set camera mode (RGB or IR)
     * @param mode Camera mode
     */
    suspend fun setCameraMode(mode: CameraMode): Boolean = withContext(Dispatchers.IO) {
        val modeString = when (mode) {
            CameraMode.RGB -> "rgb"
            CameraMode.IR -> "ir"
        }
        
        val command = JSONObject().apply {
            put("command", "set_mode")
            put("mode", modeString)
        }
        
        return@withContext sendCommand(command) != null
    }
    
    /**
     * Save preset position
     * @param presetNumber Preset number (1-255)
     */
    suspend fun savePreset(presetNumber: Int): Boolean = withContext(Dispatchers.IO) {
        val command = JSONObject().apply {
            put("command", "set_preset")
            put("preset", presetNumber)
        }
        
        return@withContext sendCommand(command) != null
    }
    
    /**
     * Go to preset position
     * @param presetNumber Preset number (1-255)
     */
    suspend fun gotoPreset(presetNumber: Int): Boolean = withContext(Dispatchers.IO) {
        val command = JSONObject().apply {
            put("command", "goto_preset")
            put("preset", presetNumber)
        }
        
        return@withContext sendCommand(command) != null
    }
    
    /**
     * Send command to the camera server
     * @param command Command to send
     * @return Response or null if failed
     */
    private suspend fun sendCommand(command: JSONObject): JSONObject? = withContext(Dispatchers.IO) {
        initClients()
        
        // Try WiFi first
        val wifiResponse = networkClient?.sendCommand(command)
        
        // If WiFi fails and Bluetooth fallback is enabled, try Bluetooth
        if (wifiResponse == null && preferenceManager.getBluetoothFallback()) {
            val btClient = bluetoothClient
            if (btClient != null && btClient.isConnected()) {
                return@withContext btClient.sendCommand(command)
            }
        }
        
        return@withContext wifiResponse
    }
}