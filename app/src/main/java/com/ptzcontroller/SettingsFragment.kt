package com.ptzcontroller

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ptzcontroller.BuildConfig
import com.ptzcontroller.R
import com.ptzcontroller.databinding.FragmentSettingsBinding
import com.ptzcontroller.utils.PreferenceManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: SettingsViewModel
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        
        preferenceManager = PreferenceManager(requireContext())
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up stream quality spinner
        setupStreamQualitySpinner()
        
        // Set up connection check interval spinner
        setupConnectionCheckIntervalSpinner()
        
        // Set up control sensitivity spinner
        setupControlSensitivitySpinner()
        
        // Set up switch listeners
        setupSwitchListeners()
        
        // Set up about button
        setupAboutButton()
    }
    
    private fun setupStreamQualitySpinner() {
        val qualities = resources.getStringArray(R.array.stream_quality_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, qualities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.streamQualitySpinner.adapter = adapter
        
        // Set initial selection based on saved preference
        binding.streamQualitySpinner.setSelection(preferenceManager.getStreamQuality())
        
        // Set listener
        binding.streamQualitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                preferenceManager.setStreamQuality(position)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
    
    private fun setupConnectionCheckIntervalSpinner() {
        val intervals = resources.getStringArray(R.array.connection_check_interval_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.connectionCheckIntervalSpinner.adapter = adapter
        
        // Set initial selection based on saved preference
        binding.connectionCheckIntervalSpinner.setSelection(preferenceManager.getConnectionCheckInterval())
        
        // Set listener
        binding.connectionCheckIntervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                preferenceManager.setConnectionCheckInterval(position)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
    
    private fun setupControlSensitivitySpinner() {
        val sensitivities = resources.getStringArray(R.array.control_sensitivity_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sensitivities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.controlSensitivitySpinner.adapter = adapter
        
        // Set initial selection based on saved preference
        binding.controlSensitivitySpinner.setSelection(preferenceManager.getControlSensitivity())
        
        // Set listener
        binding.controlSensitivitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                preferenceManager.setControlSensitivity(position)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
    
    private fun setupSwitchListeners() {
        // Status overlay switch
        binding.showStatusOverlaySwitch.isChecked = preferenceManager.getShowStatusOverlay()
        binding.showStatusOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setShowStatusOverlay(isChecked)
        }
        
        // Keep screen on switch
        binding.keepScreenOnSwitch.isChecked = preferenceManager.getKeepScreenOn()
        binding.keepScreenOnSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setKeepScreenOn(isChecked)
            // Update activity
            requireActivity().window?.let { window ->
                if (isChecked) {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
        
        // Auto reconnect switch
        binding.autoReconnectSwitch.isChecked = preferenceManager.getAutoReconnect()
        binding.autoReconnectSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setAutoReconnect(isChecked)
        }
        
        // Bluetooth fallback switch
        binding.bluetoothFallbackSwitch.isChecked = preferenceManager.getBluetoothFallback()
        binding.bluetoothFallbackSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setBluetoothFallback(isChecked)
        }
        
        // Dark mode switch
        binding.darkModeSwitch.isChecked = preferenceManager.getDarkMode()
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setDarkMode(isChecked)
            // Set dark mode
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        
        // WiFi timeout
        binding.wifiTimeoutInput.setText(preferenceManager.getWiFiTimeout().toString())
        binding.wifiTimeoutInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = binding.wifiTimeoutInput.text.toString().toIntOrNull() ?: 10
                preferenceManager.setWiFiTimeout(value)
            }
        }
    }
    
    private fun setupAboutButton() {
        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }
    }
    
    private fun showAboutDialog() {
        val aboutDialogBuilder = android.app.AlertDialog.Builder(requireContext())
        aboutDialogBuilder.setTitle("About PTZ Camera Controller")
        
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        
        val message = """
            Version: $versionName ($versionCode)
            
            PTZ Camera Controller is an advanced application for controlling Pan-Tilt-Zoom cameras with multiple connectivity options.
            
            The app supports both WiFi and Bluetooth connectivity, and provides video streaming via RTSP with real-time control of pan, tilt, and zoom functions.
            
            © 2025 PTZ Camera Controller
        """.trimIndent()
        
        aboutDialogBuilder.setMessage(message)
        aboutDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        
        aboutDialogBuilder.setNeutralButton("Project Website") { dialog, _ ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/example/ptz-camera-controller"))
            startActivity(intent)
            dialog.dismiss()
        }
        
        aboutDialogBuilder.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}