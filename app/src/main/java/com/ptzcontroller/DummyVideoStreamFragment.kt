package com.ptzcontroller

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Dummy implementation of the video stream fragment
 * Used for compilation purposes while the real implementation is being developed
 */
class DummyVideoStreamFragment : Fragment() {
    
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
    ): View {
        // Create a simple layout with a text view
        val frameLayout = FrameLayout(requireContext())
        frameLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        frameLayout.setBackgroundColor(Color.BLACK)
        
        val textView = TextView(requireContext())
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.text = getString(R.string.dummy_text)
        textView.setTextColor(Color.WHITE)
        textView.textSize = 16f
        
        // Center the text
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = android.view.Gravity.CENTER
        textView.layoutParams = params
        
        frameLayout.addView(textView)
        
        return frameLayout
    }
    
    override fun onResume() {
        super.onResume()
        // Check if connected
        if (connectionManager.isConnected()) {
            // Get stream URLs
            val streamUrls = connectionManager.getStreamUrls()
            println("Stream URLs: $streamUrls")
        }
    }
    
    companion object {
        @JvmStatic
        fun newInstance(connectionManager: ConnectionManager): DummyVideoStreamFragment {
            val fragment = DummyVideoStreamFragment()
            val args = Bundle()
            args.putSerializable("connectionManager", connectionManager)
            fragment.arguments = args
            return fragment
        }
    }
}