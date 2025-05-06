package com.ptzcontroller.data.model

import android.bluetooth.BluetoothDevice

/**
 * Data class containing information about a Bluetooth device
 */
data class BluetoothDeviceInfo(
    val device: BluetoothDevice,
    val name: String,
    val address: String,
    val isConnected: Boolean = false,
    val signalStrength: Int = 0
) {
    companion object {
        /**
         * Create from a BluetoothDevice
         */
        fun fromBluetoothDevice(device: BluetoothDevice, isConnected: Boolean = false): BluetoothDeviceInfo {
            return BluetoothDeviceInfo(
                device = device,
                name = device.name ?: "Unknown Device",
                address = device.address,
                isConnected = isConnected
            )
        }
    }
}