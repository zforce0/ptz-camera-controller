package com.ptzcontroller.data.repository

import android.content.Context
import android.util.Log
import com.ptzcontroller.CameraMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Repository for camera control operations
 * Handles communication with the server via WiFi and Bluetooth
 */
class CameraControlRepository(private val context: Context) {

    private val connectionRepository = com.ptzcontroller.data.repository.ConnectionRepository(context)
    
    /**
     * Set pan speed
     * @param speed Speed value from -100 to 100, 0 is stopped
     * @return true if command sent successfully
     */
    suspend fun setPanSpeed(speed: Int): Boolean {
        return sendControlCommand("pan", speed)
    }
    
    /**
     * Set tilt speed
     * @param speed Speed value from -100 to 100, 0 is stopped
     * @return true if command sent successfully
     */
    suspend fun setTiltSpeed(speed: Int): Boolean {
        return sendControlCommand("tilt", speed)
    }
    
    /**
     * Set zoom level
     * @param level Zoom level from 0 to 100
     * @return true if command sent successfully
     */
    suspend fun setZoomLevel(level: Int): Boolean {
        return sendControlCommand("zoom", level)
    }
    
    /**
     * Set camera mode
     * @param mode Camera mode (0 = RGB, 1 = IR)
     * @return true if command sent successfully
     */
    suspend fun setCameraMode(mode: CameraMode): Boolean {
        return sendControlCommand("mode", mode.ordinal)
    }
    
    /**
     * Save preset position
     * @param presetNumber Preset number (1-255)
     * @return true if command sent successfully
     */
    suspend fun savePreset(presetNumber: Int): Boolean {
        return sendPresetCommand("save", presetNumber)
    }
    
    /**
     * Go to preset position
     * @param presetNumber Preset number (1-255)
     * @return true if command sent successfully
     */
    suspend fun gotoPreset(presetNumber: Int): Boolean {
        return sendPresetCommand("goto", presetNumber)
    }
    
    /**
     * Check connection status
     * @return true if connected to the camera
     */
    suspend fun isConnected(): Boolean {
        return connectionRepository.isConnected()
    }
    
    /**
     * Check if using Bluetooth fallback
     * @return true if using Bluetooth instead of WiFi
     */
    suspend fun isUsingBluetoothFallback(): Boolean {
        return connectionRepository.isUsingBluetoothFallback()
    }
    
    /**
     * Ping the server
     * @return true if ping successful
     */
    suspend fun ping(): Boolean {
        return connectionRepository.ping()
    }
    
    /**
     * Send pan and tilt commands simultaneously
     * @param pan Pan value (-100 to 100)
     * @param tilt Tilt value (-100 to 100) 
     * @return true if command sent successfully
     */
    suspend fun sendPanTilt(pan: Int, tilt: Int): Boolean {
        val panSuccess = sendControlCommand("pan", pan)
        val tiltSuccess = sendControlCommand("tilt", tilt)
        return panSuccess && tiltSuccess
    }
    
    /**
     * Set zoom level (alias for setZoomLevel)
     * @param zoom Zoom level from 0 to 100
     * @return true if command sent successfully
     */
    suspend fun sendZoom(zoom: Int): Boolean {
        return setZoomLevel(zoom)
    }
    
    /**
     * Send a control command to the camera
     * @param controlType Type of control (pan, tilt, zoom, mode)
     * @param value Control value
     * @return true if command sent successfully
     */
    private suspend fun sendControlCommand(controlType: String, value: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val command = JSONObject().apply {
                    put("type", "control")
                    put("control_type", controlType)
                    put("value", value)
                }
                
                val response = connectionRepository.sendCommand(command)
                response?.optBoolean("success", false) ?: false
            } catch (e: Exception) {
                Log.e("CameraControlRepository", "Error sending control command", e)
                false
            }
        }
    }
    
    /**
     * Send a preset command to the camera
     * @param action Preset action (save, goto)
     * @param presetNumber Preset number (1-255)
     * @return true if command sent successfully
     */
    private suspend fun sendPresetCommand(action: String, presetNumber: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val command = JSONObject().apply {
                    put("type", "preset")
                    put("action", action)
                    put("preset", presetNumber)
                }
                
                val response = connectionRepository.sendCommand(command)
                response?.optBoolean("success", false) ?: false
            } catch (e: Exception) {
                Log.e("CameraControlRepository", "Error sending preset command", e)
                false
            }
        }
    }
}