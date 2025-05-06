package com.ptzcontroller.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptzcontroller.ConnectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoStreamViewModel(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    // Stream URL
    private val _streamUrl = MutableLiveData<String?>()
    val streamUrl: LiveData<String?> = _streamUrl
    
    // Connection status
    private val _isConnected = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isConnected: LiveData<Boolean> = _isConnected
    
    // Stream quality (0 = low, 1 = medium, 2 = high)
    private val _streamQuality = MutableLiveData<Int>().apply {
        value = 1 // Default: medium
    }
    val streamQuality: LiveData<Int> = _streamQuality
    
    // Current FPS
    private val _currentFps = MutableLiveData<Int>().apply {
        value = 0
    }
    val currentFps: LiveData<Int> = _currentFps
    
    // Stream resolution (e.g., "1280x720")
    private val _streamResolution = MutableLiveData<String>()
    val streamResolution: LiveData<String> = _streamResolution
    
    /**
     * Check connection status
     */
    fun checkConnectionStatus() {
        viewModelScope.launch {
            val connected = connectionRepository.isWiFiConnected()
            _isConnected.postValue(connected)
            
            if (connected && _streamUrl.value == null) {
                // If connected but no stream URL, try to get it
                getStreamUrl()
            }
        }
    }
    
    /**
     * Get stream URL from the server
     * @return Stream URL or null if not available
     */
    suspend fun getStreamUrl(): String? {
        return try {
            val url = connectionRepository.getStreamUrl()
            _streamUrl.postValue(url)
            url
        } catch (e: Exception) {
            _streamUrl.postValue(null)
            null
        }
    }
    
    /**
     * Set stream quality
     * @param quality Quality index (0 = low, 1 = medium, 2 = high)
     */
    fun setStreamQuality(quality: Int) {
        if (quality in 0..2) {
            _streamQuality.value = quality
            // Update stream URL with new quality parameter
            refreshStreamUrl()
        }
    }
    
    /**
     * Refresh stream URL (e.g., when quality changes)
     */
    private fun refreshStreamUrl() {
        viewModelScope.launch {
            getStreamUrl()
        }
    }
    
    /**
     * Update stream metrics
     * @param fps Current frames per second
     * @param resolution Current resolution (e.g., "1280x720")
     */
    fun updateStreamMetrics(fps: Int, resolution: String) {
        _currentFps.value = fps
        _streamResolution.value = resolution
    }
}