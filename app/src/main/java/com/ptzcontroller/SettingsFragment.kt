package com.ptzcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.Toast

/**
 * Settings Fragment for the PTZ Camera Controller App
 * This provides app settings and information
 */
class SettingsFragment : Fragment() {

    private lateinit var versionTextView: TextView
    private lateinit var scanQrButton: Button
    private lateinit var connectionManager: ConnectionManager
    
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
        
        // Find views
//        versionTextView = view.findViewById(R.id.version_text)
//        scanQrButton = view.findViewById(R.id.scan_qr_button)
        
        // Set app version
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = packageInfo.versionCode
            versionTextView.text = "Version: $versionName ($versionCode)"
        } catch (e: PackageManager.NameNotFoundException) {
            versionTextView.text = "Version: Unknown"
        }
        
        // QR code scanner button
        scanQrButton.setOnClickListener {
            // This would normally launch the QR scanner
            Toast.makeText(context, "QR scanner not implemented yet", Toast.LENGTH_SHORT).show()
        }
        
        return view
    }
}