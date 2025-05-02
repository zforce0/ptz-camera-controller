package com.ptzcontroller

import android.bluetooth.BluetoothDevice
import android.content.Context
import org.json.JSONObject
import java.io.Serializable

enum class ConnectionType {
    BLUETOOTH,
    WIFI,
    NONE
}

class ConnectionManager(private val context: Context) : Serializable {

    private val bluetoothManager = BluetoothConnectionManager(context)
    private val wifiManager = WifiConnectionManager(context)
    private var activeConnectionType = ConnectionType.NONE
    
    fun connectViaBluetooth(device: BluetoothDevice): Boolean {
        if (activeConnectionType != ConnectionType.NONE) {
            disconnect()
        }
        
        val success = bluetoothManager.connect(device)
        if (success) {
            activeConnectionType = ConnectionType.BLUETOOTH
        }
        
        return success
    }
    
    fun connectViaWifi(ipAddress: String, port: Int): Boolean {
        if (activeConnectionType != ConnectionType.NONE) {
            disconnect()
        }
        
        val success = wifiManager.connect(ipAddress, port)
        if (success) {
            activeConnectionType = ConnectionType.WIFI
        }
        
        return success
    }
    
    fun disconnect() {
        when (activeConnectionType) {
            ConnectionType.BLUETOOTH -> bluetoothManager.disconnect()
            ConnectionType.WIFI -> wifiManager.disconnect()
            else -> {} // No active connection
        }
        
        activeConnectionType = ConnectionType.NONE
    }
    
    fun isConnected(): Boolean {
        return when (activeConnectionType) {
            ConnectionType.BLUETOOTH -> bluetoothManager.isConnected()
            ConnectionType.WIFI -> wifiManager.isConnected()
            else -> false
        }
    }
    
    fun sendCommand(command: CameraCommand): Boolean {
        if (!isConnected()) {
            return false
        }
        
        val jsonCommand = JSONObject()
        jsonCommand.put("type", command.type)
        jsonCommand.put("value", command.value)
        
        val commandString = jsonCommand.toString()
        
        return when (activeConnectionType) {
            ConnectionType.BLUETOOTH -> bluetoothManager.sendCommand(commandString)
            ConnectionType.WIFI -> wifiManager.sendCommand(commandString)
            else -> false
        }
    }
    
    fun getConnectedDeviceName(): String {
        return when (activeConnectionType) {
            ConnectionType.BLUETOOTH -> bluetoothManager.getConnectedDeviceName()
            ConnectionType.WIFI -> wifiManager.getConnectedDeviceName()
            else -> "Not connected"
        }
    }
    
    fun getConnectionType(): ConnectionType {
        return activeConnectionType
    }
}
