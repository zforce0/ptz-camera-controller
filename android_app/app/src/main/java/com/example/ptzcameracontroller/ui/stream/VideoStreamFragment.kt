package com.example.ptzcameracontroller.ui.stream

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ptzcameracontroller.R
import com.example.ptzcameracontroller.databinding.FragmentVideoStreamBinding
import com.example.ptzcameracontroller.utils.PreferenceManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.rtsp.RtspMediaSource
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoStreamFragment : Fragment() {

    private var _binding: FragmentVideoStreamBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: VideoStreamViewModel
    private lateinit var preferenceManager: PreferenceManager
    
    // ExoPlayer for RTSP video streaming
    private var player: ExoPlayer? = null
    
    // Recording state
    private var isRecording = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(VideoStreamViewModel::class.java)
        _binding = FragmentVideoStreamBinding.inflate(inflater, container, false)
        
        preferenceManager = PreferenceManager(requireContext())
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize the player
        initializePlayer()
        
        // Set up UI controls
        setupControls()
        
        // Observe view model changes
        observeViewModel()
    }
    
    private fun initializePlayer() {
        // Create ExoPlayer instance
        player = ExoPlayer.Builder(requireContext()).build()
        
        // Set up the player view
        binding.videoPlayer.player = player
        
        // Add player event listener
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        binding.loadingSpinner.visibility = View.VISIBLE
                        viewModel.setStreamStatus(StreamStatus.BUFFERING)
                    }
                    Player.STATE_READY -> {
                        binding.loadingSpinner.visibility = View.GONE
                        viewModel.setStreamStatus(StreamStatus.PLAYING)
                        
                        // Get video resolution
                        player?.videoFormat?.let { format ->
                            val width = format.width
                            val height = format.height
                            val fps = format.frameRate.toInt()
                            
                            viewModel.setStreamInfo(width, height, fps)
                        }
                    }
                    Player.STATE_IDLE -> {
                        binding.loadingSpinner.visibility = View.GONE
                        viewModel.setStreamStatus(StreamStatus.IDLE)
                    }
                    Player.STATE_ENDED -> {
                        binding.loadingSpinner.visibility = View.GONE
                        viewModel.setStreamStatus(StreamStatus.STOPPED)
                        
                        // Try to reconnect if auto-reconnect is enabled
                        if (preferenceManager.getAutoReconnect()) {
                            reconnectStream()
                        }
                    }
                }
            }
            
            override fun onPlayerError(error: PlaybackException) {
                binding.loadingSpinner.visibility = View.GONE
                viewModel.setStreamStatus(StreamStatus.ERROR)
                
                // Display error message
                Toast.makeText(
                    requireContext(),
                    "Stream error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Try to reconnect if auto-reconnect is enabled
                if (preferenceManager.getAutoReconnect()) {
                    reconnectStream()
                }
            }
        })
        
        // Start playback
        connectToStream()
    }
    
    private fun connectToStream() {
        val streamUrl = viewModel.getStreamUrl()
        
        if (streamUrl.isNotEmpty()) {
            // Create RTSP media source
            val mediaSource = RtspMediaSource.Factory()
                .setForceUseRtpTcp(true) // Use TCP for more reliable streaming
                .createMediaSource(MediaItem.fromUri(streamUrl))
            
            // Set the media source and prepare the player
            player?.setMediaSource(mediaSource)
            player?.prepare()
            player?.playWhenReady = true
            
            // Update stream status
            viewModel.setStreamStatus(StreamStatus.CONNECTING)
            binding.loadingSpinner.visibility = View.VISIBLE
        } else {
            // No stream URL available
            viewModel.setStreamStatus(StreamStatus.DISCONNECTED)
            Toast.makeText(
                requireContext(),
                "No stream URL available. Please connect to a camera first.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun reconnectStream() {
        // Stop current playback
        player?.stop()
        
        // Show loading spinner
        binding.loadingSpinner.visibility = View.VISIBLE
        
        // Update stream status
        viewModel.setStreamStatus(StreamStatus.CONNECTING)
        
        // Wait a moment before reconnecting
        binding.root.postDelayed({
            connectToStream()
        }, 2000)
    }
    
    private fun setupControls() {
        // Reconnect button
        binding.btnReconnect.setOnClickListener {
            reconnectStream()
        }
        
        // Snapshot button
        binding.btnSnapshot.setOnClickListener {
            takeSnapshot()
        }
        
        // Record button
        binding.btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }
    
    private fun observeViewModel() {
        // Observe stream status
        viewModel.streamStatus.observe(viewLifecycleOwner) { status ->
            binding.streamStatus.text = status.description
            binding.streamStatus.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    when (status) {
                        StreamStatus.PLAYING -> R.color.status_good
                        StreamStatus.BUFFERING -> R.color.status_warning
                        StreamStatus.CONNECTING -> R.color.status_warning
                        StreamStatus.ERROR -> R.color.status_error
                        else -> R.color.status_idle
                    }
                )
            )
        }
        
        // Observe stream resolution
        viewModel.streamResolution.observe(viewLifecycleOwner) { resolution ->
            binding.streamResolution.text = resolution
        }
        
        // Observe stream FPS
        viewModel.streamFps.observe(viewLifecycleOwner) { fps ->
            binding.streamFps.text = fps.toString()
        }
        
        // Observe stream quality
        viewModel.streamQuality.observe(viewLifecycleOwner) { quality ->
            binding.streamQuality.text = quality.description
            binding.streamQuality.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    when (quality) {
                        StreamQuality.GOOD -> R.color.status_good
                        StreamQuality.FAIR -> R.color.status_warning
                        StreamQuality.POOR -> R.color.status_poor
                    }
                )
            )
        }
    }
    
    private fun takeSnapshot() {
        // Get current frame from video surface
        binding.videoPlayer.bitmap?.let { bitmap ->
            // Save to file
            saveImageToGallery(bitmap)
        } ?: run {
            Toast.makeText(
                requireContext(),
                "Failed to capture snapshot",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun saveImageToGallery(bitmap: Bitmap) {
        try {
            // Create directory if it doesn't exist
            val directory = File(requireContext().getExternalFilesDir(null), "Snapshots")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            // Create file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(directory, "PTZ_Snapshot_$timestamp.jpg")
            
            // Save bitmap to file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_snapshot_saved),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Failed to save snapshot: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun startRecording() {
        // This would require a more complex implementation with MediaRecorder
        // For now, we'll just show a toast message
        isRecording = true
        binding.btnRecord.text = getString(R.string.stream_stop_recording)
        binding.btnRecord.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.status_error))
        
        Toast.makeText(
            requireContext(),
            getString(R.string.msg_recording_started),
            Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun stopRecording() {
        // This would require a more complex implementation with MediaRecorder
        // For now, we'll just show a toast message
        isRecording = false
        binding.btnRecord.text = getString(R.string.stream_record)
        binding.btnRecord.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
        
        Toast.makeText(
            requireContext(),
            getString(R.string.msg_recording_stopped),
            Toast.LENGTH_SHORT
        ).show()
    }
    
    override fun onPause() {
        super.onPause()
        
        // Pause the player when the fragment is paused
        player?.playWhenReady = false
    }
    
    override fun onResume() {
        super.onResume()
        
        // Resume the player when the fragment is resumed
        player?.playWhenReady = true
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // Release the player when the view is destroyed
        player?.release()
        player = null
        
        _binding = null
    }
}