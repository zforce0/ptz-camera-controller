package com.example.ptzcameracontroller.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ptzcameracontroller.data.repository.ConnectionRepository

/**
 * Factory for creating ConnectionViewModel with dependencies
 */
class ConnectionViewModelFactory(
    private val connectionRepository: ConnectionRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectionViewModel::class.java)) {
            return ConnectionViewModel(connectionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}