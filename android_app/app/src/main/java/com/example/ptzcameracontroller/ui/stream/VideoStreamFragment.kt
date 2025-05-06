package com.example.ptzcameracontroller.ui.stream

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ptzcameracontroller.data.repository.ConnectionRepository
import com.example.ptzcameracontroller.databinding.FragmentVideoStreamBinding
import com.example.ptzcameracontroller.utils.PreferenceManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoStreamFragment : Fragment() {

    private var _binding: FragmentVideoStreamBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: VideoStreamViewModel
    private lateinit var preferenceManager: PreferenceManager
    
    // ExoPlayer for video streaming
    private var player: ExoPlayer? = null
    
    // Connection check job
    private var connectionCheckJob: Job? = null
    
    // Current stream URL
    private var currentStreamUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoStreamBinding.inflate(inflater, container, false)
        preferenceManager = PreferenceManager(requireContext())
        
        // Set up ViewModel with repository
        val connectionRepository = ConnectionRepository(requireContext())
        val factory = VideoStreamViewModelFactory(connectionRepository)
        viewModel = ViewModelProvider(this, factory)[VideoStreamViewModel::class.java]
        
        // Set up UI
        setupStreamControls()
        
        // Observe ViewModel
        observeViewModel()
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePlayer()
    }
    
    override fun onResume() {
        super.onResume()
        if (player == null) {
            initializePlayer()
        }
        startConnectionCheck()
    }
    
    override fun onPause() {
        super.onPause()
        stopConnectionCheck()
        releasePlayer()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        _binding = null
    }
    
    /**
     * Initialize ExoPlayer
     */
    private fun initializePlayer() {
        player = ExoPlayer.Builder(requireContext()).build()
        
        // Set player view
        binding.videoView.player = player
        
        // Set up player listener
        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                handlePlayerError(error)
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        binding.progressLoading.visibility = View.GONE
                        binding.tvStreamStatus.text = "Streaming"
                    }
                    Player.STATE_BUFFERING -> {
                        binding.progressLoading.visibility = View.VISIBLE
                        binding.tvStreamStatus.text = "Buffering..."
                    }
                    Player.STATE_ENDED -> {
                        binding.progressLoading.visibility = View.GONE
                        binding.tvStreamStatus.text = "Stream ended"
                    }
                    Player.STATE_IDLE -> {
                        binding.progressLoading.visibility = View.GONE
                        binding.tvStreamStatus.text = "Idle"
                    }
                }
            }
        })
        
        // Try to connect to the stream
        connectToStream()
    }
    
    /**
     * Release ExoPlayer resources
     */
    private fun releasePlayer() {
        player?.release()
        player = null
    }
    
    /**
     * Set up stream control buttons
     */
    private fun setupStreamControls() {
        // Reconnect button
        binding.btnReconnect.setOnClickListener {
            connectToStream()
        }
        
        // Snapshot button
        binding.btnSnapshot.setOnClickListener {
            takeSnapshot()
        }
        
        // Record button
        binding.btnRecord.setOnClickListener {
            toggleRecording()
        }
        
        // Update stream quality from preferences
        updateStreamQualityUI()
    }
    
    /**
     * Update stream quality UI
     */
    private fun updateStreamQualityUI() {
        val qualityIndex = preferenceManager.getStreamQuality()
        val qualityText = when (qualityIndex) {
            0 -> "Low Quality"
            1 -> "Medium Quality"
            2 -> "High Quality"
            else -> "Medium Quality"
        }
        
        binding.tvStreamQuality.text = qualityText
    }
    
    /**
     * Observe ViewModel
     */
    private fun observeViewModel() {
        // Observe stream URL
        viewModel.streamUrl.observe(viewLifecycleOwner) { url ->
            if (url != currentStreamUrl) {
                currentStreamUrl = url
                if (url != null) {
                    playStream(url)
                } else {
                    // No stream URL available
                    binding.tvStreamStatus.text = "No stream available"
                    binding.progressLoading.visibility = View.GONE
                }
            }
        }
        
        // Observe connection status
        viewModel.isConnected.observe(viewLifecycleOwner) { connected ->
            binding.btnReconnect.isEnabled = connected
            binding.btnSnapshot.isEnabled = connected
            binding.btnRecord.isEnabled = connected
            
            if (connected && currentStreamUrl == null) {
                connectToStream()
            }
        }
    }
    
    /**
     * Connect to the stream
     */
    private fun connectToStream() {
        binding.progressLoading.visibility = View.VISIBLE
        binding.tvStreamStatus.text = "Connecting to stream..."
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val streamUrl = viewModel.getStreamUrl()
                if (streamUrl != null) {
                    currentStreamUrl = streamUrl
                    playStream(streamUrl)
                } else {
                    binding.tvStreamStatus.text = "Failed to get stream URL"
                    binding.progressLoading.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("VideoStreamFragment", "Error connecting to stream", e)
                binding.tvStreamStatus.text = "Error: ${e.message}"
                binding.progressLoading.visibility = View.GONE
            }
        }
    }
    
    /**
     * Play the stream
     * @param url Stream URL (RTSP)
     */
    private fun playStream(url: String) {
        val uri = Uri.parse(url)
        
        // Create media source
        val mediaSource = createMediaSource(uri)
        
        // Set media source
        player?.setMediaSource(mediaSource)
        player?.prepare()
        player?.playWhenReady = true
    }
    
    /**
     * Create media source from URI
     * @param uri Stream URI
     * @return MediaSource for the player
     */
    private fun createMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSource.Factory(requireContext())
        
        return if (uri.toString().startsWith("rtsp://")) {
            // RTSP source
            RtspMediaSource.Factory()
                .setForceUseRtpTcp(true)  // Force TCP for better reliability
                .createMediaSource(MediaItem.fromUri(uri))
        } else {
            // Progressive source (for testing with HTTP streams)
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
        }
    }
    
    /**
     * Handle player errors
     * @param error PlaybackException
     */
    private fun handlePlayerError(error: PlaybackException) {
        Log.e("VideoStreamFragment", "Player error: ${error.message}")
        binding.tvStreamStatus.text = "Error: ${error.message}"
        binding.progressLoading.visibility = View.GONE
    }
    
    /**
     * Take a snapshot of the current frame
     */
    private fun takeSnapshot() {
        // This would capture the current frame and save it
        // For now, just show a toast
        Toast.makeText(requireContext(), "Snapshot feature will be implemented", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Toggle recording on/off
     */
    private fun toggleRecording() {
        // This would start/stop recording
        // For now, just show a toast
        Toast.makeText(requireContext(), "Recording feature will be implemented", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Start periodic connection check
     */
    private fun startConnectionCheck() {
        // First check immediately
        viewModel.checkConnectionStatus()
        
        // Then start periodic checking
        connectionCheckJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                while (isActive) {
                    delay(5000) // Check every 5 seconds
                    viewModel.checkConnectionStatus()
                }
            } catch (e: Exception) {
                Log.e("VideoStreamFragment", "Error in connection check", e)
            }
        }
    }
    
    /**
     * Stop periodic connection check
     */
    private fun stopConnectionCheck() {
        connectionCheckJob?.cancel()
        connectionCheckJob = null
    }
}