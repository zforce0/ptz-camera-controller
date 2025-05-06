package com.ptzcontroller.ui.connection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ptzcontroller.R
import com.ptzcontroller.data.model.BluetoothDeviceInfo

/**
 * Adapter for displaying Bluetooth devices in a RecyclerView
 */
class BluetoothDeviceInfoAdapter(
    private val listener: OnBluetoothDeviceClickListener
) : RecyclerView.Adapter<BluetoothDeviceInfoAdapter.ViewHolder>() {

    private val devices = mutableListOf<BluetoothDeviceInfo>()

    interface OnBluetoothDeviceClickListener {
        fun onDeviceClick(device: BluetoothDeviceInfo)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
        val deviceAddress: TextView = itemView.findViewById(R.id.device_address)
        val deviceStatus: TextView = itemView.findViewById(R.id.device_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        
        holder.deviceName.text = device.name
        holder.deviceAddress.text = device.address
        holder.deviceStatus.text = if (device.isConnected) "Connected" else "Available"
        
        // Set click listener
        holder.itemView.setOnClickListener {
            listener.onDeviceClick(device)
        }
    }

    override fun getItemCount(): Int = devices.size

    /**
     * Update the list of devices
     */
    fun updateDevices(newDevices: List<BluetoothDeviceInfo>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }

    /**
     * Add a device to the list
     */
    fun addDevice(device: BluetoothDeviceInfo) {
        // Check if device already exists
        val index = devices.indexOfFirst { it.address == device.address }
        if (index >= 0) {
            // Update existing device
            devices[index] = device
            notifyItemChanged(index)
        } else {
            // Add new device
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }

    /**
     * Clear the list of devices
     */
    fun clearDevices() {
        devices.clear()
        notifyDataSetChanged()
    }
}