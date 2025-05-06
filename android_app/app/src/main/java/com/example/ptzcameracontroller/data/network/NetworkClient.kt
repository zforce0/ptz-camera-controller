package com.example.ptzcameracontroller.data.network

import android.util.Log
import com.example.ptzcameracontroller.ui.control.CameraMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Handles network communication with the onboard server via WiFi
 */
class NetworkClient(private val baseUrl: String, private val port: Int) {
    
    companion object {
        private const val TAG = "NetworkClient"
        private const val CONNECT_TIMEOUT = 5L // seconds
        private const val READ_TIMEOUT = 10L // seconds
        private const val WRITE_TIMEOUT = 10L // seconds
    }
    
    // OkHttpClient for making network requests
    private val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()
    
    // Base URL for API endpoints
    private val serverUrl = "http://$baseUrl:$port"
    
    /**
     * Send a ping to check server connectivity
     * @return true if connected, false otherwise
     */
    suspend fun pingServer(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$serverUrl/ping")
                .build()
            
            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful
            response.close()
            
            isSuccess
        } catch (e: IOException) {
            Log.e(TAG, "Error pinging server: ${e.message}")
            false
        }
    }
    
    /**
     * Send pan/tilt command to the server
     * @param pan Pan value (-100 to 100)
     * @param tilt Tilt value (-100 to 100)
     * @return true if successful, false otherwise
     */
    suspend fun sendPanTilt(pan: Int, tilt: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("pan", pan)
                put("tilt", tilt)
            }
            
            val requestBody = json.toString().toRequestBody()
            
            val request = Request.Builder()
                .url("$serverUrl/control/move")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful
            response.close()
            
            isSuccess
        } catch (e: IOException) {
            Log.e(TAG, "Error sending pan/tilt command: ${e.message}")
            false
        }
    }
    
    /**
     * Send zoom command to the server
     * @param zoomLevel Zoom level (0-100)
     * @return true if successful, false otherwise
     */
    suspend fun sendZoom(zoomLevel: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("zoom", zoomLevel)
            }
            
            val requestBody = json.toString().toRequestBody()
            
            val request = Request.Builder()
                .url("$serverUrl/control/zoom")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful
            response.close()
            
            isSuccess
        } catch (e: IOException) {
            Log.e(TAG, "Error sending zoom command: ${e.message}")
            false
        }
    }
    
    /**
     * Send camera mode command to the server
     * @param mode Camera mode (RGB or IR)
     * @return true if successful, false otherwise
     */
    suspend fun sendCameraMode(mode: CameraMode): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("mode", mode.name)
            }
            
            val requestBody = json.toString().toRequestBody()
            
            val request = Request.Builder()
                .url("$serverUrl/control/mode")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful
            response.close()
            
            isSuccess
        } catch (e: IOException) {
            Log.e(TAG, "Error sending camera mode command: ${e.message}")
            false
        }
    }
    
    /**
     * Send save preset command to the server
     * @param presetNumber Preset number (1-255)
     * @return true if successful, false otherwise
     */
    suspend fun sendSavePreset(presetNumber: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("preset", presetNumber)
            }
            
            val requestBody = json.toString().toRequestBody()
            
            val request = Request.Builder()
                .url("$serverUrl/control/preset/save")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful
            response.close()
            
            isSuccess
        } catch (e: IOException) {
            Log.e(TAG, "Error sending save preset command: ${e.message}")
            false
        }
    }
    
    /**
     * Send go to preset command to the server
     * @param presetNumber Preset number (1-255)
     * @return true if successful, false otherwise
     */
    suspend fun sendGotoPreset(presetNumber: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("preset", presetNumber)
            }
            
            val requestBody = json.toString().toRequestBody()
            
            val request = Request.Builder()
                .url("$serverUrl/control/preset/goto")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful
            response.close()
            
            isSuccess
        } catch (e: IOException) {
            Log.e(TAG, "Error sending goto preset command: ${e.message}")
            false
        }
    }
    
    /**
     * Get the RTSP stream URL from the server
     * @return RTSP URL as string, or null if error
     */
    suspend fun getStreamUrl(): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$serverUrl/stream/url")
                .build()
            
            val response = client.newCall(request).execute()
            val result = if (response.isSuccessful) {
                response.body?.string()?.let { body ->
                    val json = JSONObject(body)
                    json.optString("url")
                }
            } else {
                null
            }
            response.close()
            
            result
        } catch (e: IOException) {
            Log.e(TAG, "Error getting stream URL: ${e.message}")
            null
        }
    }
    
    /**
     * Get server status
     * @return ServerStatus object, or null if error
     */
    suspend fun getServerStatus(): ServerStatus? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$serverUrl/status")
                .build()
            
            val response = client.newCall(request).execute()
            val result = if (response.isSuccessful) {
                response.body?.string()?.let { body ->
                    val json = JSONObject(body)
                    ServerStatus(
                        temperature = json.optDouble("temperature", 0.0),
                        cpuUsage = json.optDouble("cpu_usage", 0.0),
                        memoryUsage = json.optDouble("memory_usage", 0.0),
                        uptime = json.optLong("uptime", 0)
                    )
                }
            } else {
                null
            }
            response.close()
            
            result
        } catch (e: IOException) {
            Log.e(TAG, "Error getting server status: ${e.message}")
            null
        }
    }
}

/**
 * Data class for server status
 */
data class ServerStatus(
    val temperature: Double, // CPU temperature in Celsius
    val cpuUsage: Double,    // CPU usage percentage (0-100)
    val memoryUsage: Double, // Memory usage percentage (0-100)
    val uptime: Long         // Server uptime in seconds
)