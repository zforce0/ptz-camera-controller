package com.ptzcontroller.ui.connection

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ptzcontroller.R
import com.ptzcontroller.data.model.BluetoothDeviceInfo
import com.ptzcontroller.data.repository.ConnectionRepository
import com.ptzcontroller.databinding.FragmentConnectionBinding
import com.ptzcontroller.utils.PreferenceManager

/**
 * Fragment for connecting to a camera
 */
class ConnectionFragment : Fragment(), BluetoothDeviceInfoAdapter.OnBluetoothDeviceClickListener {

    private var _binding: FragmentConnectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ConnectionViewModel
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var pairedDevicesAdapter: BluetoothDeviceInfoAdapter
    private lateinit var discoveredDevicesAdapter: BluetoothDeviceInfoAdapter

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

        // Set up WiFi connection UI
        setupWifiConnection()

        // Set up Bluetooth connection UI
        setupBluetoothConnection()

        // Observe ViewModel
        observeViewModel()

        // Load initial data
        loadInitialData()
    }

    private fun setupWifiConnection() {
        // Load saved IP and port from preferences
        binding.ipAddressInput.setText(preferenceManager.getWiFiIpAddress())
        binding.portInput.setText(preferenceManager.getWiFiPort().toString())

        // Connect button click listener
        binding.btnWifiConnect.setOnClickListener {
            val ipAddress = binding.ipAddressInput.text.toString()
            val portText = binding.portInput.text.toString()

            if (ipAddress.isBlank()) {
                binding.ipAddressInput.error = "IP address is required"
                return@setOnClickListener
            }

            if (portText.isBlank()) {
                binding.portInput.error = "Port is required"
                return@setOnClickListener
            }

            val port = portText.toIntOrNull() ?: 8000
            viewModel.connectWifi(ipAddress, port)
        }
    }

    private fun setupBluetoothConnection() {
        // Initialize adapters
        pairedDevicesAdapter = BluetoothDeviceInfoAdapter(this)
        discoveredDevicesAdapter = BluetoothDeviceInfoAdapter(this)

        // Set up RecyclerViews
        binding.pairedDevicesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pairedDevicesAdapter
        }

        binding.detectedDevicesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = discoveredDevicesAdapter
        }

        // Enable Bluetooth button
        binding.btnEnableBluetooth.setOnClickListener {
            // Open Bluetooth settings to enable Bluetooth
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }

        // Scan for devices button
        binding.btnScanDevices.setOnClickListener {
            viewModel.scanForDevices()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeViewModel() {
        // Observe connection status
        viewModel.connectionStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                ConnectionStatus.CONNECTED -> {
                    // Navigate to camera control fragment
                    // Using Navigation Component:
                    // findNavController().navigate(R.id.action_connectionFragment_to_cameraControlFragment)
                }
                else -> {
                    // Stay on this screen
                }
            }
        }

        // Observe WiFi connecting state
        viewModel.wifiConnectingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WifiConnectingState.Connecting -> {
                    binding.btnWifiConnect.isEnabled = false
                    Toast.makeText(
                        context,
                        getString(R.string.msg_wifi_connecting, state.ipAddress, state.port),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is WifiConnectingState.Success -> {
                    binding.btnWifiConnect.isEnabled = true
                    Toast.makeText(
                        context,
                        getString(R.string.msg_wifi_connected, state.ipAddress, state.port),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is WifiConnectingState.Failed -> {
                    binding.btnWifiConnect.isEnabled = true
                    Toast.makeText(
                        context,
                        getString(R.string.msg_wifi_failed, state.ipAddress, state.port),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    binding.btnWifiConnect.isEnabled = true
                }
            }
        }

        // Observe Bluetooth enabled state
        viewModel.isBluetoothEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.btnEnableBluetooth.visibility = if (enabled) View.GONE else View.VISIBLE
            binding.btnScanDevices.isEnabled = enabled
            binding.tvNoBluetoothDevices.visibility =
                if (!enabled) View.VISIBLE else View.GONE
            binding.pairedDevicesList.visibility =
                if (enabled) View.VISIBLE else View.GONE
            binding.detectedDevicesList.visibility =
                if (enabled) View.VISIBLE else View.GONE
        }

        // Observe scanning state
        viewModel.isScanning.observe(viewLifecycleOwner) { scanning ->
            binding.scanningProgress.visibility = if (scanning) View.VISIBLE else View.GONE
            binding.btnScanDevices.isEnabled = !scanning
        }

        // Observe paired devices
        viewModel.pairedDevices.observe(viewLifecycleOwner) { devices ->
            pairedDevicesAdapter.updateDevices(devices)
            binding.tvPairedDevicesLabel.visibility =
                if (devices.isNotEmpty()) View.VISIBLE else View.GONE
        }

        // Observe discovered devices
        viewModel.discoveredDevices.observe(viewLifecycleOwner) { devices ->
            discoveredDevicesAdapter.updateDevices(devices)
            binding.tvDetectedDevicesLabel.visibility =
                if (devices.isNotEmpty()) View.VISIBLE else View.GONE
        }

        // Observe Bluetooth connecting state
        viewModel.bluetoothConnectingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BluetoothConnectingState.Connecting -> {
                    Toast.makeText(
                        context,
                        getString(R.string.msg_bluetooth_connecting, state.deviceInfo.name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is BluetoothConnectingState.Success -> {
                    Toast.makeText(
                        context,
                        getString(R.string.msg_bluetooth_connected, state.deviceInfo.name),
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Update device lists
                    viewModel.loadPairedDevices()
                }
                is BluetoothConnectingState.Failed -> {
                    Toast.makeText(
                        context,
                        getString(R.string.msg_bluetooth_failed, state.deviceInfo.name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    // Do nothing
                }
            }
        }
    }

    private fun loadInitialData() {
        // Check Bluetooth state
        viewModel.checkBluetoothEnabled()

        // Load paired devices if Bluetooth is enabled
        viewModel.loadPairedDevices()
    }

    override fun onDeviceClick(device: BluetoothDeviceInfo) {
        // Connect to selected device
        viewModel.connectBluetooth(device)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}