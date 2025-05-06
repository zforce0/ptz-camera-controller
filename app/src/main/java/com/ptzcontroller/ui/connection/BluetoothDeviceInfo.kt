package com.ptzcontroller.ui.connection

/**
 * Data class to represent Bluetooth device information
 * Used for displaying device information in the UI
 */
data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val isConnected: Boolean = false,
    val isBonded: Boolean = false
)