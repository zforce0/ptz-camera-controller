package com.ptzcontroller

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.ptzcontroller.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeoutException

/**
 * Repository for managing connections to the PTZ camera server
 */
class ConnectionRepository(private val context: Context) {

    private val preferenceManager = PreferenceManager(context)
    private var networkClient: NetworkClient? = null
    private var bluetoothClient: BluetoothClient? = null
    
    /**
     * Check if device is connected to a WiFi network
     * @return true if connected to WiFi
     */
    fun isWiFiConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    /**
     * Connect to the camera server via WiFi
     * @param ipAddress IP address of the server
     * @param port Port number of the server
     * @return true if connection successful
     */
    suspend fun connectWiFi(ipAddress: String, port: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Create new network client
                networkClient = NetworkClient(ipAddress, port, preferenceManager.getWiFiTimeout())
                
                // Test connection
                val connected = networkClient?.testConnection() ?: false
                
                // Save connection info if successful
                if (connected) {
                    preferenceManager.setWiFiIpAddress(ipAddress)
                    preferenceManager.setWiFiPort(port)
                }
                
                connected
            } catch (e: Exception) {
                Log.e("ConnectionRepository", "Error connecting to WiFi server", e)
                false
            }
        }
    }
    
    /**
     * Disconnect from the WiFi server
     */
    fun disconnectWiFi() {
        networkClient?.disconnect()
        networkClient = null
    }
    
    /**
     * Connect to the camera server via Bluetooth
     * @param device BluetoothDevice to connect to
     * @return true if connection successful
     */
    suspend fun connectBluetooth(device: BluetoothDevice): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Create new bluetooth client
                bluetoothClient = BluetoothClient(context)
                
                // Connect to device
                val connected = bluetoothClient?.connect(device) ?: false
                
                // Save device address if successful
                if (connected) {
                    preferenceManager.setBluetoothDeviceAddress(device.address)
                }
                
                connected
            } catch (e: Exception) {
                Log.e("ConnectionRepository", "Error connecting to Bluetooth device", e)
                false
            }
        }
    }
    
    /**
     * Disconnect from the Bluetooth device
     */
    fun disconnectBluetooth() {
        bluetoothClient?.disconnect()
        bluetoothClient = null
    }
    
    /**
     * Get a list of paired Bluetooth devices
     * @return List of paired devices
     */
    fun getPairedBluetoothDevices(): List<BluetoothDevice> {
        return bluetoothClient?.getPairedDevices() ?: emptyList()
    }
    
    /**
     * Scan for Bluetooth devices
     * @return List of discovered devices
     */
    suspend fun scanBluetoothDevices(): List<BluetoothDevice> {
        return withContext(Dispatchers.IO) {
            try {
                if (bluetoothClient == null) {
                    bluetoothClient = BluetoothClient(context)
                }
                bluetoothClient?.scanDevices() ?: emptyList()
            } catch (e: Exception) {
                Log.e("ConnectionRepository", "Error scanning for Bluetooth devices", e)
                emptyList()
            }
        }
    }
    
    /**
     * Send a control command to the server
     * @param command Command JSON object
     * @return Response JSON object or null if failed
     */
    suspend fun sendCommand(command: JSONObject): JSONObject? {
        return withContext(Dispatchers.IO) {
            try {
                // Try WiFi first
                if (networkClient != null && isWiFiConnected()) {
                    val response = networkClient?.sendCommand(command)
                    if (response != null) {
                        return@withContext response
                    }
                }
                
                // Fall back to Bluetooth if WiFi failed
                if (preferenceManager.getBluetoothFallback() && bluetoothClient != null) {
                    return@withContext bluetoothClient?.sendCommand(command)
                }
                
                null
            } catch (e: Exception) {
                Log.e("ConnectionRepository", "Error sending command", e)
                null
            }
        }
    }
    
    /**
     * Get the stream URL for the RTSP video feed
     * @return RTSP URL or null if not available
     */
    suspend fun getStreamUrl(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val ipAddress = preferenceManager.getWiFiIpAddress()
                val port = preferenceManager.getWiFiPort()
                val quality = preferenceManager.getStreamQuality()
                
                val qualityParam = when (quality) {
                    0 -> "low"
                    1 -> "medium"
                    2 -> "high"
                    else -> "medium"
                }
                
                // Construct RTSP URL
                "rtsp://$ipAddress:$port/stream?quality=$qualityParam"
            } catch (e: Exception) {
                Log.e("ConnectionRepository", "Error getting stream URL", e)
                null
            }
        }
    }
    
    /**
     * Check if we're connected to the server (either WiFi or Bluetooth)
     * @return true if connected
     */
    suspend fun isConnected(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Check WiFi connection
                if (networkClient != null && isWiFiConnected()) {
                    val wifiConnected = networkClient?.testConnection() ?: false
                    if (wifiConnected) {
                        return@withContext true
                    }
                }
                
                // Check Bluetooth connection
                if (bluetoothClient != null) {
                    return@withContext bluetoothClient?.isConnected() ?: false
                }
                
                false
            } catch (e: Exception) {
                Log.e("ConnectionRepository", "Error checking connection status", e)
                false
            }
        }
    }
}