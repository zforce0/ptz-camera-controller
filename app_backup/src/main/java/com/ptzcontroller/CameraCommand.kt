package com.ptzcontroller

import java.io.Serializable

/**
 * Represents a command to be sent to the PTZ camera
 * 
 * @param type The type of command: "pan", "tilt", "zoom", "mode", etc.
 * @param value The value for the command: 
 *              - For pan/tilt/zoom: -100 to 100 representing speed percentage
 *              - For mode: 0 for RGB, 1 for IR/Thermal
 */
data class CameraCommand(val type: String, val value: Int) : Serializable {

    companion object {
        const val TYPE_PAN = "pan"
        const val TYPE_TILT = "tilt"
        const val TYPE_ZOOM = "zoom"
        const val TYPE_MODE = "mode"

        const val MODE_RGB = 0
        const val MODE_IR = 1
    }

    // Add this method
    override fun toString(): String {
        return "$type:$value"
    }
}
