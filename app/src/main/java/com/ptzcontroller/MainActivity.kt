package com.ptzcontroller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var connectionManager: ConnectionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize connection manager
        connectionManager = ConnectionManager()
        
        // Set up bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_video_stream -> {
                    loadFragment(DummyVideoStreamFragment.newInstance(connectionManager))
                    true
                }
                R.id.nav_camera_control -> {
                    loadFragment(DummyCameraControlFragment.newInstance(connectionManager))
                    true
                }
                R.id.nav_connection -> {
                    loadFragment(ConnectionFragment.newInstance(connectionManager))
                    true
                }
                R.id.nav_settings -> {
                    // For settings fragment, just use a placeholder for now
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        
        // Load default fragment
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_video_stream
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    // Placeholder SettingsFragment
    class SettingsFragment : Fragment() {
        override fun onCreateView(
            inflater: android.view.LayoutInflater,
            container: android.view.ViewGroup?,
            savedInstanceState: Bundle?
        ): android.view.View? {
            // Just a text view for now
            val textView = android.widget.TextView(requireContext())
            textView.text = getString(R.string.dummy_text)
            textView.gravity = android.view.Gravity.CENTER
            textView.layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            textView.setPadding(16, 16, 16, 16)
            return textView
        }
    }
    
    companion object {
        // Helper method to create a new instance of ConnectionFragment
        fun newConnectionFragment(connectionManager: ConnectionManager): ConnectionFragment {
            val fragment = ConnectionFragment()
            val args = Bundle()
            args.putSerializable("connectionManager", connectionManager)
            fragment.arguments = args
            return fragment
        }
    }
}