package com.example.ptzcameracontroller.ui.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ptzcameracontroller.data.repository.CameraControlRepository

/**
 * Factory for creating CameraControlViewModel with dependencies
 */
class CameraControlViewModelFactory(
    private val cameraControlRepository: CameraControlRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraControlViewModel::class.java)) {
            return CameraControlViewModel(cameraControlRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}