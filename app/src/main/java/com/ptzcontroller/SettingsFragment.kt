package com.ptzcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var connectionManager: ConnectionManager
    
    // UI components
    private lateinit var editDefaultIp: EditText
    private lateinit var editDefaultPort: EditText
    private lateinit var spinnerConnectionType: Spinner
    private lateinit var spinnerVideoQuality: Spinner
    private lateinit var btnSaveSettings: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            connectionManager = it.getSerializable("connectionManager") as ConnectionManager
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        
        // Initialize UI components
        editDefaultIp = view.findViewById(R.id.edit_default_ip)
        editDefaultPort = view.findViewById(R.id.edit_default_port)
        spinnerConnectionType = view.findViewById(R.id.spinner_connection_type)
        spinnerVideoQuality = view.findViewById(R.id.spinner_video_quality)
        btnSaveSettings = view.findViewById(R.id.btn_save_settings)
        
        // Set up connection type spinner
        val connectionTypes = arrayOf("WiFi", "Bluetooth")
        val connectionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, connectionTypes)
        connectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerConnectionType.adapter = connectionAdapter
        
        // Set up video quality spinner
        val videoQualities = arrayOf("Low", "Medium", "High")
        val qualityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, videoQualities)
        qualityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerVideoQuality.adapter = qualityAdapter
        
        // Load saved settings
        loadSettings()
        
        // Set up save button
        btnSaveSettings.setOnClickListener {
            saveSettings()
        }
        
        return view
    }
    
    private fun loadSettings() {
        val sharedPrefs = requireActivity().getSharedPreferences("PTZControllerPrefs", 0)
        
        editDefaultIp.setText(sharedPrefs.getString("default_ip", "192.168.1.100"))
        editDefaultPort.setText(sharedPrefs.getString("default_port", "8000"))
        
        val defaultConnectionType = sharedPrefs.getInt("connection_type", 0) // 0 for WiFi, 1 for Bluetooth
        spinnerConnectionType.setSelection(defaultConnectionType)
        
        val defaultVideoQuality = sharedPrefs.getInt("video_quality", 1) // 0 for Low, 1 for Medium, 2 for High
        spinnerVideoQuality.setSelection(defaultVideoQuality)
    }
    
    private fun saveSettings() {
        val sharedPrefs = requireActivity().getSharedPreferences("PTZControllerPrefs", 0)
        val editor = sharedPrefs.edit()
        
        editor.putString("default_ip", editDefaultIp.text.toString())
        editor.putString("default_port", editDefaultPort.text.toString())
        editor.putInt("connection_type", spinnerConnectionType.selectedItemPosition)
        editor.putInt("video_quality", spinnerVideoQuality.selectedItemPosition)
        
        editor.apply()
        
        Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
    }
}
