package com.ptzcontroller

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Client for network communication with the camera server
 */
class NetworkClient(
    private val ipAddress: String,
    private val port: Int,
    private val timeout: Int = 10 // seconds
) {
    private var socket: Socket? = null
    
    /**
     * Test the connection to the server
     * @return true if connection successful
     */
    fun testConnection(): Boolean {
        return try {
            val url = URL("http://$ipAddress:$port/status")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = timeout * 1000
            connection.readTimeout = timeout * 1000
            connection.requestMethod = "GET"
            
            val responseCode = connection.responseCode
            responseCode == 200
        } catch (e: Exception) {
            Log.e("NetworkClient", "Error testing connection", e)
            false
        }
    }
    
    /**
     * Send a command to the server via HTTP
     * @param command Command JSON object
     * @return Response JSON object or null if failed
     */
    fun sendCommand(command: JSONObject): JSONObject? {
        return try {
            val url = URL("http://$ipAddress:$port/command")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = timeout * 1000
            connection.readTimeout = timeout * 1000
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            
            // Write the command
            val outputStream = connection.outputStream
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))
            writer.write(command.toString())
            writer.flush()
            writer.close()
            
            // Read the response
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                
                JSONObject(response.toString())
            } else {
                Log.e("NetworkClient", "Error response code: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("NetworkClient", "Error sending command", e)
            null
        }
    }
    
    /**
     * Connect to the server via TCP socket
     * @return true if connection successful
     */
    fun connect(): Boolean {
        return try {
            socket = Socket(ipAddress, port)
            socket?.soTimeout = timeout * 1000
            socket != null
        } catch (e: Exception) {
            Log.e("NetworkClient", "Error connecting to server", e)
            false
        }
    }
    
    /**
     * Send a command to the server via TCP socket
     * @param command Command string
     * @return Response string or null if failed
     */
    fun sendSocketCommand(command: String): String? {
        return try {
            if (socket == null || socket?.isClosed == true) {
                if (!connect()) {
                    return null
                }
            }
            
            // Send the command
            val outputStream = socket?.getOutputStream()
            outputStream?.write("$command\n".toByteArray())
            outputStream?.flush()
            
            // Read the response
            val inputStream = socket?.getInputStream()
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = reader.readLine()
            
            response
        } catch (e: Exception) {
            Log.e("NetworkClient", "Error sending socket command", e)
            null
        }
    }
    
    /**
     * Disconnect from the server
     */
    fun disconnect() {
        try {
            socket?.close()
            socket = null
        } catch (e: Exception) {
            Log.e("NetworkClient", "Error disconnecting", e)
        }
    }
}