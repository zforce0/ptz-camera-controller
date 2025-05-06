package com.ptzcontroller.ui.connection

/**
 * Enum representing connection status
 */
enum class ConnectionStatus {
    CONNECTED,      // Connected to the server
    CONNECTING,     // Attempting to connect
    DISCONNECTED,   // Not connected to the server
    FAILED          // Connection attempt failed
}