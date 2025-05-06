package com.ptzcontroller.data.network

/**
 * Enum representing server status
 */
enum class ServerStatus {
    ONLINE,     // Server is online and responding
    OFFLINE,    // Server is offline or not responding
    LIMITED     // Server is online but with limited functionality
}