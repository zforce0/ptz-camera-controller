package com.ptzcontroller.data.network

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.ptzcontroller.ui.control.CameraMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Handles Bluetooth communication with the onboard server
 */
class BluetoothClient(private val device: BluetoothDevice) {
    
    companion object {
        private const val TAG = "BluetoothClient"
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID
    }
    
    // Bluetooth socket for communication
    private var socket: BluetoothSocket? = null
    
    // Input and output streams
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    // Connection status
    private var isConnected = false
    
    /**
     * Connect to the Bluetooth device
     * @return true if connected successfully, false otherwise
     */
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Create RFCOMM socket
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            
            // Connect to the device
            socket?.connect()
            
            // Get streams
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream
            
            isConnected = true
            Log.d(TAG, "Connected to ${device.name}")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to connect: ${e.message}")
            close()
            false
        }
    }
    
    /**
     * Close the connection
     */
    fun close() {
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing connection: ${e.message}")
        } finally {
            inputStream = null
            outputStream = null
            socket = null
            isConnected = false
        }
    }
    
    /**
     * Send a command to the server
     * @param command The command to send
     * @return true if successful, false otherwise
     */
    private suspend fun sendCommand(command: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected) {
            Log.e(TAG, "Not connected")
            return@withContext false
        }
        
        try {
            // Add newline as command terminator
            val bytes = "$command\n".toByteArray()
            outputStream?.write(bytes)
            outputStream?.flush()
            
            Log.d(TAG, "Sent command: $command")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to send command: ${e.message}")
            false
        }
    }
    
    /**
     * Receive a response from the server
     * @return Response string, or null if error
     */
    private suspend fun receiveResponse(): String? = withContext(Dispatchers.IO) {
        if (!isConnected) {
            Log.e(TAG, "Not connected")
            return@withContext null
        }
        
        try {
            val buffer = ByteArray(1024)
            val bytes = inputStream?.read(buffer) ?: -1
            
            if (bytes > 0) {
                val response = String(buffer, 0, bytes)
                Log.d(TAG, "Received response: $response")
                return@withContext response
            }
            
            null
        } catch (e: IOException) {
            Log.e(TAG, "Failed to receive response: ${e.message}")
            null
        }
    }
    
    /**
     * Send pan/tilt command
     * @param pan Pan value (-100 to 100)
     * @param tilt Tilt value (-100 to 100)
     * @return true if successful, false otherwise
     */
    suspend fun sendPanTilt(pan: Int, tilt: Int): Boolean {
        val command = JSONObject().apply {
            put("command", "move")
            put("pan", pan)
            put("tilt", tilt)
        }.toString()
        
        return sendCommand(command)
    }
    
    /**
     * Send zoom command
     * @param zoomLevel Zoom level (0-100)
     * @return true if successful, false otherwise
     */
    suspend fun sendZoom(zoomLevel: Int): Boolean {
        val command = JSONObject().apply {
            put("command", "zoom")
            put("level", zoomLevel)
        }.toString()
        
        return sendCommand(command)
    }
    
    /**
     * Send camera mode command
     * @param mode Camera mode (RGB or IR)
     * @return true if successful, false otherwise
     */
    suspend fun sendCameraMode(mode: CameraMode): Boolean {
        val command = JSONObject().apply {
            put("command", "mode")
            put("mode", mode.name)
        }.toString()
        
        return sendCommand(command)
    }
    
    /**
     * Send save preset command
     * @param presetNumber Preset number (1-255)
     * @return true if successful, false otherwise
     */
    suspend fun sendSavePreset(presetNumber: Int): Boolean {
        val command = JSONObject().apply {
            put("command", "preset")
            put("action", "save")
            put("number", presetNumber)
        }.toString()
        
        return sendCommand(command)
    }
    
    /**
     * Send go to preset command
     * @param presetNumber Preset number (1-255)
     * @return true if successful, false otherwise
     */
    suspend fun sendGotoPreset(presetNumber: Int): Boolean {
        val command = JSONObject().apply {
            put("command", "preset")
            put("action", "goto")
            put("number", presetNumber)
        }.toString()
        
        return sendCommand(command)
    }
    
    /**
     * Send a ping to check connection
     * @return true if connected, false otherwise
     */
    suspend fun ping(): Boolean {
        val command = JSONObject().apply {
            put("command", "ping")
        }.toString()
        
        return sendCommand(command) && receiveResponse() != null
    }
}