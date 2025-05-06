package com.example.ptzcameracontroller.ui.stream

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ptzcameracontroller.ui.control.ConnectionStatus

// Stream status enum
enum class StreamStatus(val description: String) {
    IDLE("Idle"),
    CONNECTING("Connecting..."),
    BUFFERING("Buffering..."),
    PLAYING("Playing"),
    STOPPED("Stopped"),
    ERROR("Error"),
    DISCONNECTED("Disconnected")
}

// Stream quality enum
enum class StreamQuality(val description: String) {
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor")
}

class VideoStreamViewModel : ViewModel() {

    // Stream repository - will be injected later
    private lateinit var streamRepository: StreamRepository

    // Stream URL
    private val _streamUrl = MutableLiveData<String>()
    
    // Stream status
    private val _streamStatus = MutableLiveData<StreamStatus>().apply {
        value = StreamStatus.IDLE
    }
    val streamStatus: LiveData<StreamStatus> = _streamStatus
    
    // Stream resolution
    private val _streamResolution = MutableLiveData<String>().apply {
        value = "N/A"
    }
    val streamResolution: LiveData<String> = _streamResolution
    
    // Stream FPS
    private val _streamFps = MutableLiveData<Int>().apply {
        value = 0
    }
    val streamFps: LiveData<Int> = _streamFps
    
    // Stream quality
    private val _streamQuality = MutableLiveData<StreamQuality>().apply {
        value = StreamQuality.GOOD
    }
    val streamQuality: LiveData<StreamQuality> = _streamQuality
    
    // Frame drop counter (for quality calculation)
    private var frameDrops = 0
    
    // Quality check interval (in frames)
    private val qualityCheckInterval = 30
    
    // Quality threshold
    private val poorQualityThreshold = 5  // frame drops per interval
    private val fairQualityThreshold = 2  // frame drops per interval
    
    // Connection status
    private val _connectionStatus = MutableLiveData<ConnectionStatus>().apply {
        value = ConnectionStatus.DISCONNECTED
    }
    val connectionStatus: LiveData<ConnectionStatus> = _connectionStatus
    
    // Get the stream URL (real implementation would get from repository)
    fun getStreamUrl(): String {
        return _streamUrl.value ?: "rtsp://192.168.1.100:8554/stream"
    }
    
    // Set stream status
    fun setStreamStatus(status: StreamStatus) {
        _streamStatus.postValue(status)
        
        // Update connection status based on stream status
        when (status) {
            StreamStatus.PLAYING -> _connectionStatus.postValue(ConnectionStatus.CONNECTED)
            StreamStatus.CONNECTING, StreamStatus.BUFFERING -> _connectionStatus.postValue(ConnectionStatus.CONNECTING)
            else -> _connectionStatus.postValue(ConnectionStatus.DISCONNECTED)
        }
    }
    
    // Set stream information
    fun setStreamInfo(width: Int, height: Int, fps: Int) {
        _streamResolution.postValue("${width}x${height}")
        _streamFps.postValue(fps)
    }
    
    // Set stream URL
    fun setStreamUrl(url: String) {
        _streamUrl.postValue(url)
    }
    
    // Report frame drops (for quality calculation)
    fun reportFrameDrop() {
        frameDrops++
        
        // Check quality every qualityCheckInterval frames
        if (frameDrops % qualityCheckInterval == 0) {
            calculateStreamQuality(frameDrops / qualityCheckInterval)
            frameDrops = 0
        }
    }
    
    // Calculate stream quality based on frame drops
    private fun calculateStreamQuality(dropsPerInterval: Int) {
        val quality = when {
            dropsPerInterval >= poorQualityThreshold -> StreamQuality.POOR
            dropsPerInterval >= fairQualityThreshold -> StreamQuality.FAIR
            else -> StreamQuality.GOOD
        }
        _streamQuality.postValue(quality)
    }
}