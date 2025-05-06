package com.ptzcontroller.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.ptzcontroller.R
import com.ptzcontroller.databinding.FragmentSettingsBinding
import com.ptzcontroller.utils.PreferenceManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        preferenceManager = PreferenceManager(requireContext())
        
        setupControlSettings()
        setupDisplaySettings()
        setupInformationSection()
        
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * Set up control sensitivity settings
     */
    private fun setupControlSettings() {
        // Control sensitivity
        binding.seekbarControlSensitivity.progress = preferenceManager.getControlSensitivity()
        binding.textControlSensitivityValue.text = "${preferenceManager.getControlSensitivity()}%"
        
        binding.seekbarControlSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textControlSensitivityValue.text = "$progress%"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    preferenceManager.setControlSensitivity(it.progress)
                }
            }
        })
        
        // Zoom sensitivity
        binding.seekbarZoomSensitivity.progress = preferenceManager.getZoomSensitivity()
        binding.textZoomSensitivityValue.text = "${preferenceManager.getZoomSensitivity()}%"
        
        binding.seekbarZoomSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textZoomSensitivityValue.text = "$progress%"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    preferenceManager.setZoomSensitivity(it.progress)
                }
            }
        })
        
        // Stream quality
        val qualityValue = preferenceManager.getStreamQuality()
        binding.radioGroupStreamQuality.check(
            when (qualityValue) {
                0 -> R.id.radioLowQuality
                1 -> R.id.radioMediumQuality
                2 -> R.id.radioHighQuality
                else -> R.id.radioMediumQuality
            }
        )
        
        binding.radioGroupStreamQuality.setOnCheckedChangeListener { _, checkedId ->
            val quality = when (checkedId) {
                R.id.radioLowQuality -> 0
                R.id.radioMediumQuality -> 1
                R.id.radioHighQuality -> 2
                else -> 1
            }
            preferenceManager.setStreamQuality(quality)
        }
    }
    
    /**
     * Set up display settings
     */
    private fun setupDisplaySettings() {
        // Dark mode
        binding.switchDarkMode.isChecked = preferenceManager.getDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setDarkMode(isChecked)
            
            // Apply immediately
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        
        // Keep screen on
        binding.switchKeepScreenOn.isChecked = preferenceManager.getKeepScreenOn()
        binding.switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setKeepScreenOn(isChecked)
            
            // Apply to activity
            if (isChecked) {
                requireActivity().window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                requireActivity().window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        
        // Hide controls
        binding.switchHideControls.isChecked = preferenceManager.getHideControls()
        binding.switchHideControls.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setHideControls(isChecked)
        }
    }
    
    /**
     * Set up version information
     */
    private fun setupInformationSection() {
        // App version
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        binding.textAppVersion.text = "App Version: ${packageInfo.versionName} (${packageInfo.versionCode})"
    }
    
    companion object {
        /**
         * Create a new instance of the fragment
         */
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}