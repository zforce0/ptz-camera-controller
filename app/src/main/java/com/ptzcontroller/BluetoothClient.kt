package com.ptzcontroller

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

/**
 * Client for Bluetooth communication with the camera server
 */
class BluetoothClient(private val context: Context) {

    // Bluetooth SPP UUID
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputWriter: BufferedWriter? = null
    private var inputReader: BufferedReader? = null
    private var connectedDevice: BluetoothDevice? = null
    
    init {
        // Initialize Bluetooth adapter
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }
    
    /**
     * Check if Bluetooth is available
     * @return true if Bluetooth is available
     */
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null
    }
    
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
        return if (isBluetoothAvailable() && isBluetoothEnabled()) {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    /**
     * Scan for Bluetooth devices
     * @return List of discovered devices
     */
    fun scanDevices(): List<BluetoothDevice> {
        // This would actually start a Bluetooth scan, but for simplicity,
        // we'll just return the paired devices for now
        return getPairedDevices()
    }
    
    /**
     * Connect to a Bluetooth device
     * @param device BluetoothDevice to connect to
     * @return true if connection successful
     */
    fun connect(device: BluetoothDevice): Boolean {
        try {
            // Disconnect existing connection if any
            disconnect()
            
            // Connect to the device
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            
            // Set up I/O streams
            outputWriter = BufferedWriter(OutputStreamWriter(bluetoothSocket?.outputStream))
            inputReader = BufferedReader(InputStreamReader(bluetoothSocket?.inputStream))
            
            // Store the connected device
            connectedDevice = device
            
            return true
        } catch (e: Exception) {
            Log.e("BluetoothClient", "Error connecting to device: ${device.address}", e)
            disconnect()
            return false
        }
    }
    
    /**
     * Disconnect from Bluetooth device
     */
    fun disconnect() {
        try {
            outputWriter?.close()
            inputReader?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            Log.e("BluetoothClient", "Error disconnecting", e)
        } finally {
            outputWriter = null
            inputReader = null
            bluetoothSocket = null
            connectedDevice = null
        }
    }
    
    /**
     * Check if connected to a device
     * @return true if connected
     */
    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }
    
    /**
     * Send a command to the server via Bluetooth
     * @param command Command JSON object
     * @return Response JSON object or null if failed
     */
    fun sendCommand(command: JSONObject): JSONObject? {
        return try {
            if (!isConnected()) {
                Log.e("BluetoothClient", "Not connected")
                return null
            }
            
            // Send the command
            outputWriter?.write(command.toString())
            outputWriter?.newLine()
            outputWriter?.flush()
            
            // Read the response
            val response = inputReader?.readLine()
            
            if (response != null) {
                JSONObject(response)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("BluetoothClient", "Error sending command", e)
            null
        }
    }
    
    /**
     * Get the connected device
     * @return Currently connected BluetoothDevice or null
     */
    fun getConnectedDevice(): BluetoothDevice? {
        return connectedDevice
    }
}