package com.ptzcontroller.ui.connection

/**
 * @deprecated Use com.ptzcontroller.data.model.BluetoothDeviceInfo instead.
 * This class is kept for backward compatibility but should not be used in new code.
 */
@Deprecated("Use com.ptzcontroller.data.model.BluetoothDeviceInfo instead")
data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val isConnected: Boolean = false,
    val isBonded: Boolean = false
)