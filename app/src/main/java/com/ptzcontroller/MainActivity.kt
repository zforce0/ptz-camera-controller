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
                    loadFragment(VideoStreamFragment.newInstance(connectionManager))
                    true
                }
                R.id.nav_camera_control -> {
                    loadFragment(CameraControlFragment.newInstance(connectionManager))
                    true
                }
                R.id.nav_connection -> {
                    loadFragment(ConnectionFragment.newInstance(connectionManager))
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

        // Load default fragment
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_connection
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}