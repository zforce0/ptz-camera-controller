package com.ptzcontroller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * ConnectionFragment handles UI and logic for connecting to the camera server
 * via WiFi or Bluetooth
 */
class ConnectionFragment : Fragment() {
    
    companion object {
        @JvmStatic
        fun newInstance(connectionManager: ConnectionManager): ConnectionFragment {
            val fragment = ConnectionFragment()
            val args = Bundle()
            args.putSerializable("connectionManager", connectionManager)
            fragment.arguments = args
            return fragment
        }
    }
    
    private lateinit var connectionManager: ConnectionManager
    
    // UI components
    private lateinit var radioWifi: RadioButton
    private lateinit var radioBluetooth: RadioButton
    private lateinit var editIpAddress: EditText
    private lateinit var editPort: EditText
    private lateinit var spinnerBtDevices: Spinner
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button
    private lateinit var btnScan: Button
    private lateinit var layoutWifi: LinearLayout
    private lateinit var layoutBluetooth: LinearLayout
    private lateinit var statusTextView: TextView
    
    // Using a dummy BluetoothAdapter for compilation
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val devicesList = ArrayList<DummyBluetoothDevice>()
    private lateinit var devicesAdapter: ArrayAdapter<String>
    
    // Constants
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_PERMISSIONS = 2
    
    // Dummy BluetoothDevice for compilation
    class DummyBluetoothDevice {
        var name: String = "Dummy Device"
        var address: String = "00:00:00:00:00:00"
    }
    
    // Dummy receiver for Bluetooth device discovery
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Dummy implementation
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            connectionManager = it.getSerializable("connectionManager") as ConnectionManager
        }
        
        // Register for dummy broadcasts
        val filter = IntentFilter()
        requireActivity().registerReceiver(receiver, filter)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connection, container, false)
        
        // Initialize UI components
        radioWifi = view.findViewById(R.id.radio_wifi)
        radioBluetooth = view.findViewById(R.id.radio_bluetooth)
        editIpAddress = view.findViewById(R.id.edit_ip_address)
        editPort = view.findViewById(R.id.edit_port)
        spinnerBtDevices = view.findViewById(R.id.spinner_bt_devices)
        btnConnect = view.findViewById(R.id.btn_connect)
        btnDisconnect = view.findViewById(R.id.btn_disconnect)
        btnScan = view.findViewById(R.id.btn_scan)
        layoutWifi = view.findViewById(R.id.layout_wifi)
        layoutBluetooth = view.findViewById(R.id.layout_bluetooth)
        statusTextView = view.findViewById(R.id.status_text)
        
        // Initialize Bluetooth devices adapter
        devicesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ArrayList<String>())
        devicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBtDevices.adapter = devicesAdapter
        
        // Add some dummy devices for testing
        addDummyDevices()
        
        // Set up connection type radio buttons
        radioWifi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                layoutWifi.visibility = View.VISIBLE
                layoutBluetooth.visibility = View.GONE
            }
        }
        
        radioBluetooth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                layoutWifi.visibility = View.GONE
                layoutBluetooth.visibility = View.VISIBLE
                
                // Check if Bluetooth is available and enabled
                if (bluetoothAdapter == null) {
                    Toast.makeText(context, "Bluetooth is not available on this device", Toast.LENGTH_SHORT).show()
                    radioWifi.isChecked = true
                } else if (bluetoothAdapter.isEnabled) {
                    // Bluetooth is enabled, we're good to go
                } else {
                    // Request to enable Bluetooth
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }
            }
        }
        
        // Set up scan button
        btnScan.setOnClickListener {
            // Dummy scan operation that adds a new device
            val newDevice = DummyBluetoothDevice()
            newDevice.name = "PTZ Camera ${(devicesList.size + 1)}"
            newDevice.address = "00:00:00:00:00:${devicesList.size + 1}"
            
            devicesList.add(newDevice)
            devicesAdapter.add("${newDevice.name} (${newDevice.address})")
            devicesAdapter.notifyDataSetChanged()
            
            Toast.makeText(context, "Scanning for devices...", Toast.LENGTH_SHORT).show()
        }
        
        // Set up connect button
        btnConnect.setOnClickListener {
            if (radioWifi.isChecked) {
                // Connect via WiFi
                val ip = editIpAddress.text.toString()
                val portStr = editPort.text.toString()
                
                if (ip.isEmpty()) {
                    Toast.makeText(context, "Please enter an IP address", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                if (portStr.isEmpty()) {
                    Toast.makeText(context, "Please enter a port number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val port = portStr.toInt()
                
                val success = connectionManager.connectWifi(ip, port)
                
                if (success) {
                    updateConnectionStatus(true)
                    Toast.makeText(context, "Connected to $ip:$port", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show()
                }
                
            } else {
                // Connect via Bluetooth
                val position = spinnerBtDevices.selectedItemPosition
                
                if (position < 0 || position >= devicesList.size) {
                    Toast.makeText(context, "Please select a device", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val device = devicesList[position]
                val success = connectionManager.connectBluetooth(device.address)
                
                if (success) {
                    updateConnectionStatus(true)
                    Toast.makeText(context, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Set up disconnect button
        btnDisconnect.setOnClickListener {
            val success = connectionManager.disconnect()
            
            if (success) {
                updateConnectionStatus(false)
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to disconnect", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Set default values
        editIpAddress.setText("192.168.1.100")
        editPort.setText("8000")
        
        // Initialize connection status
        updateConnectionStatus(connectionManager.isConnected())
        
        return view
    }
    
    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(receiver)
    }
    
    override fun onResume() {
        super.onResume()
        updateConnectionStatus(connectionManager.isConnected())
    }
    
    private fun updateConnectionStatus(connected: Boolean) {
        if (connected) {
            statusTextView.text = "Connected to: ${connectionManager.getConnectedDeviceName()}"
            statusTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            btnConnect.isEnabled = false
            btnDisconnect.isEnabled = true
        } else {
            statusTextView.text = getString(R.string.status_not_connected)
            statusTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            btnConnect.isEnabled = true
            btnDisconnect.isEnabled = false
        }
    }
    
    private fun addDummyDevices() {
        // Add some dummy Bluetooth devices for testing
        for (i in 1..3) {
            val device = DummyBluetoothDevice()
            device.name = "PTZ Camera $i"
            device.address = "00:00:00:00:00:0$i"
            
            devicesList.add(device)
            devicesAdapter.add("${device.name} (${device.address})")
        }
        
        devicesAdapter.notifyDataSetChanged()
    }
}