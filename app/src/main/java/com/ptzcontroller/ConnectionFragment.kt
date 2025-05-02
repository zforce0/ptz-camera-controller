package com.ptzcontroller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class ConnectionFragment : Fragment() {

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
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val devicesList = ArrayList<BluetoothDevice>()
    private lateinit var devicesAdapter: ArrayAdapter<String>
    
    // Constants
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_PERMISSIONS = 2
    
    // Receiver for Bluetooth device discovery
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        val deviceName = device.name ?: "Unknown Device"
                        val deviceAddress = device.address
                        val deviceString = "$deviceName ($deviceAddress)"
                        
                        if (!devicesList.contains(device)) {
                            devicesList.add(device)
                            devicesAdapter.add(deviceString)
                            devicesAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            connectionManager = it.getSerializable("connectionManager") as ConnectionManager
        }
        
        // Register for broadcasts when a device is discovered
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
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
        
        // Set up Bluetooth devices adapter
        devicesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ArrayList<String>())
        devicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBtDevices.adapter = devicesAdapter
        
        // Default values
        editPort.setText("8000")
        
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
                
                // Check Bluetooth permissions
                checkBluetoothPermissions()
            }
        }
        
        // Set up buttons
        btnConnect.setOnClickListener {
            connectToDevice()
        }
        
        btnDisconnect.setOnClickListener {
            connectionManager.disconnect()
            updateConnectionStatus()
        }
        
        btnScan.setOnClickListener {
            scanForBluetoothDevices()
        }
        
        // Initial UI state
        radioWifi.isChecked = true
        updateConnectionStatus()
        
        return view
    }
    
    private fun connectToDevice() {
        if (radioWifi.isChecked) {
            val ipAddress = editIpAddress.text.toString()
            val port = editPort.text.toString().toIntOrNull() ?: 8000
            
            if (ipAddress.isEmpty()) {
                Toast.makeText(context, "Please enter an IP address", Toast.LENGTH_SHORT).show()
                return
            }
            
            try {
                connectionManager.connectViaWifi(ipAddress, port)
                Toast.makeText(context, "Connecting via WiFi...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Connection error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else if (radioBluetooth.isChecked) {
            if (devicesList.isEmpty() || spinnerBtDevices.selectedItemPosition < 0) {
                Toast.makeText(context, "Please select a Bluetooth device", Toast.LENGTH_SHORT).show()
                return
            }
            
            val selectedDevice = devicesList[spinnerBtDevices.selectedItemPosition]
            try {
                connectionManager.connectViaBluetooth(selectedDevice)
                Toast.makeText(context, "Connecting via Bluetooth...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Connection error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        updateConnectionStatus()
    }
    
    private fun scanForBluetoothDevices() {
        // Clear previous devices
        devicesList.clear()
        devicesAdapter.clear()
        
        // Check if Bluetooth is enabled
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return
        }
        
        // Check permissions
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Bluetooth permissions required", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Add paired devices
        val pairedDevices = bluetoothAdapter.bondedDevices
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                val deviceName = device.name
                val deviceAddress = device.address
                devicesList.add(device)
                devicesAdapter.add("$deviceName ($deviceAddress)")
            }
        }
        
        // Start discovery
        Toast.makeText(context, "Scanning for Bluetooth devices...", Toast.LENGTH_SHORT).show()
        bluetoothAdapter.startDiscovery()
    }
    
    private fun checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_PERMISSIONS
            )
        }
    }
    
    private fun updateConnectionStatus() {
        if (connectionManager.isConnected()) {
            statusTextView.text = "Status: Connected to ${connectionManager.getConnectedDeviceName()}"
            statusTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            btnConnect.isEnabled = false
            btnDisconnect.isEnabled = true
        } else {
            statusTextView.text = "Status: Not connected"
            statusTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            btnConnect.isEnabled = true
            btnDisconnect.isEnabled = false
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(receiver)
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
    }
}
