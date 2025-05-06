package com.ptzcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ptzcontroller.databinding.FragmentConnectionBinding
import com.ptzcontroller.utils.PreferenceManager

class ConnectionFragment : Fragment() {

    private var _binding: FragmentConnectionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ConnectionViewModel
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionBinding.inflate(inflater, container, false)
        
        // Initialize preference manager
        preferenceManager = PreferenceManager(requireContext())
        
        // Initialize ViewModel
        val connectionRepository = ConnectionRepository(requireContext())
        val factory = ConnectionViewModelFactory(connectionRepository)
        viewModel = ViewModelProvider(this, factory)[ConnectionViewModel::class.java]
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up WiFi connection section
        setupWiFiSection()
        
        // Set up Bluetooth connection section
        setupBluetoothSection()
        
        // Set up QR code scanning
        setupQRCodeScanning()
        
        // Observe ViewModel
        observeViewModel()
    }
    
    private fun setupWiFiSection() {
        // Get saved WiFi preferences
        val savedIpAddress = preferenceManager.getWiFiIpAddress()
        val savedPort = preferenceManager.getWiFiPort()
        
        // Set IP address and port input fields
        binding.ipAddressInput.setText(savedIpAddress)
        binding.portInput.setText(savedPort.toString())
        
        // WiFi connect button
        binding.btnWifiConnect.setOnClickListener {
            val ipAddress = binding.ipAddressInput.text.toString()
            val port = binding.portInput.text.toString().toIntOrNull() ?: 0
            
            if (ipAddress.isNotEmpty() && port > 0) {
                // Save to preferences
                preferenceManager.setWiFiIpAddress(ipAddress)
                preferenceManager.setWiFiPort(port)
                
                // Connect
                viewModel.connectWiFi(ipAddress, port)
            } else {
                // Show error
                if (ipAddress.isEmpty()) {
                    binding.ipAddressInput.error = "Enter a valid IP address"
                }
                if (port <= 0) {
                    binding.portInput.error = "Enter a valid port number"
                }
            }
        }
        
        // WiFi disconnect button
        binding.btnWifiDisconnect.setOnClickListener {
            viewModel.disconnectWiFi()
        }
    }
    
    private fun setupBluetoothSection() {
        // Set up Bluetooth device recycler view
        val bluetoothDeviceAdapter = BluetoothDeviceAdapter { device ->
            // Connect to selected device
            viewModel.connectBluetooth(device)
        }
        
        binding.recyclerBtDevices.adapter = bluetoothDeviceAdapter
        
        // Scan button
        binding.btnScanDevices.setOnClickListener {
            startBluetoothScan()
        }
        
        // Disconnect button
        binding.btnBtDisconnect.setOnClickListener {
            viewModel.disconnectBluetooth()
        }
        
        // Update adapter when device list changes
        viewModel.pairedDevices.observe(viewLifecycleOwner) { devices ->
            bluetoothDeviceAdapter.submitList(devices)
            binding.recyclerBtDevices.visibility = if (devices.isEmpty()) View.GONE else View.VISIBLE
            binding.textNoDevices.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
        }
    }
    
    private fun setupQRCodeScanning() {
        binding.btnScanQrCode.setOnClickListener {
            // Start QR code scanner
            viewModel.startQRCodeScanner(requireActivity())
        }
    }
    
    private fun startBluetoothScan() {
        // Show scanning indicator
        binding.btScanIndicator.visibility = View.VISIBLE
        binding.textScanning.visibility = View.VISIBLE
        binding.btnScanDevices.isEnabled = false
        
        // Start scan
        viewModel.scanBluetoothDevices()
    }
    
    private fun observeViewModel() {
        // WiFi connection status
        viewModel.wifiStatus.observe(viewLifecycleOwner) { status ->
            binding.wifiStatusText.text = status
            
            // Update UI based on connection status
            val isConnected = status == getString(R.string.wifi_status_connected)
            binding.btnWifiConnect.isEnabled = !isConnected
            binding.btnWifiDisconnect.isEnabled = isConnected
            binding.ipAddressInput.isEnabled = !isConnected
            binding.portInput.isEnabled = !isConnected
        }
        
        // Bluetooth connection status
        viewModel.bluetoothStatus.observe(viewLifecycleOwner) { status ->
            binding.btStatusText.text = status
            
            // Update UI based on connection status
            val isConnected = status == getString(R.string.bt_status_connected)
            binding.btnScanDevices.isEnabled = !isConnected
            binding.btnBtDisconnect.isEnabled = isConnected
            binding.recyclerBtDevices.isEnabled = !isConnected
        }
        
        // Bluetooth scanning status
        viewModel.isScanning.observe(viewLifecycleOwner) { scanning ->
            if (!scanning) {
                binding.btScanIndicator.visibility = View.GONE
                binding.textScanning.visibility = View.GONE
                binding.btnScanDevices.isEnabled = true
            }
        }
        
        // Connection errors
        viewModel.connectionError.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
        
        // QR code scan result
        viewModel.qrScanResult.observe(viewLifecycleOwner) { result ->
            if (result.isNotEmpty()) {
                Toast.makeText(context, "QR code scanned: $result", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check connection status
        viewModel.checkConnectionStatus()
        
        // Load paired devices
        viewModel.loadPairedDevices()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}