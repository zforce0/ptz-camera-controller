package com.example.ptzcameracontroller.ui.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ptzcameracontroller.R
import com.example.ptzcameracontroller.data.repository.ConnectionRepository
import com.example.ptzcameracontroller.databinding.FragmentConnectionBinding
import com.example.ptzcameracontroller.utils.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnectionFragment : Fragment() {

    private var _binding: FragmentConnectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ConnectionViewModel
    private lateinit var bluetoothDeviceAdapter: BluetoothDeviceAdapter
    private lateinit var preferenceManager: PreferenceManager
    
    // Bluetooth components
    private val bluetoothManager by lazy {
        requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager.adapter
    }
    
    // Activity result launcher for enabling Bluetooth
    private val requestBluetoothEnable = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // Bluetooth was enabled, scan for devices
            scanForBluetoothDevices()
        } else {
            Toast.makeText(requireContext(), "Bluetooth is required for device connection", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Activity result launcher for scanning QR code
    private val scanQRCode = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle QR code scanning result
        // This would process the result from a barcode scanner activity
        // For now, we'll use manual entry
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionBinding.inflate(inflater, container, false)
        preferenceManager = PreferenceManager(requireContext())
        
        // Set up ViewModel with repository
        val connectionRepository = ConnectionRepository(requireContext())
        val factory = ConnectionViewModelFactory(connectionRepository)
        
        viewModel = ViewModelProvider(this, factory)[ConnectionViewModel::class.java]
        
        // Set up Bluetooth devices RecyclerView
        setupBluetoothRecyclerView()
        
        // Set up UI elements
        setupUIElements()
        
        // Observe LiveData from ViewModel
        observeViewModel()
        
        // Load saved connection information
        loadSavedConnectionInfo()
        
        return binding.root
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh connection status
        refreshConnectionStatus()
    }
    
    private fun setupBluetoothRecyclerView() {
        bluetoothDeviceAdapter = BluetoothDeviceAdapter { deviceInfo ->
            // Device clicked, connect to it
            connectToBluetooth(deviceInfo)
        }
        
        binding.rvBluetoothDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bluetoothDeviceAdapter
        }
    }
    
    private fun setupUIElements() {
        // WiFi connection button
        binding.btnConnectWifi.setOnClickListener {
            connectToWiFi()
        }
        
        // WiFi disconnect button
        binding.btnDisconnectWifi.setOnClickListener {
            disconnectFromWiFi()
        }
        
        // Bluetooth scan button
        binding.btnScanDevices.setOnClickListener {
            scanForBluetoothDevices()
        }
        
        // Bluetooth disconnect button
        binding.btnDisconnectBluetooth.setOnClickListener {
            disconnectFromBluetooth()
        }
        
        // QR code scan button
        binding.btnScanQrCode.setOnClickListener {
            scanQRCode()
        }
        
        // Set initial field values from saved preferences
        binding.etIpAddress.setText(preferenceManager.getLastIpAddress())
        binding.etPort.setText(preferenceManager.getLastPort().toString())
    }
    
    private fun observeViewModel() {
        // Observe WiFi connection status
        viewModel.wifiConnectionStatus.observe(viewLifecycleOwner) { status ->
            updateWiFiConnectionUI(status)
        }
        
        // Observe Bluetooth connection status
        viewModel.bluetoothConnectionStatus.observe(viewLifecycleOwner) { status ->
            updateBluetoothConnectionUI(status)
        }
        
        // Observe Bluetooth devices list
        viewModel.bluetoothDevices.observe(viewLifecycleOwner) { devices ->
            bluetoothDeviceAdapter.submitList(devices)
            binding.tvNoPairedDevices.isVisible = devices.isEmpty()
        }
    }
    
    private fun updateWiFiConnectionUI(status: ConnectionStatus) {
        binding.tvWifiStatus.text = status.description
        
        // Update UI based on status
        when (status) {
            ConnectionStatus.CONNECTED -> {
                binding.tvWifiStatus.setTextColor(requireContext().getColor(R.color.status_good))
                binding.btnConnectWifi.isEnabled = false
                binding.btnDisconnectWifi.isEnabled = true
                binding.etIpAddress.isEnabled = false
                binding.etPort.isEnabled = false
            }
            ConnectionStatus.CONNECTING -> {
                binding.tvWifiStatus.setTextColor(requireContext().getColor(R.color.status_warning))
                binding.btnConnectWifi.isEnabled = false
                binding.btnDisconnectWifi.isEnabled = false
                binding.etIpAddress.isEnabled = false
                binding.etPort.isEnabled = false
            }
            ConnectionStatus.DISCONNECTED -> {
                binding.tvWifiStatus.setTextColor(requireContext().getColor(R.color.status_idle))
                binding.btnConnectWifi.isEnabled = true
                binding.btnDisconnectWifi.isEnabled = false
                binding.etIpAddress.isEnabled = true
                binding.etPort.isEnabled = true
            }
            ConnectionStatus.ERROR -> {
                binding.tvWifiStatus.setTextColor(requireContext().getColor(R.color.status_error))
                binding.btnConnectWifi.isEnabled = true
                binding.btnDisconnectWifi.isEnabled = false
                binding.etIpAddress.isEnabled = true
                binding.etPort.isEnabled = true
                
                Snackbar.make(binding.root, "Failed to connect to WiFi server", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateBluetoothConnectionUI(status: ConnectionStatus) {
        binding.tvBluetoothStatus.text = status.description
        
        // Update UI based on status
        when (status) {
            ConnectionStatus.CONNECTED -> {
                binding.tvBluetoothStatus.setTextColor(requireContext().getColor(R.color.status_good))
                binding.rvBluetoothDevices.isEnabled = false
                binding.btnScanDevices.isEnabled = false
                binding.btnDisconnectBluetooth.isEnabled = true
            }
            ConnectionStatus.CONNECTING -> {
                binding.tvBluetoothStatus.setTextColor(requireContext().getColor(R.color.status_warning))
                binding.rvBluetoothDevices.isEnabled = false
                binding.btnScanDevices.isEnabled = false
                binding.btnDisconnectBluetooth.isEnabled = false
            }
            ConnectionStatus.DISCONNECTED -> {
                binding.tvBluetoothStatus.setTextColor(requireContext().getColor(R.color.status_idle))
                binding.rvBluetoothDevices.isEnabled = true
                binding.btnScanDevices.isEnabled = true
                binding.btnDisconnectBluetooth.isEnabled = false
            }
            ConnectionStatus.ERROR -> {
                binding.tvBluetoothStatus.setTextColor(requireContext().getColor(R.color.status_error))
                binding.rvBluetoothDevices.isEnabled = true
                binding.btnScanDevices.isEnabled = true
                binding.btnDisconnectBluetooth.isEnabled = false
                
                Snackbar.make(binding.root, "Failed to connect to Bluetooth device", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadSavedConnectionInfo() {
        // Load saved IP address and port
        val savedIpAddress = preferenceManager.getLastIpAddress()
        val savedPort = preferenceManager.getLastPort()
        
        if (savedIpAddress.isNotEmpty()) {
            binding.etIpAddress.setText(savedIpAddress)
            binding.etPort.setText(savedPort.toString())
        }
        
        // Load saved Bluetooth device
        val savedBluetoothDevice = preferenceManager.getLastBluetoothDevice()
        
        if (savedBluetoothDevice.isNotEmpty()) {
            // We would connect to this device automatically if auto-connect is enabled
            if (preferenceManager.getAutoReconnect()) {
                // This would happen in a real implementation
                // For now, just scan for devices
                scanForBluetoothDevices()
            }
        }
    }
    
    private fun connectToWiFi() {
        // Get IP address and port from UI
        val ipAddress = binding.etIpAddress.text.toString().trim()
        val portStr = binding.etPort.text.toString().trim()
        
        // Validate input
        if (ipAddress.isEmpty()) {
            binding.etIpAddress.error = "IP address is required"
            return
        }
        
        if (portStr.isEmpty()) {
            binding.etPort.error = "Port is required"
            return
        }
        
        val port = portStr.toIntOrNull() ?: 8000
        
        // Save connection info
        preferenceManager.setLastIpAddress(ipAddress)
        preferenceManager.setLastPort(port)
        
        // Connect via ViewModel
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.connectToWiFi(ipAddress, port)
        }
    }
    
    private fun disconnectFromWiFi() {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.disconnectFromWiFi()
        }
    }
    
    private fun scanForBluetoothDevices() {
        // Check if Bluetooth is available and enabled
        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "This device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!bluetoothAdapter.isEnabled) {
            // Request to enable Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetoothEnable.launch(enableBtIntent)
            return
        }
        
        // Show scanning indicator
        binding.progressScanningDevices.isVisible = true
        binding.tvBluetoothStatus.text = getString(R.string.scanning)
        
        // Scan for paired devices using coroutine
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val devices = viewModel.getPairedDevices()
                binding.progressScanningDevices.isVisible = false
                binding.tvBluetoothStatus.text = when {
                    devices.isEmpty() -> "No paired devices"
                    else -> "Found ${devices.size} device(s)"
                }
            } catch (e: Exception) {
                binding.progressScanningDevices.isVisible = false
                binding.tvBluetoothStatus.text = "Scan failed: ${e.message}"
                Log.e("ConnectionFragment", "Error scanning for devices", e)
            }
        }
    }
    
    private fun connectToBluetooth(deviceInfo: BluetoothDeviceInfo) {
        // Save selected device
        preferenceManager.setLastBluetoothDevice(deviceInfo.address)
        
        // Connect via ViewModel
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.connectToBluetooth(deviceInfo)
        }
    }
    
    private fun disconnectFromBluetooth() {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.disconnectFromBluetooth()
        }
    }
    
    private fun scanQRCode() {
        // In a real implementation, this would launch a QR code scanner
        // For now, show a dialog explaining that the feature is available
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("QR Code Scanner")
            .setMessage("This feature would launch a QR code scanner to easily scan connection information from the server. You can manually enter the connection information for now.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
        
        dialog.show()
    }
    
    private fun refreshConnectionStatus() {
        // Refresh connection status when returning to this fragment
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.refreshConnectionStatus()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}