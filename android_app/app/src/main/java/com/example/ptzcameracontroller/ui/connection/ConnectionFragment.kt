package com.example.ptzcameracontroller.ui.connection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ptzcameracontroller.R
import com.example.ptzcameracontroller.databinding.FragmentConnectionBinding
import com.example.ptzcameracontroller.utils.PreferenceManager

class ConnectionFragment : Fragment() {

    private var _binding: FragmentConnectionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ConnectionViewModel
    private lateinit var preferenceManager: PreferenceManager
    
    // Bluetooth adapter
    private var bluetoothAdapter: BluetoothAdapter? = null
    
    // Bluetooth device adapter for RecyclerView
    private lateinit var bluetoothDeviceAdapter: BluetoothDeviceAdapter
    
    // Permissions
    private val requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            setupBluetooth()
        } else {
            Toast.makeText(requireContext(), "Bluetooth permissions denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Bluetooth intent launcher
    private val bluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Bluetooth enabled", Toast.LENGTH_SHORT).show()
            setupBluetooth()
        } else {
            Toast.makeText(requireContext(), "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
        }
    }
    
    // QR scanner launcher
    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // Process QR scan result
            result.data?.getStringExtra("SCAN_RESULT")?.let { qrData ->
                handleQrScanResult(qrData)
            }
        }
    }
    
    // Bluetooth device discovery receiver
    private val deviceDiscoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Get discovered device
                    val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    
                    // Add device to list if not already present
                    device?.let {
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                            val deviceName = it.name ?: "Unknown Device"
                            val deviceAddress = it.address
                            
                            val deviceInfo = BluetoothDeviceInfo(deviceName, deviceAddress, it)
                            viewModel.addBluetoothDevice(deviceInfo)
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // Discovery finished
                    binding.btnScanDevices.isEnabled = true
                    binding.btnScanDevices.text = getString(R.string.scan_devices)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ConnectionViewModel::class.java)
        _binding = FragmentConnectionBinding.inflate(inflater, container, false)
        
        preferenceManager = PreferenceManager(requireContext())
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize Bluetooth
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        
        // Initialize Bluetooth device adapter
        bluetoothDeviceAdapter = BluetoothDeviceAdapter { device ->
            connectToBluetoothDevice(device)
        }
        
        // Set up RecyclerView
        binding.bluetoothDevicesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bluetoothDeviceAdapter
        }
        
        // Load saved WiFi settings
        binding.ipAddressInput.setText(preferenceManager.getLastWiFiIP())
        binding.portInput.setText(preferenceManager.getLastWiFiPort().toString())
        
        // Set up button listeners
        setupButtonListeners()
        
        // Set up observers
        setupObservers()
        
        // Check and request permissions for Bluetooth
        checkAndRequestPermissions()
    }
    
    private fun setupButtonListeners() {
        // WiFi connect button
        binding.btnWifiConnect.setOnClickListener {
            val ip = binding.ipAddressInput.text.toString()
            val portStr = binding.portInput.text.toString()
            
            if (ip.isNotEmpty() && portStr.isNotEmpty()) {
                try {
                    val port = portStr.toInt()
                    viewModel.connectToWiFi(ip, port)
                    
                    // Save to preferences
                    preferenceManager.setLastWiFiConnection(ip, port)
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Invalid port number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter IP address and port", Toast.LENGTH_SHORT).show()
            }
        }
        
        // WiFi disconnect button
        binding.btnWifiDisconnect.setOnClickListener {
            viewModel.disconnectFromWiFi()
        }
        
        // Bluetooth scan button
        binding.btnScanDevices.setOnClickListener {
            startBluetoothDiscovery()
        }
        
        // Bluetooth disconnect button
        binding.btnBtDisconnect.setOnClickListener {
            viewModel.disconnectFromBluetooth()
        }
        
        // QR scan button
        binding.btnScanQr.setOnClickListener {
            // Launch QR scanner
            // This would normally use an Intent to start the QR scanner activity
            // For now, simulate with a toast message
            Toast.makeText(requireContext(), "QR scanner would launch here", Toast.LENGTH_SHORT).show()
            
            /* Example of how to launch real QR scanner:
            try {
                val intent = Intent("com.google.zxing.client.android.SCAN")
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
                qrScannerLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "QR scanner app not found", Toast.LENGTH_SHORT).show()
            }
            */
        }
    }
    
    private fun setupObservers() {
        // Observe WiFi connection status
        viewModel.wifiConnectionStatus.observe(viewLifecycleOwner) { status ->
            binding.wifiStatus.text = status.description
            binding.wifiStatus.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    when (status) {
                        ConnectionStatus.CONNECTED -> R.color.status_good
                        ConnectionStatus.CONNECTING -> R.color.status_warning
                        ConnectionStatus.DISCONNECTED -> R.color.status_idle
                        ConnectionStatus.ERROR -> R.color.status_error
                    }
                )
            )
            
            // Enable/disable buttons based on status
            binding.btnWifiConnect.isEnabled = status != ConnectionStatus.CONNECTING && status != ConnectionStatus.CONNECTED
            binding.btnWifiDisconnect.isEnabled = status == ConnectionStatus.CONNECTED
        }
        
        // Observe Bluetooth connection status
        viewModel.bluetoothConnectionStatus.observe(viewLifecycleOwner) { status ->
            binding.bluetoothStatus.text = status.description
            binding.bluetoothStatus.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    when (status) {
                        ConnectionStatus.CONNECTED -> R.color.status_good
                        ConnectionStatus.CONNECTING -> R.color.status_warning
                        ConnectionStatus.DISCONNECTED -> R.color.status_idle
                        ConnectionStatus.ERROR -> R.color.status_error
                    }
                )
            )
            
            // Enable/disable buttons based on status
            binding.btnScanDevices.isEnabled = status != ConnectionStatus.CONNECTING && status != ConnectionStatus.CONNECTED
            binding.btnBtDisconnect.isEnabled = status == ConnectionStatus.CONNECTED
        }
        
        // Observe Bluetooth devices
        viewModel.bluetoothDevices.observe(viewLifecycleOwner) { devices ->
            bluetoothDeviceAdapter.submitList(devices)
        }
    }
    
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        } else {
            setupBluetooth()
        }
    }
    
    private fun setupBluetooth() {
        // Check if Bluetooth is supported
        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if Bluetooth is enabled
        if (bluetoothAdapter?.isEnabled == false) {
            // Request to enable Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothLauncher.launch(enableBtIntent)
        } else {
            // Bluetooth is enabled, proceed
            loadPairedDevices()
        }
    }
    
    private fun loadPairedDevices() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        // Get paired devices
        val pairedDevices = bluetoothAdapter?.bondedDevices ?: return
        
        // Add paired devices to the view model
        viewModel.clearBluetoothDevices()
        for (device in pairedDevices) {
            val deviceName = device.name ?: "Unknown Device"
            val deviceAddress = device.address
            
            val deviceInfo = BluetoothDeviceInfo(deviceName, deviceAddress, device)
            viewModel.addBluetoothDevice(deviceInfo)
        }
    }
    
    private fun startBluetoothDiscovery() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        // Cancel any ongoing discovery
        bluetoothAdapter?.cancelDiscovery()
        
        // Clear existing devices and add paired devices again
        loadPairedDevices()
        
        // Start discovery
        bluetoothAdapter?.startDiscovery()
        
        // Update button
        binding.btnScanDevices.isEnabled = false
        binding.btnScanDevices.text = getString(R.string.scanning)
    }
    
    private fun connectToBluetoothDevice(deviceInfo: BluetoothDeviceInfo) {
        viewModel.connectToBluetooth(deviceInfo)
        
        // Save to preferences
        preferenceManager.setLastBTDevice(deviceInfo.address)
    }
    
    private fun handleQrScanResult(qrData: String) {
        try {
            // Parse QR data
            // Format: "ptz://connect?wifi=192.168.1.100:8000&bt=PTZ_Camera"
            if (qrData.startsWith("ptz://connect")) {
                // Extract WiFi info
                val wifiMatch = Regex("wifi=([^&]+)").find(qrData)
                wifiMatch?.groupValues?.get(1)?.split(":")?.let {
                    if (it.size == 2) {
                        val ip = it[0]
                        val port = it[1].toInt()
                        
                        // Update UI
                        binding.ipAddressInput.setText(ip)
                        binding.portInput.setText(port.toString())
                        
                        // Connect to WiFi
                        viewModel.connectToWiFi(ip, port)
                        
                        // Save to preferences
                        preferenceManager.setLastWiFiConnection(ip, port)
                    }
                }
                
                // Extract Bluetooth info
                val btMatch = Regex("bt=([^&]+)").find(qrData)
                btMatch?.groupValues?.get(1)?.let { btName ->
                    // Find device by name and connect
                    viewModel.bluetoothDevices.value?.find { it.name == btName }?.let {
                        connectToBluetoothDevice(it)
                    }
                }
                
                Toast.makeText(requireContext(), "Configuration applied from QR code", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Invalid QR code format", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error parsing QR code: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onStart() {
        super.onStart()
        
        // Register Bluetooth discovery receiver
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        requireContext().registerReceiver(deviceDiscoveryReceiver, filter)
    }
    
    override fun onStop() {
        super.onStop()
        
        // Unregister Bluetooth discovery receiver
        try {
            requireContext().unregisterReceiver(deviceDiscoveryReceiver)
        } catch (e: IllegalArgumentException) {
            // Ignore if not registered
        }
        
        // Cancel discovery
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.cancelDiscovery()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}