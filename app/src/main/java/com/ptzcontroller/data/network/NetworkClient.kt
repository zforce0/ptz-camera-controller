package com.ptzcontroller.data.network

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Client for communicating with the camera server over WiFi
 */
class NetworkClient(
    private val ipAddress: String,
    private val port: Int,
    private val timeoutMillis: Long = 3000
) {
    companion object {
        private const val TAG = "NetworkClient"
    }

    /**
     * Test connection to the server
     * @return true if connection successful
     */
    fun testConnection(): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ipAddress, port), timeoutMillis.toInt())
            socket.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            false
        }
    }

    /**
     * Send a command to the server
     * @param command JSON command to send
     * @return Response JSON or null if failed
     */
    fun sendCommand(command: JSONObject): JSONObject? {
        return try {
            val url = URL("http://$ipAddress:$port/api")
            val connection = url.openConnection() as HttpURLConnection
            
            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = timeoutMillis.toInt()
                connection.readTimeout = timeoutMillis.toInt()
                
                // Send request
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(command.toString())
                    writer.flush()
                }
                
                // Check response
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Parse response
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        val response = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        return JSONObject(response.toString())
                    }
                } else {
                    Log.e(TAG, "Server returned error: $responseCode")
                    null
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending command", e)
            null
        }
    }

    /**
     * Disconnect from the server
     */
    fun disconnect() {
        // No persistent connection to close
    }
}