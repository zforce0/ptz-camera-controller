package com.ptzcontroller.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class for managing shared preferences
 */
class PreferenceManager(context: Context) {
    
    companion object {
        private const val PREFERENCES_NAME = "ptz_camera_prefs"
        
        // Keys for preferences
        private const val KEY_STREAM_QUALITY = "stream_quality"
        private const val KEY_CONNECTION_CHECK_INTERVAL = "connection_check_interval"
        private const val KEY_CONTROL_SENSITIVITY = "control_sensitivity"
        private const val KEY_SHOW_STATUS_OVERLAY = "show_status_overlay"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_AUTO_RECONNECT = "auto_reconnect"
        private const val KEY_BLUETOOTH_FALLBACK = "bluetooth_fallback"
        private const val KEY_WIFI_TIMEOUT = "wifi_timeout"
        private const val KEY_LAST_IP_ADDRESS = "last_ip_address"
        private const val KEY_LAST_PORT = "last_port"
        private const val KEY_LAST_BT_DEVICE = "last_bt_device"
        private const val KEY_DARK_MODE = "dark_mode"
    }
    
    // SharedPreferences instance
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    
    /**
     * Get stream quality preference
     * @return Quality index (0=Low, 1=Medium, 2=High)
     */
    fun getStreamQuality(): Int {
        return preferences.getInt(KEY_STREAM_QUALITY, 1) // Default: Medium
    }
    
    /**
     * Set stream quality preference
     * @param quality Quality index (0=Low, 1=Medium, 2=High)
     */
    fun setStreamQuality(quality: Int) {
        preferences.edit().putInt(KEY_STREAM_QUALITY, quality).apply()
    }
    
    /**
     * Get connection check interval preference
     * @return Interval index (0=5s, 1=15s, 2=30s)
     */
    fun getConnectionCheckInterval(): Int {
        return preferences.getInt(KEY_CONNECTION_CHECK_INTERVAL, 1) // Default: 15s
    }
    
    /**
     * Set connection check interval preference
     * @param interval Interval index (0=5s, 1=15s, 2=30s)
     */
    fun setConnectionCheckInterval(interval: Int) {
        preferences.edit().putInt(KEY_CONNECTION_CHECK_INTERVAL, interval).apply()
    }
    
    /**
     * Get control sensitivity preference
     * @return Sensitivity index (0=Low, 1=Medium, 2=High)
     */
    fun getControlSensitivity(): Int {
        return preferences.getInt(KEY_CONTROL_SENSITIVITY, 1) // Default: Medium
    }
    
    /**
     * Set control sensitivity preference
     * @param sensitivity Sensitivity index (0=Low, 1=Medium, 2=High)
     */
    fun setControlSensitivity(sensitivity: Int) {
        preferences.edit().putInt(KEY_CONTROL_SENSITIVITY, sensitivity).apply()
    }
    
    /**
     * Get show status overlay preference
     * @return true if status overlay should be shown, false otherwise
     */
    fun getShowStatusOverlay(): Boolean {
        return preferences.getBoolean(KEY_SHOW_STATUS_OVERLAY, true) // Default: Show
    }
    
    /**
     * Set show status overlay preference
     * @param show true if status overlay should be shown, false otherwise
     */
    fun setShowStatusOverlay(show: Boolean) {
        preferences.edit().putBoolean(KEY_SHOW_STATUS_OVERLAY, show).apply()
    }
    
    /**
     * Get keep screen on preference
     * @return true if screen should stay on, false otherwise
     */
    fun getKeepScreenOn(): Boolean {
        return preferences.getBoolean(KEY_KEEP_SCREEN_ON, true) // Default: Keep on
    }
    
    /**
     * Set keep screen on preference
     * @param keepOn true if screen should stay on, false otherwise
     */
    fun setKeepScreenOn(keepOn: Boolean) {
        preferences.edit().putBoolean(KEY_KEEP_SCREEN_ON, keepOn).apply()
    }
    
    /**
     * Get auto reconnect preference
     * @return true if auto reconnect is enabled, false otherwise
     */
    fun getAutoReconnect(): Boolean {
        return preferences.getBoolean(KEY_AUTO_RECONNECT, true) // Default: Enabled
    }
    
    /**
     * Set auto reconnect preference
     * @param autoReconnect true if auto reconnect is enabled, false otherwise
     */
    fun setAutoReconnect(autoReconnect: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_RECONNECT, autoReconnect).apply()
    }
    
    /**
     * Get Bluetooth fallback preference
     * @return true if Bluetooth fallback is enabled, false otherwise
     */
    fun getBluetoothFallback(): Boolean {
        return preferences.getBoolean(KEY_BLUETOOTH_FALLBACK, true) // Default: Enabled
    }
    
    /**
     * Set Bluetooth fallback preference
     * @param fallback true if Bluetooth fallback is enabled, false otherwise
     */
    fun setBluetoothFallback(fallback: Boolean) {
        preferences.edit().putBoolean(KEY_BLUETOOTH_FALLBACK, fallback).apply()
    }
    
    /**
     * Get WiFi timeout preference
     * @return Timeout in seconds
     */
    fun getWiFiTimeout(): Int {
        return preferences.getInt(KEY_WIFI_TIMEOUT, 10) // Default: 10 seconds
    }
    
    /**
     * Set WiFi timeout preference
     * @param timeout Timeout in seconds
     */
    fun setWiFiTimeout(timeout: Int) {
        preferences.edit().putInt(KEY_WIFI_TIMEOUT, timeout).apply()
    }
    
    /**
     * Get last used IP address
     * @return IP address string or empty string if none
     */
    fun getLastIpAddress(): String {
        return preferences.getString(KEY_LAST_IP_ADDRESS, "") ?: ""
    }
    
    /**
     * Set last used IP address
     * @param ipAddress IP address string
     */
    fun setLastIpAddress(ipAddress: String) {
        preferences.edit().putString(KEY_LAST_IP_ADDRESS, ipAddress).apply()
    }
    
    /**
     * Get last used port
     * @return Port number
     */
    fun getLastPort(): Int {
        return preferences.getInt(KEY_LAST_PORT, 8000) // Default: 8000
    }
    
    /**
     * Set last used port
     * @param port Port number
     */
    fun setLastPort(port: Int) {
        preferences.edit().putInt(KEY_LAST_PORT, port).apply()
    }
    
    /**
     * Get last used Bluetooth device address
     * @return Device address string or empty string if none
     */
    fun getLastBluetoothDevice(): String {
        return preferences.getString(KEY_LAST_BT_DEVICE, "") ?: ""
    }
    
    /**
     * Set last used Bluetooth device address
     * @param deviceAddress Device address string
     */
    fun setLastBluetoothDevice(deviceAddress: String) {
        preferences.edit().putString(KEY_LAST_BT_DEVICE, deviceAddress).apply()
    }
    
    /**
     * Get dark mode preference
     * @return true if dark mode is enabled, false otherwise
     */
    fun getDarkMode(): Boolean {
        return preferences.getBoolean(KEY_DARK_MODE, false) // Default: System default
    }
    
    /**
     * Set dark mode preference
     * @param darkMode true if dark mode is enabled, false otherwise
     */
    fun setDarkMode(darkMode: Boolean) {
        preferences.edit().putBoolean(KEY_DARK_MODE, darkMode).apply()
    }
}