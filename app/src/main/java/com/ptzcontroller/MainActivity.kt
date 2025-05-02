package com.ptzcontroller

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var bottomNav: BottomNavigationView
    
    // Fragments
    private val videoStreamFragment = VideoStreamFragment()
    private val cameraControlFragment = CameraControlFragment()
    private val connectionFragment = ConnectionFragment()
    private val settingsFragment = SettingsFragment()
    
    // Connection manager
    private lateinit var connectionManager: ConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize the connection manager
        connectionManager = ConnectionManager(this)
        
        // Initialize the bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener(this)
        
        // Set default fragment
        loadFragment(videoStreamFragment)
        
        // Share connection manager with fragments
        val bundle = Bundle()
        bundle.putSerializable("connectionManager", connectionManager)
        
        videoStreamFragment.arguments = bundle
        cameraControlFragment.arguments = bundle
        connectionFragment.arguments = bundle
        settingsFragment.arguments = bundle
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_stream -> {
                loadFragment(videoStreamFragment)
                return true
            }
            R.id.nav_controls -> {
                loadFragment(cameraControlFragment)
                return true
            }
            R.id.nav_connection -> {
                loadFragment(connectionFragment)
                return true
            }
            R.id.nav_settings -> {
                loadFragment(settingsFragment)
                return true
            }
        }
        return false
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        connectionManager.disconnect()
    }
}
