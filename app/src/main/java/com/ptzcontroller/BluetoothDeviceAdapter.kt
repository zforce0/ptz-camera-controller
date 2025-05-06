package com.ptzcontroller.ui.connection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ptzcontroller.databinding.ItemBluetoothDeviceBinding

/**
 * Adapter for displaying Bluetooth devices in a RecyclerView
 * Uses the modern ListAdapter pattern with DiffUtil
 */
class BluetoothDeviceInfoAdapter(
    private val onDeviceClickListener: (BluetoothDeviceInfo) -> Unit
) : ListAdapter<BluetoothDeviceInfo, BluetoothDeviceInfoAdapter.ViewHolder>(BluetoothDeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBluetoothDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val deviceInfo = getItem(position)
        holder.bind(deviceInfo)
    }

    inner class ViewHolder(private val binding: ItemBluetoothDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeviceClickListener(getItem(position))
                }
            }
        }
        
        fun bind(deviceInfo: BluetoothDeviceInfo) {
            binding.deviceName.text = deviceInfo.name
            binding.deviceAddress.text = deviceInfo.address
        }
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    class BluetoothDeviceDiffCallback : DiffUtil.ItemCallback<BluetoothDeviceInfo>() {
        override fun areItemsTheSame(oldItem: BluetoothDeviceInfo, newItem: BluetoothDeviceInfo): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: BluetoothDeviceInfo, newItem: BluetoothDeviceInfo): Boolean {
            return oldItem == newItem
        }
    }
}