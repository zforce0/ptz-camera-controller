package com.ptzcontroller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothConnectionManager(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothConnectionMgr"
        private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID
    }

    private var bluetoothSocket: BluetoothSocket? = null
    private var connectedDevice: BluetoothDevice? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var connectionThread: ConnectThread? = null
    private var isConnected = false
    
    fun connect(device: BluetoothDevice): Boolean {
        // Cancel discovery because it otherwise slows down the connection
        // BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
        
        if (isConnected) {
            Log.d(TAG, "Already connected, disconnecting first")
            disconnect()
        }
        
        connectionThread = ConnectThread(device)
        connectionThread?.start()
        
        return true
    }
    
    fun disconnect() {
        connectionThread?.cancel()
        connectionThread = null
        
        try {
            outputStream?.close()
            inputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing connection", e)
        }
        
        connectedDevice = null
        outputStream = null
        inputStream = null
        bluetoothSocket = null
        isConnected = false
    }
    
    fun isConnected(): Boolean {
        return isConnected
    }
    
    fun sendCommand(commandString: String): Boolean {
        if (!isConnected || outputStream == null) {
            Log.e(TAG, "Cannot send command - not connected")
            return false
        }
        
        try {
            outputStream?.write(commandString.toByteArray())
            return true
        } catch (e: IOException) {
            Log.e(TAG, "Error sending command", e)
            // Connection was lost
            disconnect()
            return false
        }
    }
    
    fun getConnectedDeviceName(): String {
        return connectedDevice?.name ?: "Unknown Device"
    }
    
    // Thread for establishing the connection
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        
        init {
            connectedDevice = device
        }
        
        override fun run() {
            var tempSocket: BluetoothSocket? = null
            
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                tempSocket = connectedDevice?.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "Socket create() failed", e)
                return
            }
            
            bluetoothSocket = tempSocket
            
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                bluetoothSocket?.connect()
            } catch (connectException: IOException) {
                // Unable to connect; close the socket and return
                try {
                    bluetoothSocket?.close()
                } catch (closeException: IOException) {
                    Log.e(TAG, "Could not close the client socket", closeException)
                }
                return
            }
            
            // Get the input and output streams
            try {
                outputStream = bluetoothSocket?.outputStream
                inputStream = bluetoothSocket?.inputStream
                isConnected = true
                
                // Start listening for incoming messages
                val buffer = ByteArray(1024)
                var bytes: Int
                
                while (isConnected) {
                    try {
                        bytes = inputStream?.read(buffer) ?: 0
                        if (bytes > 0) {
                            // Process received data if needed
                            val receivedData = String(buffer, 0, bytes)
                            Log.d(TAG, "Received: $receivedData")
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Input stream disconnected", e)
                        break
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error getting streams", e)
                disconnect()
            }
        }
        
        fun cancel() {
            try {
                isConnected = false
                bluetoothSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }
}
