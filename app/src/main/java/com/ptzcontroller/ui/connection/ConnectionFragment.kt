package com.ptzcontroller.ui.connection

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ptzcontroller.ConnectionRepository
import com.ptzcontroller.databinding.FragmentConnectionBinding
import com.ptzcontroller.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnectionFragment : Fragment() {

    private var _binding: FragmentConnectionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var connectionRepository: ConnectionRepository
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var bluetoothDeviceAdapter: BluetoothDeviceAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionBinding.inflate(inflater, container, false)
        
        // Initialize repositories
        connectionRepository = ConnectionRepository(requireContext())
        preferenceManager = PreferenceManager(requireContext())
        
        // Set up the UI
        setupWiFiUI()
        setupBluetoothUI()
        setupConnectionButtons()
        
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * Set up WiFi connection UI
     */
    private fun setupWiFiUI() {
        // Load saved WiFi settings
        binding.editIpAddress.setText(preferenceManager.getWiFiIpAddress())
        binding.editPort.setText(preferenceManager.getWiFiPort().toString())
        
        // WiFi connect button
        binding.btnConnectWifi.setOnClickListener {
            connectWiFi()
        }
    }
    
    /**
     * Set up Bluetooth connection UI
     */
    private fun setupBluetoothUI() {
        // Set up Bluetooth device list
        bluetoothDeviceAdapter = BluetoothDeviceAdapter(emptyList()) { device ->
            connectBluetooth(device)
        }
        
        binding.recyclerBluetoothDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bluetoothDeviceAdapter
        }
        
        // Set up scan button
        binding.btnScanBluetooth.setOnClickListener {
            scanBluetoothDevices()
        }
        
        // Load paired devices initially
        loadPairedBluetoothDevices()
        
        // Set up Bluetooth fallback checkbox
        binding.checkboxBluetoothFallback.isChecked = preferenceManager.getBluetoothFallback()
        binding.checkboxBluetoothFallback.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setBluetoothFallback(isChecked)
        }
    }
    
    /**
     * Set up general connection buttons
     */
    private fun setupConnectionButtons() {
        // Disconnect button
        binding.btnDisconnect.setOnClickListener {
            disconnectAll()
        }
        
        // Test connection button
        binding.btnTestConnection.setOnClickListener {
            testConnection()
        }
    }
    
    /**
     * Connect to the camera server via WiFi
     */
    private fun connectWiFi() {
        val ipAddress = binding.editIpAddress.text.toString()
        val portStr = binding.editPort.text.toString()
        
        if (ipAddress.isEmpty() || portStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter IP address and port", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val port = portStr.toInt()
            
            // Show progress
            binding.progressWifi.visibility = View.VISIBLE
            binding.btnConnectWifi.isEnabled = false
            
            // Connect in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val connected = connectionRepository.connectWiFi(ipAddress, port)
                    
                    withContext(Dispatchers.Main) {
                        binding.progressWifi.visibility = View.GONE
                        binding.btnConnectWifi.isEnabled = true
                        
                        if (connected) {
                            Toast.makeText(requireContext(), "Connected to WiFi server", Toast.LENGTH_SHORT).show()
                            updateConnectionStatus()
                        } else {
                            Toast.makeText(requireContext(), "Failed to connect to WiFi server", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ConnectionFragment", "Error connecting to WiFi", e)
                    withContext(Dispatchers.Main) {
                        binding.progressWifi.visibility = View.GONE
                        binding.btnConnectWifi.isEnabled = true
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid port number", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Load paired Bluetooth devices
     */
    private fun loadPairedBluetoothDevices() {
        val pairedDevices = connectionRepository.getPairedBluetoothDevices()
        bluetoothDeviceAdapter.updateDevices(pairedDevices)
        
        binding.tvBluetoothStatus.text = if (pairedDevices.isEmpty()) {
            "No paired devices found"
        } else {
            "Found ${pairedDevices.size} paired devices"
        }
    }
    
    /**
     * Scan for Bluetooth devices
     */
    private fun scanBluetoothDevices() {
        // Show progress
        binding.progressBluetooth.visibility = View.VISIBLE
        binding.btnScanBluetooth.isEnabled = false
        binding.tvBluetoothStatus.text = "Scanning for devices..."
        
        // Scan in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val devices = connectionRepository.scanBluetoothDevices()
                
                withContext(Dispatchers.Main) {
                    binding.progressBluetooth.visibility = View.GONE
                    binding.btnScanBluetooth.isEnabled = true
                    
                    bluetoothDeviceAdapter.updateDevices(devices)
                    
                    binding.tvBluetoothStatus.text = if (devices.isEmpty()) {
                        "No devices found"
                    } else {
                        "Found ${devices.size} devices"
                    }
                }
            } catch (e: Exception) {
                Log.e("ConnectionFragment", "Error scanning for Bluetooth devices", e)
                withContext(Dispatchers.Main) {
                    binding.progressBluetooth.visibility = View.GONE
                    binding.btnScanBluetooth.isEnabled = true
                    binding.tvBluetoothStatus.text = "Error scanning: ${e.message}"
                }
            }
        }
    }
    
    /**
     * Connect to a Bluetooth device
     */
    private fun connectBluetooth(device: BluetoothDevice) {
        // Show progress
        binding.progressBluetooth.visibility = View.VISIBLE
        binding.tvBluetoothStatus.text = "Connecting to ${device.name}..."
        
        // Connect in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connected = connectionRepository.connectBluetooth(device)
                
                withContext(Dispatchers.Main) {
                    binding.progressBluetooth.visibility = View.GONE
                    
                    if (connected) {
                        binding.tvBluetoothStatus.text = "Connected to ${device.name}"
                        Toast.makeText(requireContext(), "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
                        updateConnectionStatus()
                    } else {
                        binding.tvBluetoothStatus.text = "Failed to connect to ${device.name}"
                        Toast.makeText(requireContext(), "Failed to connect to ${device.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ConnectionFragment", "Error connecting to Bluetooth device", e)
                withContext(Dispatchers.Main) {
                    binding.progressBluetooth.visibility = View.GONE
                    binding.tvBluetoothStatus.text = "Error connecting: ${e.message}"
                }
            }
        }
    }
    
    /**
     * Disconnect from all connections
     */
    private fun disconnectAll() {
        connectionRepository.disconnectWiFi()
        connectionRepository.disconnectBluetooth()
        
        // Update UI
        binding.tvConnectionStatus.text = "Disconnected"
        binding.tvBluetoothStatus.text = "Disconnected"
        Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Test the current connection
     */
    private fun testConnection() {
        // Show progress
        binding.progressConnection.visibility = View.VISIBLE
        binding.btnTestConnection.isEnabled = false
        
        // Test in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connected = connectionRepository.isConnected()
                
                withContext(Dispatchers.Main) {
                    binding.progressConnection.visibility = View.GONE
                    binding.btnTestConnection.isEnabled = true
                    
                    if (connected) {
                        binding.tvConnectionStatus.text = "Connected"
                        Toast.makeText(requireContext(), "Connection OK", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.tvConnectionStatus.text = "Not connected"
                        Toast.makeText(requireContext(), "Not connected", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ConnectionFragment", "Error testing connection", e)
                withContext(Dispatchers.Main) {
                    binding.progressConnection.visibility = View.GONE
                    binding.btnTestConnection.isEnabled = true
                    binding.tvConnectionStatus.text = "Error: ${e.message}"
                }
            }
        }
    }
    
    /**
     * Update the connection status display
     */
    private fun updateConnectionStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connected = connectionRepository.isConnected()
                
                withContext(Dispatchers.Main) {
                    binding.tvConnectionStatus.text = if (connected) "Connected" else "Not connected"
                }
            } catch (e: Exception) {
                Log.e("ConnectionFragment", "Error updating connection status", e)
            }
        }
    }
    
    companion object {
        /**
         * Create a new instance of the fragment
         */
        fun newInstance(): ConnectionFragment {
            return ConnectionFragment()
        }
    }
}