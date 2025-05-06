package com.ptzcontroller.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class for managing shared preferences
 */
class PreferenceManager(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "PTZControllerPrefs"
        
        // Connection preferences
        private const val KEY_WIFI_IP_ADDRESS = "wifi_ip_address"
        private const val KEY_WIFI_PORT = "wifi_port"
        private const val KEY_WIFI_TIMEOUT = "wifi_timeout"
        private const val KEY_BLUETOOTH_ADDRESS = "bluetooth_address"
        private const val KEY_BLUETOOTH_FALLBACK = "bluetooth_fallback"
        
        // Video preferences
        private const val KEY_STREAM_QUALITY = "stream_quality"
        private const val KEY_AUTO_RECONNECT = "auto_reconnect"
        
        // Control preferences
        private const val KEY_CONTROL_SENSITIVITY = "control_sensitivity"
        private const val KEY_ZOOM_SENSITIVITY = "zoom_sensitivity"
        private const val KEY_CONTROL_INVERT_PAN = "control_invert_pan"
        private const val KEY_CONTROL_INVERT_TILT = "control_invert_tilt"
        
        // UI preferences
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_DARK_MODE = "dark_mode"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // WiFi IP Address
    fun setWiFiIpAddress(ipAddress: String) {
        prefs.edit().putString(KEY_WIFI_IP_ADDRESS, ipAddress).apply()
    }
    
    fun getWiFiIpAddress(): String {
        return prefs.getString(KEY_WIFI_IP_ADDRESS, "192.168.1.100") ?: "192.168.1.100"
    }
    
    // WiFi Port
    fun setWiFiPort(port: Int) {
        prefs.edit().putInt(KEY_WIFI_PORT, port).apply()
    }
    
    fun getWiFiPort(): Int {
        return prefs.getInt(KEY_WIFI_PORT, 8000)
    }
    
    // WiFi Timeout
    fun setWiFiTimeout(timeout: Long) {
        prefs.edit().putLong(KEY_WIFI_TIMEOUT, timeout).apply()
    }
    
    fun getWiFiTimeout(): Long {
        return prefs.getLong(KEY_WIFI_TIMEOUT, 3000)
    }
    
    // Bluetooth Device Address
    fun setBluetoothDeviceAddress(address: String) {
        prefs.edit().putString(KEY_BLUETOOTH_ADDRESS, address).apply()
    }
    
    fun getBluetoothDeviceAddress(): String? {
        return prefs.getString(KEY_BLUETOOTH_ADDRESS, null)
    }
    
    // Bluetooth Fallback
    fun setBluetoothFallback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BLUETOOTH_FALLBACK, enabled).apply()
    }
    
    fun getBluetoothFallback(): Boolean {
        return prefs.getBoolean(KEY_BLUETOOTH_FALLBACK, true)
    }
    
    // Video Stream Quality (0 = low, 1 = medium, 2 = high)
    fun setStreamQuality(quality: Int) {
        prefs.edit().putInt(KEY_STREAM_QUALITY, quality).apply()
    }
    
    fun getStreamQuality(): Int {
        return prefs.getInt(KEY_STREAM_QUALITY, 1)
    }
    
    // Auto Reconnect
    fun setAutoReconnect(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RECONNECT, enabled).apply()
    }
    
    fun getAutoReconnect(): Boolean {
        return prefs.getBoolean(KEY_AUTO_RECONNECT, true)
    }
    
    // Control Sensitivity (0 = low, 1 = medium, 2 = high)
    fun setControlSensitivity(sensitivity: Int) {
        prefs.edit().putInt(KEY_CONTROL_SENSITIVITY, sensitivity).apply()
    }
    
    fun getControlSensitivity(): Int {
        return prefs.getInt(KEY_CONTROL_SENSITIVITY, 1)
    }
    
    // Invert Pan Control
    fun setInvertPan(invert: Boolean) {
        prefs.edit().putBoolean(KEY_CONTROL_INVERT_PAN, invert).apply()
    }
    
    fun getInvertPan(): Boolean {
        return prefs.getBoolean(KEY_CONTROL_INVERT_PAN, false)
    }
    
    // Invert Tilt Control
    fun setInvertTilt(invert: Boolean) {
        prefs.edit().putBoolean(KEY_CONTROL_INVERT_TILT, invert).apply()
    }
    
    fun getInvertTilt(): Boolean {
        return prefs.getBoolean(KEY_CONTROL_INVERT_TILT, false)
    }
    
    // Zoom Sensitivity
    fun setZoomSensitivity(sensitivity: Int) {
        prefs.edit().putInt(KEY_ZOOM_SENSITIVITY, sensitivity).apply()
    }
    
    fun getZoomSensitivity(): Int {
        return prefs.getInt(KEY_ZOOM_SENSITIVITY, 50)
    }
    
    // Keep Screen On
    fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
    }
    
    fun getKeepScreenOn(): Boolean {
        return prefs.getBoolean(KEY_KEEP_SCREEN_ON, true)
    }
    
    // Dark Mode
    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
    
    fun getDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }
    
    // Clear all preferences
    fun clearAllPreferences() {
        prefs.edit().clear().apply()
    }
}