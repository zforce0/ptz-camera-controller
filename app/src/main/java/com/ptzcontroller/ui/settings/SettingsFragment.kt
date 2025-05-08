package com.ptzcontroller.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.ptzcontroller.R
import com.ptzcontroller.databinding.FragmentSettingsBinding
import com.ptzcontroller.ui.settings.SettingsFragment
import com.ptzcontroller.utils.PreferenceManager
import com.ptzcontroller.ui.viewmodels.VideoStreamViewModel
import com.ptzcontroller.ui.control.CameraControlFragment

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var seekbarControlSensitivity: SeekBar
    private lateinit var textControlSensitivityValue: TextView
    private lateinit var seekbarZoomSensitivity: SeekBar
    private lateinit var textZoomSensitivityValue: TextView
    private lateinit var radioGroupStreamQuality: RadioGroup
    private lateinit var radioLowQuality: RadioButton
    private lateinit var radioMediumQuality: RadioButton
    private lateinit var radioHighQuality: RadioButton
    private lateinit var switchDarkMode: Switch
    private lateinit var switchKeepScreenOn: Switch
    private lateinit var switchHideControls: Switch
    private lateinit var textAppVersion: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        preferenceManager = PreferenceManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seekbarControlSensitivity = binding.seekbarControlSensitivity
        textControlSensitivityValue = binding.textControlSensitivityValue
        seekbarZoomSensitivity = binding.seekbarZoomSensitivity
        textZoomSensitivityValue = binding.textZoomSensitivityValue
        radioGroupStreamQuality = binding.radioGroupStreamQuality
        radioLowQuality = binding.radioLowQuality
        radioMediumQuality = binding.radioMediumQuality
        radioHighQuality = binding.radioHighQuality
        switchDarkMode = binding.switchDarkMode
        switchKeepScreenOn = binding.switchKeepScreenOn
        switchHideControls = binding.switchHideControls
        textAppVersion = binding.textAppVersion

        setupControlSensitivity()
        setupZoomSensitivity()
        setupStreamQuality()
        setupDarkMode()
        setupKeepScreenOn()
        setupHideControls()
        setupAppVersion()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupControlSensitivity() {
        seekbarControlSensitivity.progress = preferenceManager.getControlSensitivity()
        textControlSensitivityValue.text = "${preferenceManager.getControlSensitivity()}%"

        seekbarControlSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textControlSensitivityValue.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    preferenceManager.setControlSensitivity(it.progress)
                }
            }
        })
    }

    private fun setupZoomSensitivity() {
        seekbarZoomSensitivity.progress = preferenceManager.getZoomSensitivity()
        textZoomSensitivityValue.text = "${preferenceManager.getZoomSensitivity()}%"

        seekbarZoomSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textZoomSensitivityValue.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    preferenceManager.setZoomSensitivity(it.progress)
                }
            }
        })
    }

    private fun setupStreamQuality() {
        val qualityValue = preferenceManager.getStreamQuality()
        radioGroupStreamQuality.check(
            when (qualityValue) {
                0 -> R.id.radioLowQuality
                1 -> R.id.radioMediumQuality
                2 -> R.id.radioHighQuality
                else -> R.id.radioMediumQuality
            }
        )

        radioGroupStreamQuality.setOnCheckedChangeListener { _, checkedId ->
            val quality = when (checkedId) {
                R.id.radioLowQuality -> 0
                R.id.radioMediumQuality -> 1
                R.id.radioHighQuality -> 2
                else -> 1
            }
            preferenceManager.setStreamQuality(quality)
        }
    }

    private fun setupDarkMode() {
        binding.switchDarkMode.isChecked = preferenceManager.getDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setDarkMode(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setupKeepScreenOn() {
        switchKeepScreenOn.isChecked = preferenceManager.getKeepScreenOn()
        switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setKeepScreenOn(isChecked)
            if (isChecked) {
                requireActivity().window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                requireActivity().window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    private fun setupHideControls() {
        switchHideControls.isChecked = preferenceManager.getHideControls()
        switchHideControls.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setHideControls(isChecked)
        }
    }


    private fun setupAppVersion() {
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        textAppVersion.text = "App Version: ${packageInfo.versionName} (${packageInfo.versionCode})"
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}