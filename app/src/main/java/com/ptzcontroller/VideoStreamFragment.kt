package com.ptzcontroller

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.button.MaterialButtonToggleGroup

class VideoStreamFragment : Fragment(), Player.Listener {

    private lateinit var connectionManager: ConnectionManager
    private lateinit var playerView: PlayerView
    private lateinit var player: SimpleExoPlayer
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var btnRgbMode: Button
    private lateinit var btnIrMode: Button
    
    private var currentStreamUrl: String? = null
    private var currentStreamType: String = "rgb" // Default is RGB
    
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
        val view = inflater.inflate(R.layout.fragment_video_stream, container, false)
        
        // Initialize UI components
        playerView = view.findViewById(R.id.player_view)
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        errorTextView = view.findViewById(R.id.error_text)
        toggleGroup = view.findViewById(R.id.mode_toggle_group)
        btnRgbMode = view.findViewById(R.id.btn_rgb_mode)
        btnIrMode = view.findViewById(R.id.btn_ir_mode)
        
        // Set up player
        initializePlayer()
        
        // Set up mode toggle
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_rgb_mode -> switchCameraMode("rgb")
                    R.id.btn_ir_mode -> switchCameraMode("ir")
                }
            }
        }
        
        return view
    }
    
    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = player
        player.addListener(this)
        
        // Set default state
        showError("Not connected to camera")
    }
    
    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24 || player == null) {
            initializePlayer()
        }
        
        // Auto-connect to stream if we have connection
        if (connectionManager.isConnected()) {
            connectToStream()
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }
    
    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }
    
    private fun releasePlayer() {
        if (::player.isInitialized) {
            player.release()
        }
    }
    
    private fun connectToStream() {
        if (!connectionManager.isConnected()) {
            showError("Not connected to camera")
            return
        }
        
        // Determine stream URL based on connection type and current mode
        val host = when (connectionManager.getConnectionType()) {
            ConnectionType.WIFI -> {
                val ipParts = connectionManager.getConnectedDeviceName().split(":")
                ipParts[0]
            }
            ConnectionType.BLUETOOTH -> {
                // For Bluetooth, use a default IP (localhost)
                "127.0.0.1"
            }
            else -> null
        }
        
        if (host == null) {
            showError("Invalid connection")
            return
        }
        
        // Construct the stream URL
        val streamUri = when (currentStreamType) {
            "rgb" -> "rtsp://$host:8554/rgb"
            "ir" -> "rtsp://$host:8554/ir"
            else -> "rtsp://$host:8554/rgb" // Default to RGB
        }
        
        if (streamUri == currentStreamUrl && player.isPlaying) {
            // Already playing this stream
            return
        }
        
        currentStreamUrl = streamUri
        
        // Create media source
        val dataSourceFactory = DefaultDataSourceFactory(
            requireContext(),
            Util.getUserAgent(requireContext(), "PTZControllerApp")
        )
        
        val mediaSource: MediaSource = if (streamUri.startsWith("rtsp")) {
            RtspMediaSource.Factory()
                .createMediaSource(MediaItem.fromUri(Uri.parse(streamUri)))
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(streamUri)))
        }
        
        // Start playback
        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
        
        // Show loading indicator
        showLoading()
    }
    
    private fun switchCameraMode(mode: String) {
        if (!connectionManager.isConnected()) {
            Toast.makeText(context, "Not connected to camera", Toast.LENGTH_SHORT).show()
            return
        }
        
        currentStreamType = mode
        
        // Send command to switch camera mode
        val modeValue = if (mode == "rgb") 0 else 1
        val command = CameraCommand("mode", modeValue)
        connectionManager.sendCommand(command)
        
        // Reconnect to the new stream
        connectToStream()
        
        Toast.makeText(
            context,
            "Switching to ${if (mode == "rgb") "RGB" else "IR/Thermal"} mode",
            Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun showLoading() {
        loadingIndicator.visibility = View.VISIBLE
        errorTextView.visibility = View.GONE
        playerView.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        loadingIndicator.visibility = View.GONE
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = message
        playerView.visibility = View.GONE
    }
    
    private fun showPlayer() {
        loadingIndicator.visibility = View.GONE
        errorTextView.visibility = View.GONE
        playerView.visibility = View.VISIBLE
    }
    
    // ExoPlayer event listeners
    override fun onPlaybackStateChanged(state: Int) {
        when (state) {
            Player.STATE_READY -> showPlayer()
            Player.STATE_BUFFERING -> showLoading()
            Player.STATE_ENDED -> showError("Stream ended")
            Player.STATE_IDLE -> showError("Stream idle")
        }
    }
    
    override fun onPlayerError(error: com.google.android.exoplayer2.ExoPlaybackException) {
        showError("Stream error: ${error.message}")
    }
}
