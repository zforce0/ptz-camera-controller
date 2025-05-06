
package com.ptzcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {
    
    private lateinit var videoQualitySpinner: Spinner
    private lateinit var controlSensitivitySeekBar: SeekBar
    private lateinit var defaultIpEditText: EditText
    private lateinit var defaultPortEditText: EditText
    private lateinit var saveButton: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        
        videoQualitySpinner = view.findViewById(R.id.spinner_video_quality)
        controlSensitivitySeekBar = view.findViewById(R.id.seekbar_control_sensitivity)
        defaultIpEditText = view.findViewById(R.id.edit_default_ip)
        defaultPortEditText = view.findViewById(R.id.edit_default_port)
        saveButton = view.findViewById(R.id.btn_save_settings)
        
        setupVideoQualitySpinner()
        setupControlSensitivity()
        setupDefaultConnection()
        setupSaveButton()
        
        return view
    }
    
    private fun setupVideoQualitySpinner() {
        val qualities = arrayOf(
            getString(R.string.high_quality),
            getString(R.string.medium_quality),
            getString(R.string.low_quality)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, qualities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        videoQualitySpinner.adapter = adapter
    }
    
    private fun setupControlSensitivity() {
        controlSensitivitySeekBar.max = 100
        controlSensitivitySeekBar.progress = 50
    }
    
    private fun setupDefaultConnection() {
        defaultIpEditText.setText("192.168.1.100")
        defaultPortEditText.setText("8000")
    }
    
    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            // Save settings to SharedPreferences
            val prefs = requireContext().getSharedPreferences("PTZSettings", android.content.Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("defaultIp", defaultIpEditText.text.toString())
                putString("defaultPort", defaultPortEditText.text.toString())
                putInt("videoQuality", videoQualitySpinner.selectedItemPosition)
                putInt("controlSensitivity", controlSensitivitySeekBar.progress)
                apply()
            }
            
            Toast.makeText(context, getString(R.string.save_settings), Toast.LENGTH_SHORT).show()
        }
    }
}
