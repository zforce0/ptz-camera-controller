package com.ptzcontroller

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class WifiConnectionManager(private val context: Context) {

    companion object {
        private const val TAG = "WifiConnectionManager"
        private const val CONNECTION_TIMEOUT = 5000 // 5 seconds
    }

    private var socket: Socket? = null
    private var serverIp: String? = null
    private var serverPort: Int = 8000
    private var isConnected = false
    private var outputWriter: OutputStreamWriter? = null
    private var inputReader: BufferedReader? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    
    fun connect(ipAddress: String, port: Int): Boolean {
        if (isConnected) {
            Log.d(TAG, "Already connected, disconnecting first")
            disconnect()
        }
        
        serverIp = ipAddress
        serverPort = port
        
        executor.execute {
            try {
                socket = Socket(serverIp, serverPort)
                socket?.soTimeout = CONNECTION_TIMEOUT
                
                outputWriter = OutputStreamWriter(socket?.getOutputStream())
                inputReader = BufferedReader(InputStreamReader(socket?.getInputStream()))
                
                isConnected = true
                Log.d(TAG, "Connected to $serverIp:$serverPort")
                
                // Start listening for incoming data
                listenForData()
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to server", e)
                disconnect()
            }
        }
        
        return true
    }
    
    fun disconnect() {
        isConnected = false
        
        try {
            outputWriter?.close()
            inputReader?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing connection", e)
        }
        
        outputWriter = null
        inputReader = null
        socket = null
    }
    
    fun isConnected(): Boolean {
        return isConnected && socket != null && socket?.isConnected == true && !socket?.isClosed!!
    }
    
    fun sendCommand(commandString: String): Boolean {
        if (!isConnected || outputWriter == null) {
            Log.e(TAG, "Cannot send command - not connected")
            return false
        }
        
        executor.execute {
            try {
                outputWriter?.write(commandString + "\n")
                outputWriter?.flush()
                Log.d(TAG, "Sent command: $commandString")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending command", e)
                disconnect()
            }
        }
        
        return true
    }
    
    fun sendHttpCommand(command: String): Boolean {
        if (serverIp == null) {
            Log.e(TAG, "Server IP not set")
            return false
        }
        
        executor.execute {
            try {
                val url = URL("http://$serverIp:$serverPort/command")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = CONNECTION_TIMEOUT
                
                val jsonCommand = JSONObject(command)
                
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonCommand.toString())
                writer.flush()
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Command sent successfully")
                } else {
                    Log.e(TAG, "HTTP error: $responseCode")
                }
                
                writer.close()
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error sending HTTP command", e)
            }
        }
        
        return true
    }
    
    fun getConnectedDeviceName(): String {
        return "$serverIp:$serverPort"
    }
    
    private fun listenForData() {
        try {
            var line: String?
            while (isConnected && inputReader != null) {
                line = inputReader?.readLine()
                if (line != null) {
                    Log.d(TAG, "Received: $line")
                    // Process received data if needed
                } else {
                    // End of stream, connection closed
                    break
                }
            }
        } catch (e: Exception) {
            if (isConnected) {
                Log.e(TAG, "Error reading from socket", e)
                disconnect()
            }
        }
    }
}
