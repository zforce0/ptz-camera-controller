package com.ptzcontroller.data.repository

import android.content.Context
import android.util.Log
import com.ptzcontroller.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository for managing application settings
 */
class SettingsRepository(
    private val context: Context,
    private val preferenceManager: PreferenceManager
) {
    
    companion object {
        private const val TAG = "SettingsRepository"
        private const val LOG_DIR = "logs"
    }
    
    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        // Stream quality (medium)
        preferenceManager.setStreamQuality(1)
        
        // Connection check interval (15 seconds)
        preferenceManager.setConnectionCheckInterval(1)
        
        // Control sensitivity (medium)
        preferenceManager.setControlSensitivity(1)
        
        // Status overlay (show)
        preferenceManager.setShowStatusOverlay(true)
        
        // Keep screen on (enabled)
        preferenceManager.setKeepScreenOn(true)
        
        // Auto reconnect (enabled)
        preferenceManager.setAutoReconnect(true)
        
        // Bluetooth fallback (enabled)
        preferenceManager.setBluetoothFallback(true)
        
        // WiFi timeout (10 seconds)
        preferenceManager.setWiFiTimeout(10)
        
        // Dark mode (system default)
        preferenceManager.setDarkMode(false)
    }
    
    /**
     * Export application logs for troubleshooting
     * @return Path to the log file, or null if error
     */
    suspend fun exportLogs(): String? = withContext(Dispatchers.IO) {
        try {
            // Create logs directory if it doesn't exist
            val logsDir = File(context.getExternalFilesDir(null), LOG_DIR)
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            
            // Create timestamp for log file name
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val logFile = File(logsDir, "ptz_controller_log_$timestamp.txt")
            
            // Write logs
            FileWriter(logFile).use { writer ->
                writer.write("PTZ Camera Controller Logs\n")
                writer.write("Generated: ${Date()}\n")
                writer.write("App Version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}\n")
                writer.write("Device: ${android.os.Build.MODEL} (${android.os.Build.PRODUCT})\n")
                writer.write("Android Version: ${android.os.Build.VERSION.RELEASE}\n")
                writer.write("\n--- Settings ---\n")
                writer.write("Stream Quality: ${getStreamQualityString(preferenceManager.getStreamQuality())}\n")
                writer.write("Connection Check Interval: ${getConnectionCheckIntervalString(preferenceManager.getConnectionCheckInterval())}\n")
                writer.write("Control Sensitivity: ${getControlSensitivityString(preferenceManager.getControlSensitivity())}\n")
                writer.write("Show Status Overlay: ${preferenceManager.getShowStatusOverlay()}\n")
                writer.write("Keep Screen On: ${preferenceManager.getKeepScreenOn()}\n")
                writer.write("Auto Reconnect: ${preferenceManager.getAutoReconnect()}\n")
                writer.write("Bluetooth Fallback: ${preferenceManager.getBluetoothFallback()}\n")
                writer.write("WiFi Timeout: ${preferenceManager.getWiFiTimeout()} seconds\n")
                writer.write("Dark Mode: ${preferenceManager.getDarkMode()}\n")
                
                // Get system logs
                writer.write("\n--- System Logs ---\n")
                val process = Runtime.getRuntime().exec("logcat -d")
                process.inputStream.bufferedReader().use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line?.contains("com.example.ptzcameracontroller") == true) {
                            writer.write("$line\n")
                        }
                    }
                }
            }
            
            logFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting logs: ${e.message}")
            null
        }
    }
    
    /**
     * Get string representation of stream quality
     * @param qualityIndex Index from preferences
     * @return String description of quality
     */
    private fun getStreamQualityString(qualityIndex: Int): String {
        return when (qualityIndex) {
            0 -> "Low"
            1 -> "Medium"
            2 -> "High"
            else -> "Medium"
        }
    }
    
    /**
     * Get string representation of connection check interval
     * @param intervalIndex Index from preferences
     * @return String description of interval
     */
    private fun getConnectionCheckIntervalString(intervalIndex: Int): String {
        return when (intervalIndex) {
            0 -> "5 seconds"
            1 -> "15 seconds"
            2 -> "30 seconds"
            else -> "15 seconds"
        }
    }
    
    /**
     * Get string representation of control sensitivity
     * @param sensitivityIndex Index from preferences
     * @return String description of sensitivity
     */
    private fun getControlSensitivityString(sensitivityIndex: Int): String {
        return when (sensitivityIndex) {
            0 -> "Low"
            1 -> "Medium"
            2 -> "High"
            else -> "Medium"
        }
    }
}