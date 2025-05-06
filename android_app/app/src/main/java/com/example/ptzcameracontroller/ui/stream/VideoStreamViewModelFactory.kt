package com.example.ptzcameracontroller.ui.stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ptzcameracontroller.data.repository.ConnectionRepository

/**
 * Factory for creating VideoStreamViewModel with dependencies
 */
class VideoStreamViewModelFactory(
    private val connectionRepository: ConnectionRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoStreamViewModel::class.java)) {
            return VideoStreamViewModel(connectionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}