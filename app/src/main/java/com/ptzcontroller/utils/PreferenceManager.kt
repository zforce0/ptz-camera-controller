package com.ptzcontroller.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manager for app preferences
 */
class PreferenceManager(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )
    
    /**
     * WiFi connection settings
     */
    fun getWiFiIpAddress(): String {
        return preferences.getString(KEY_WIFI_IP_ADDRESS, "192.168.1.100") ?: "192.168.1.100"
    }
    
    fun setWiFiIpAddress(ipAddress: String) {
        preferences.edit().putString(KEY_WIFI_IP_ADDRESS, ipAddress).apply()
    }
    
    fun getWiFiPort(): Int {
        return preferences.getInt(KEY_WIFI_PORT, 8000)
    }
    
    fun setWiFiPort(port: Int) {
        preferences.edit().putInt(KEY_WIFI_PORT, port).apply()
    }
    
    fun getWiFiTimeout(): Int {
        return preferences.getInt(KEY_WIFI_TIMEOUT, 10)
    }
    
    fun setWiFiTimeout(timeoutSeconds: Int) {
        preferences.edit().putInt(KEY_WIFI_TIMEOUT, timeoutSeconds).apply()
    }
    
    /**
     * Bluetooth connection settings
     */
    fun getBluetoothDeviceAddress(): String? {
        return preferences.getString(KEY_BLUETOOTH_DEVICE_ADDRESS, null)
    }
    
    fun setBluetoothDeviceAddress(address: String) {
        preferences.edit().putString(KEY_BLUETOOTH_DEVICE_ADDRESS, address).apply()
    }
    
    fun getBluetoothFallback(): Boolean {
        return preferences.getBoolean(KEY_BLUETOOTH_FALLBACK, true)
    }
    
    fun setBluetoothFallback(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_BLUETOOTH_FALLBACK, enabled).apply()
    }
    
    /**
     * Stream settings
     */
    fun getStreamQuality(): Int {
        return preferences.getInt(KEY_STREAM_QUALITY, 1)
    }
    
    fun setStreamQuality(quality: Int) {
        preferences.edit().putInt(KEY_STREAM_QUALITY, quality).apply()
    }
    
    /**
     * Control settings
     */
    fun getControlSensitivity(): Int {
        return preferences.getInt(KEY_CONTROL_SENSITIVITY, 50)
    }
    
    fun setControlSensitivity(sensitivity: Int) {
        preferences.edit().putInt(KEY_CONTROL_SENSITIVITY, sensitivity).apply()
    }
    
    fun getZoomSensitivity(): Int {
        return preferences.getInt(KEY_ZOOM_SENSITIVITY, 50)
    }
    
    fun setZoomSensitivity(sensitivity: Int) {
        preferences.edit().putInt(KEY_ZOOM_SENSITIVITY, sensitivity).apply()
    }
    
    /**
     * UI settings
     */
    fun getDarkMode(): Boolean {
        return preferences.getBoolean(KEY_DARK_MODE, false)
    }
    
    fun setDarkMode(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
    
    fun getKeepScreenOn(): Boolean {
        return preferences.getBoolean(KEY_KEEP_SCREEN_ON, true)
    }
    
    fun setKeepScreenOn(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
    }
    
    fun getHideControls(): Boolean {
        return preferences.getBoolean(KEY_HIDE_CONTROLS, false)
    }
    
    fun setHideControls(hidden: Boolean) {
        preferences.edit().putBoolean(KEY_HIDE_CONTROLS, hidden).apply()
    }
    
    /**
     * Default presets
     */
    fun getDefaultPreset(): Int {
        return preferences.getInt(KEY_DEFAULT_PRESET, 1)
    }
    
    fun setDefaultPreset(preset: Int) {
        preferences.edit().putInt(KEY_DEFAULT_PRESET, preset).apply()
    }
    
    companion object {
        private const val PREF_NAME = "ptz_controller_prefs"
        
        // WiFi settings
        private const val KEY_WIFI_IP_ADDRESS = "wifi_ip_address"
        private const val KEY_WIFI_PORT = "wifi_port"
        private const val KEY_WIFI_TIMEOUT = "wifi_timeout"
        
        // Bluetooth settings
        private const val KEY_BLUETOOTH_DEVICE_ADDRESS = "bluetooth_device_address"
        private const val KEY_BLUETOOTH_FALLBACK = "bluetooth_fallback"
        
        // Stream settings
        private const val KEY_STREAM_QUALITY = "stream_quality"
        
        // Control settings
        private const val KEY_CONTROL_SENSITIVITY = "control_sensitivity"
        private const val KEY_ZOOM_SENSITIVITY = "zoom_sensitivity"
        
        // UI settings
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_HIDE_CONTROLS = "hide_controls"
        
        // Presets
        private const val KEY_DEFAULT_PRESET = "default_preset"
    }
}