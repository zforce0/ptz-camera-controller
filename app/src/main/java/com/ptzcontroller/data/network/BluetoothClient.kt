package com.ptzcontroller.data.network

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Client for communicating with the camera server over Bluetooth
 */
class BluetoothClient(private val context: Context) {
    companion object {
        private const val TAG = "BluetoothClient"
        // Standard UUID for SPP (Serial Port Profile)
        private val UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    
    private val connected = AtomicBoolean(false)

    /**
     * Check if Bluetooth is enabled
     * @return true if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Get a list of paired Bluetooth devices
     * @return List of paired devices
     */
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!isBluetoothEnabled()) {
            return emptyList()
        }
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    /**
     * Scan for Bluetooth devices
     * @return List of devices found
     */
    fun scanDevices(): List<BluetoothDevice> {
        // This is a simplified implementation that just returns paired devices
        // A real implementation would use BluetoothLeScanner or the classic discovery process
        return getPairedDevices()
    }

    /**
     * Connect to a Bluetooth device
     * @param device BluetoothDevice to connect to
     * @return true if connection successful
     */
    fun connect(device: BluetoothDevice): Boolean {
        if (connected.get()) {
            disconnect()
        }
        
        try {
            // Create a socket
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_SPP)
            bluetoothSocket?.connect()
            
            // Get input and output streams
            outputStream = bluetoothSocket?.outputStream
            inputStream = bluetoothSocket?.inputStream
            
            connected.set(true)
            return true
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to device", e)
            disconnect()
            return false
        }
    }

    /**
     * Send a command to the Bluetooth device
     * @param command JSON command to send
     * @return Response JSON or null if failed
     */
    fun sendCommand(command: JSONObject): JSONObject? {
        if (!connected.get()) {
            return null
        }
        
        try {
            // Convert command to byte array
            val commandBytes = command.toString().toByteArray()
            
            // Write command to output stream
            outputStream?.write(commandBytes)
            outputStream?.flush()
            
            // Read response from input stream
            val buffer = ByteArray(1024)
            val bytesRead = inputStream?.read(buffer) ?: -1
            
            if (bytesRead > 0) {
                val responseString = String(buffer, 0, bytesRead)
                return JSONObject(responseString)
            }
            
            return null
        } catch (e: IOException) {
            Log.e(TAG, "Error sending command", e)
            return null
        }
    }

    /**
     * Check if client is connected
     * @return true if connected
     */
    fun isConnected(): Boolean {
        return connected.get()
    }

    /**
     * Disconnect from the Bluetooth device
     */
    fun disconnect() {
        try {
            outputStream?.close()
            inputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error disconnecting", e)
        } finally {
            outputStream = null
            inputStream = null
            bluetoothSocket = null
            connected.set(false)
        }
    }
}